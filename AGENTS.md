# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project context

`go-reader` is a Go port of `py-reader`: a Chinese-language web-novel reader with both a WebUI (chi + embedded templates) and a TUI (bubbletea) front-end, sharing the same parser/processor/repo backend. UI selection happens at startup via `UI_TYPE`. The README and templates are Chinese; preserve Chinese strings in user-facing code.

## Commands

```bash
go mod tidy
go run ./cmd/reader              # runs WebUI or TUI based on UI_TYPE
go build -o reader.exe ./cmd/reader

go test ./...                    # all tests
go test ./tests/parser -run TestRuleParserCSSRules -v   # single test
```

`.env` (or `.env.example`) must exist next to the executable; `config.Get()` looks up `.env` relative to the running binary, not the working directory. Database connection is configured via `DB_URI` and may point to PostgreSQL, MySQL, or SQLite. PostgreSQL schema is still encoded in the URI query string via `search_path=...` when using Postgres. Tables are auto-migrated via GORM (`repo.Migrate`) at WebUI/TUI startup; seed `book_sources` from `configs/booksources.sql`.

## Architecture

### Request flow

`cmd/reader/main.go` dispatches to `web.Run` or `tui.Run`. Both create a `processor.Processor` that wires together a `webdriver.Driver`, the `parser.Engine`, and persistence:

```
URL → Driver.Get → Driver.PageSource → Engine.ParseContent → postProcess → repo.UpdateRecord
```

`processor.ReadURLContent` is the single entry point for "fetch and parse a URL"; both the WebUI handler (`/api/v1/read`) and the TUI's `cmdOpenURL` call into it.

### Parser engine (the central abstraction)

`internal/parser/engine.go` routes each page through two strategies:

1. **`RuleParser`** — looks up book sources from Postgres, picks the one whose `BookSourceURL` best matches the page (host must equal, path prefix wins; see `matchScore` in `url.go`), and runs its CSS/XPath/regex rules. Returns `ErrRuleExecution` if the content rule yields nothing.
2. **`DefaultParser`** — fallback heuristic. Strips noise tags, captures `下一章`/`下一页` anchors, and picks the largest deduped non-`<div>` text block (within 15% of the longest). Title extraction goes through an LLM (`/chat/completions`, OpenAI-compatible) if an enabled `LLMConfig` row exists, else falls back to `<title>`.

`RuleParser` implements a **restricted subset of Legado rules**:

- `@css:selector@field` — field is `text` (default), `html`, `all`, `textNodes`/`ownText`, `href`/`src`, or `@attr`. Nested `@css:` allowed in field position.
- `@XPath:...` and bare `//...` — converted to CSS by `xpathToCSS`. Only supports `//tag`, `tag[@attr='v']`, `tag[contains(@attr,'v')]`, and `tag[n]` predicates. Anything else returns `""` and the rule fails silently.
- `:pattern` — leading `:` runs `regexFindAll` on the raw HTML.
- `##pat##repl[###]` — chained regex replacements after the base rule. `$1` group references are rewritten to Go's `${1}` template form before `re.ReplaceAllString`.
- URL-shaped rules (containing `href`/`src`/`Url`/...) get resolved against the page URL via `normalizeURL`, which also strips trailing `,{"..."}` Legado option markers.

`getTextSep` (in `default_parser.go`) is a deliberate replacement for goquery's `Text()`: it walks descendant text nodes and joins with a separator, mimicking BeautifulSoup's `Tag.get_text(separator=...)`. Use it when joining nested `<p>`/`<br>` text — goquery's built-in `Text()` flattens without separators and loses paragraph breaks.

### Webdriver layer

`webdriver.Driver` is a browser-driver-style interface (`Get`/`PageSource`/`ExecuteScript`/...) but only `NoneDriver` is implemented — plain `net/http` with a Chrome User-Agent. `webdriver.New` ignores its argument and always returns `NoneDriver`; `CHROME_DRIVER`, `CHROME_VERSION`, `CHROME_DATA_DIR` are placeholders for future CDP/chromedp integration. Don't add real chromedp deps without coordinating — the comment in `factory.go` calls this out explicitly.

### Repo layer

GORM models live in `internal/model`. `BookSource` uses **camelCase column names** (`bookSourceUrl`, `bookName`, `chapterName`, `nextContentUrl`) for Legado-schema compatibility — keep column tags as-is. `repo.UpdateRecord` upserts keyed by `book_name`, not by URL. `repo.GetEnabledLLMConfig` swallows "not found" errors and returns `(nil, nil)` so the engine can branch on the nil pointer.

### Post-processing

`processor.postProcess`:
- Always runs `postprocess.ToSimplified` (OpenCC `t2s`) on book and chapter names.
- Normalises content paragraph spacing to exactly one blank line between paragraphs.
- **TUI mode only**: prepends each line with `postprocess.GenLog()` — a fake Java/Spring log prefix (`<timestamp> <LEVEL> <classname>: `). This is a deliberate "screen-share disguise" so the terminal looks like a build log, not a novel. Don't strip this when refactoring the TUI.

### WebUI specifics

- Templates are served from an `embed.FS` rooted at `internal/web/templates` (`static.go`). 
- `AuthMiddleware` is a no-op when `WEBUI_TOKEN` is empty. When set, all paths require the `auth_token` cookie except `/`, `/api/v1/login`, `/ui/login.html`, `/ui/style*`, and `/ui/sidebar*`. API requests get 401; HTML requests get a 302 to `/ui/login.html`.

### TUI specifics

`internal/tui/app.go` is a single-file Bubble Tea model with two display modes (`modeContent`, `modeBooks`). Notable behaviours:

**Input & key bindings:**
- `Ctrl+B` is a **prefix/leader key** (sets `prefixKey = true`). After pressing it, a second key is expected: `b` → 书架, `n` → 下一章, `q` → 退出, `r` → 重新读取当前内容 (`cmdReadCurrentContent`). Any other key clears the prefix state.
- Pressing `/` opens the bottom input bar (`showInput = true`); backspacing to empty closes it. `bottomReserve` (4) is the baked-in layout constant used by `resizeViewport` to leave room for the input + hint bar.
- Slash commands: `/exit`, `/books`, `/next`, `/refresh` (re-fetches current chapter URL), `/reload` (re-reads current content without re-fetching), `/open <url>`. Bare `http...` input is treated as `/open`.

**Content & navigation:**
- `lineLinks` parallel-array maps each rendered viewport line to a clickable URL (chapter rows in 书架 mode, the trailing `下一章: <url>` line in content mode). Mouse clicks are mapped via `urlAt(screenY)` accounting for `viewport.YOffset` for scrolled state. Mouse wheel events are delegated to the viewport for scrolling.
- `viewportHeader` prepends the chapter title (or "Reader" as fallback) to the viewport content so it scrolls with the text.
- A spinner + "加载中: <url>" message is shown in the viewport while a request is in flight (`renderLoading`).
