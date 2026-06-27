This file provides guidance to agentic assistants when working with code in this repository.

## Project Context

`go-reader`: a Chinese-language web-novel reader with both a WebUI (chi + embedded templates) and a TUI (bubbletea) front-end, sharing the same parser/processor/repo backend. UI selection happens at startup via `UI_TYPE`. The README and templates are Chinese; preserve Chinese strings in user-facing code.

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

### Service & Domain Layer

The `reading.Service` struct in `internal/reading/reading.go` serves as the app-facing reading backend. Both front-ends routing reading and bookshelf operations adapt to it.
- **Service API**:
  - `Open(ctx, url)`: Fetches a URL, parses it, updates the reading record, and applies the log disguise if enabled.
  - `ReloadCurrent(ctx)`: Re-parses the webdriver's current page source without re-fetching it, and applies the disguise.
  - `Bookshelf(ctx)`: Retrieves all reading records from the repository sorted by update time descending.
- **Disguise Configuration**:
  - Built with optional configuration: `reading.New(proc, reading.Disguised())` activates the screen-share log disguise on content. The TUI uses the disguised service; the WebUI uses a standard service.

### Request Flow

```
Front-End (TUI / WebUI)
    │
    ▼
reading.Service (Open / ReloadCurrent / Bookshelf)
    │
    ▼
processor.Processor
    │
    ├──► webdriver.Driver (Get / PageSource)
    │
    ├──► parser.Engine (ParseContent)
    │
    ├──► processor.postProcess (t2s translation, spacing)
    │
    └─► repo (UpdateRecord / GetAllRecords)
```

For authoring and testing book sources, the WebUI bypasses `reading.Service` and calls `processor.PreviewSource(ctx, src, url)` directly. This previews the parsing rules without modifying database records or applying post-processing.

### Parser Engine

`internal/parser/engine.go` routes each page through two strategies:

1. **`RuleParser`** — looks up book sources from Postgres, picks the one whose `BookSourceURL` best matches the page (host must equal, path prefix wins; see `matchScore` in `url.go`), and runs its CSS/XPath/regex rules. Returns `ErrRuleExecution` if the content rule yields nothing.
2. **`DefaultParser`** — fallback heuristic. Strips noise tags, captures `下一章`/`下一页` anchors, and picks the largest deduped non-`<div>` text block (within 15% of the longest). Title extraction goes through an LLM (`/chat/completions`, OpenAI-compatible) if an enabled `LLMConfig` row exists, else falls back to `<title>`.

`RuleParser` implements a **restricted subset of Legado rules**:

- `@css:selector@field` — field is `text` (default), `html`, `all`, `textNodes`/`ownText`, `href`/`src`, or `@attr`. Nested `@css:` allowed in field position.
- `@XPath:...` and bare `//...` — converted to CSS by `xpathToCSS`. Only supports `//tag`, `tag[@attr='v']`, `tag[contains(@attr,'v')]`, and `tag[n]` predicates. Anything else returns `""` and the rule fails silently.
- `:pattern` — leading `:` runs `regexFindAll` on the raw HTML.
- `##pat##repl[###]` — chained regex replacements after the base rule. `$1` group references are rewritten to Go's `${1}` template form before `re.ReplaceAllString`.
- URL-shaped rules (containing `href`/`src`/`Url`/...) get resolved against the page URL via `normalizeURL`, which also strips trailing `,{"..."}` Legado option markers.

`getTextSep` (in `default_parser.go`) is a replacement for goquery's `Text()`: it walks descendant text nodes and joins with a separator. Use it when joining nested `<p>`/`<br>` text as goquery's built-in `Text()` flattens without separators and loses paragraph breaks.

### Webdriver Layer

`webdriver.Driver` is a browser-driver-style interface (`Get`/`PageSource`/`ExecuteScript`/...) but only `NoneDriver` is implemented — plain `net/http` with a Chrome User-Agent. `webdriver.New` ignores its argument and always returns `NoneDriver`. `CHROME_DRIVER`, `CHROME_VERSION`, `CHROME_DATA_DIR` are placeholders. Do not add real chromedp dependencies without coordinating.

### Repo Layer

GORM models live in `internal/model`. `BookSource` uses **camelCase column names** (`bookSourceUrl`, `bookName`, `chapterName`, `nextContentUrl`) for Legado-schema compatibility — keep column tags as-is. `repo.UpdateRecord` upserts keyed by `book_name`, not by URL. `repo.GetEnabledLLMConfig` swallows "not found" errors and returns `(nil, nil)`.

### Post-Processing & Disguise

- **General Post-Processing** (`processor.postProcess`):
  - Runs `postprocess.ToSimplified` (OpenCC `t2s`) on book, chapter names, and content.
  - Normalizes content paragraph spacing to exactly one blank line between paragraphs.
- **Log Disguise** (`postprocess.Disguise`):
  - Enabled via `reading.Service` (TUI mode).
  - Prepends each line with `postprocess.GenLog()` — a fake Java/Spring log prefix (`<timestamp> <LEVEL> <classname>: `) so the terminal looks like a build log.

### WebUI Specifics

- Templates are served from an `embed.FS` rooted at `internal/web/templates` (`static.go`).
- `AuthMiddleware` is a no-op when `WEBUI_TOKEN` is empty. When set, all paths require the `auth_token` cookie except `/`, `/api/v1/login`, `/ui/login.html`, `/ui/style*`, and `/ui/sidebar*`. API requests get 401; HTML requests get a 302 to `/ui/login.html`.

### TUI Specifics

`internal/tui/app.go` is a Bubble Tea model with display modes `modeContent` and `modeBooks`.

**Loading & Request Orchestration:**
- The model implements `beginLoad(loadURL, timeout)` to manage async requests.
- It cancels in-flight operations via a `context.CancelFunc` and maintains an incrementing `reqID`.
- Incoming async messages (`chapterMsg`, `booksMsg`, `backupMsg`) are discarded in `Update` if their `reqID` does not match the model's current `reqID`, preventing slow/outdated operations from clobbering the view.

**Input & Key Bindings:**
- `Ctrl+B` is a prefix/leader key (sets `prefixKey = true`). After pressing it, a second key is expected: `b` → 书架, `n` → 下一章, `q` → 退出. Any other key clears the prefix state.
- Pressing `/` opens the bottom input bar (`showInput = true`); backspacing to empty closes it. `bottomReserve` (4) is the layout constant used by `resizeViewport` to leave room for the input + hint bar.
- Slash commands: `/exit`, `/quit`, `/books`, `/next`, `/refresh` (re-fetches current chapter URL), `/open <url>`, `/export <path>`, and `/import <path>`. Bare `http...` input is treated as `/open`.

**Content & Navigation:**
- `lineLinks` maps each rendered viewport line to a clickable URL. Mouse clicks are mapped via `urlAt(screenY)` accounting for `viewport.YOffset`. Mouse wheel events scroll the viewport.
- `viewportHeader` prepends the chapter title (or "Reader" as fallback) to the viewport content so it scrolls with the text.
- A spinner + "加载中: <url>" message is shown in the viewport while a request is in flight.
- **Prefetching**: When a chapter loads successfully and has a `NextURL`, the TUI silently calls `cmdPrefetch` which warms the processor's LRU cache via `svc.Prefetch`. This does not affect the loading/spinner state.
