package tui

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/charmbracelet/bubbles/spinner"
	"github.com/charmbracelet/bubbles/viewport"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/charmbracelet/x/ansi"
	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/model"
	"github.com/go-reader/reader/internal/processor"
	"github.com/go-reader/reader/internal/reading"
	"github.com/go-reader/reader/internal/repo"
)

type chapterMsg struct {
	chapter *model.ChapterContent
	err     error
	reqID   int
}

type booksMsg struct {
	records []model.Record
	err     error
	reqID   int
}

type backupMsg struct {
	action string
	path   string
	stats  repo.BackupStats
	err    error
	reqID  int
}

type mode int

const (
	modeContent mode = iota
	modeBooks
)

// Model is the Bubble Tea model.
type Model struct {
	svc      *reading.Service
	state    *model.ChapterContent
	input    string
	width    int
	height   int
	mode     mode
	viewport viewport.Model
	spinner  spinner.Model
	loading  bool
	loadURL  string
	// showInput controls whether the bottom input + hint bar is rendered;
	// toggle with Ctrl+B.
	showInput bool
	// lineLinks maps each 0-based line index in the viewport content to a URL
	// that should be opened when the line is clicked. Empty slots are inert.
	lineLinks []string
	prefixKey bool
	// books holds the records shown in modeBooks; bookSel is the
	// keyboard-selected index into it.
	books   []model.Record
	bookSel int
	// cancel aborts the current in-flight load so new input supersedes it;
	// reqID tags each load so a superseded request's result is discarded.
	cancel context.CancelFunc
	reqID  int
}

const (
	bottomReserve = 4 // bordered input + hint (conservative)
)

// Run starts the TUI until the user quits or ctx is cancelled.
func Run(ctx context.Context) error {
	if err := repo.Migrate(); err != nil {
		return err
	}
	proc := processor.New(config.Get().ChromeDriver)
	if err := proc.Driver.Start(ctx); err != nil {
		return err
	}
	defer proc.Driver.Stop(context.Background())

	sp := spinner.New()
	sp.Spinner = spinner.Dot
	sp.Style = lipgloss.NewStyle().Foreground(lipgloss.Color("212"))

	svc := reading.New(proc, reading.Disguised())
	m := &Model{
		svc:       svc,
		state:     &model.ChapterContent{},
		viewport:  viewport.New(80, 20),
		spinner:   sp,
		showInput: false,
		prefixKey: false,
	}
	_, err := tea.NewProgram(m, tea.WithAltScreen(), tea.WithMouseCellMotion()).Run()
	return err
}

func (m *Model) Init() tea.Cmd {
	return m.cmdBooks()
}

func (m *Model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.WindowSizeMsg:
		m.width, m.height = msg.Width, msg.Height
		m.resizeViewport()
		return m, nil
	case tea.KeyMsg:
		if m.prefixKey {
			var cmd tea.Cmd = nil
			switch string(msg.Runes) {
			case "b":
				cmd = m.cmdBooks()
			case "n":
				cmd = m.cmdOpenURL(m.state.NextURL)
			case "q":
				return m, tea.Quit
			case "r":
				cmd = m.cmdReadCurrentContent()
			}
			m.prefixKey = false
			return m, cmd
		}
		switch msg.Type {
		case tea.KeyCtrlB:
			if !m.prefixKey {
				m.prefixKey = true
			}
			return m, nil
		case tea.KeyEnter:
			if !m.showInput {
				if m.mode == modeBooks && len(m.books) > 0 {
					return m, m.cmdOpenURL(m.books[m.bookSel].ChapterURL)
				}
				return m, nil
			}
			m.showInput = false
			m.resizeViewport()
			var m, cmd = m.handleEnter()
			return m, cmd
		case tea.KeyBackspace:
			if !m.showInput {
				return m, nil
			}
			if len(m.input) > 0 {
				m.input = m.input[:len(m.input)-1]
			}
			if len(m.input) == 0 {
				m.showInput = !m.showInput
				m.resizeViewport()
			}
			return m, nil
		case tea.KeyRunes, tea.KeySpace:
			if !m.showInput {
				if string(msg.Runes) == "/" {
					m.showInput = true
					m.input = "/"
					m.resizeViewport()
					return m, nil
				}
				return m, nil
			}
			m.input += string(msg.Runes)
			return m, nil
		case tea.KeyUp, tea.KeyDown:
			if m.mode == modeBooks && !m.showInput && len(m.books) > 0 {
				if msg.Type == tea.KeyUp && m.bookSel > 0 {
					m.bookSel--
				} else if msg.Type == tea.KeyDown && m.bookSel < len(m.books)-1 {
					m.bookSel++
				}
				m.renderBooks()
				m.ensureBookVisible()
				return m, nil
			}
			var cmd tea.Cmd
			m.viewport, cmd = m.viewport.Update(msg)
			return m, cmd
		case tea.KeyPgUp, tea.KeyPgDown, tea.KeyHome, tea.KeyEnd:
			var cmd tea.Cmd
			m.viewport, cmd = m.viewport.Update(msg)
			return m, cmd
		case tea.KeyRight:
			return m, m.cmdOpenURL(m.state.NextURL)
		}

	case tea.MouseMsg:
		if msg.Action == tea.MouseActionPress && msg.Button == tea.MouseButtonLeft {
			if url := m.urlAt(msg.Y); url != "" {
				return m, m.cmdOpenURL(url)
			}
		}
		// Delegate wheel scrolling (and other motion events) to the viewport.
		var cmd tea.Cmd
		m.viewport, cmd = m.viewport.Update(msg)
		return m, cmd

	case chapterMsg:
		if msg.reqID != m.reqID {
			return m, nil
		}
		m.loading = false
		m.loadURL = ""
		if msg.err != nil {
			m.lineLinks = nil
			m.viewport.SetContent(m.wrap("错误: " + msg.err.Error()))
			m.viewport.GotoTop()
			return m, nil
		}
		m.mode = modeContent
		m.lineLinks = nil
		m.state = msg.chapter
		m.renderContent()
		return m, nil

	case booksMsg:
		if msg.reqID != m.reqID {
			return m, nil
		}
		m.loading = false
		m.loadURL = ""
		if msg.err != nil {
			m.lineLinks = nil
			m.viewport.SetContent(m.wrap("错误: " + msg.err.Error()))
			m.viewport.GotoTop()
			return m, nil
		}
		m.books = msg.records
		m.bookSel = 0
		m.renderBooks()
		m.viewport.GotoTop()
		return m, nil

	case backupMsg:
		if msg.reqID != m.reqID {
			return m, nil
		}
		m.loading = false
		m.loadURL = ""
		m.renderBackupResult(msg)
		return m, nil

	case spinner.TickMsg:
		if !m.loading {
			return m, nil
		}
		var cmd tea.Cmd
		m.spinner, cmd = m.spinner.Update(msg)
		m.renderLoading()
		return m, cmd
	}
	return m, nil
}

const helpText = `命令:
  /help              显示本帮助
  /books             打开书架
  /next              下一章
  /refresh           重新获取当前章节
  /reload            重新读取当前内容（不重新获取）
  /open <url>        打开指定 URL
  /export <path>     导出数据库到备份文件
  /import <path>     从备份文件导入数据库
  /exit, /quit       退出
  <url>              直接输入网址打开

快捷键:
  /                  打开命令输入栏
  Enter              书架: 打开选中书籍 / 输入: 提交命令
  Backspace          删除输入（清空时关闭输入栏）
  ↑ / ↓              书架: 上下选择 / 内容: 滚动
  PgUp / PgDn        翻页滚动
  Home / End         滚动到顶部 / 底部
  → 右方向键          下一章
  鼠标左键            点击链接（章节 / 下一章）
  鼠标滚轮            滚动内容

Ctrl+B 前缀键（按下后再按下列键）:
  b                  书架
  n                  下一章
  r                  重新读取当前内容
  q                  退出`

func (m *Model) handleEnter() (tea.Model, tea.Cmd) {
	cmd := strings.TrimSpace(m.input)
	m.input = ""
	switch {
	case cmd == "/help":
		m.renderMessage("帮助", helpText)
		return m, nil
	case cmd == "/exit" || cmd == "/quit":
		return m, tea.Quit
	case cmd == "/books":
		return m, m.cmdBooks()
	case cmd == "/next":
		if m.state.NextURL != "" {
			return m, m.cmdOpenURL(m.state.NextURL)
		}
	case cmd == "/refresh":
		if m.state.ChapterURL != "" {
			return m, m.cmdOpenURL(m.state.ChapterURL)
		}
	case cmd == "/reload":
		if m.state.ChapterURL != "" {
			return m, m.cmdReadCurrentContent()
		}
	case strings.HasPrefix(cmd, "/open "):
		return m, m.cmdOpenURL(strings.TrimPrefix(cmd, "/open "))
	case cmd == "/export":
		m.renderMessage("备份", "用法: /export path/to/backup.json")
		return m, nil
	case strings.HasPrefix(cmd, "/export "):
		path := commandPath(cmd, "/export ")
		if path == "" {
			m.renderMessage("备份", "用法: /export path/to/backup.json")
			return m, nil
		}
		return m, m.cmdExport(path)
	case cmd == "/import":
		m.renderMessage("备份", "用法: /import path/to/backup.json")
		return m, nil
	case strings.HasPrefix(cmd, "/import "):
		path := commandPath(cmd, "/import ")
		if path == "" {
			m.renderMessage("备份", "用法: /import path/to/backup.json")
			return m, nil
		}
		return m, m.cmdImport(path)
	case strings.HasPrefix(cmd, "http"):
		return m, m.cmdOpenURL(cmd)
	}
	return m, nil
}

// beginLoad cancels any in-flight load, then arms loading state for a new one.
// It returns the request context, its cancel func (the spawned goroutine should
// defer it), and a request id. Results whose id no longer matches m.reqID are
// discarded by the Update handlers so a superseded request can't clobber the view.
func (m *Model) beginLoad(loadURL string, timeout time.Duration) (context.Context, context.CancelFunc, int) {
	if m.cancel != nil {
		m.cancel()
	}
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	m.cancel = cancel
	m.reqID++
	m.loading = true
	m.loadURL = loadURL
	m.lineLinks = nil
	m.renderLoading()
	return ctx, cancel, m.reqID
}

func (m *Model) cmdOpenURL(url string) tea.Cmd {
	ctx, cancel, id := m.beginLoad(url, 60*time.Second)
	load := func() tea.Msg {
		defer cancel()
		c, err := m.svc.Open(ctx, url)
		return chapterMsg{chapter: c, err: err, reqID: id}
	}
	return tea.Batch(load, m.spinner.Tick)
}

func (m *Model) cmdReadCurrentContent() tea.Cmd {
	ctx, cancel, id := m.beginLoad(m.state.ChapterURL, 60*time.Second)
	load := func() tea.Msg {
		defer cancel()
		c, err := m.svc.ReloadCurrent(ctx)
		return chapterMsg{chapter: c, err: err, reqID: id}
	}
	return tea.Batch(load, m.spinner.Tick)
}

func (m *Model) cmdBooks() tea.Cmd {
	ctx, cancel, id := m.beginLoad("书架", 10*time.Second)
	load := func() tea.Msg {
		defer cancel()
		recs, err := m.svc.Bookshelf(ctx)
		return booksMsg{records: recs, err: err, reqID: id}
	}
	return tea.Batch(load, m.spinner.Tick)
}

func (m *Model) cmdExport(path string) tea.Cmd {
	ctx, cancel, id := m.beginLoad("导出: "+path, 5*time.Minute)
	load := func() tea.Msg {
		defer cancel()
		stats, err := repo.ExportDatabase(ctx, path)
		return backupMsg{action: "export", path: path, stats: stats, err: err, reqID: id}
	}
	return tea.Batch(load, m.spinner.Tick)
}

func (m *Model) cmdImport(path string) tea.Cmd {
	ctx, cancel, id := m.beginLoad("导入: "+path, 5*time.Minute)
	load := func() tea.Msg {
		defer cancel()
		stats, err := repo.ImportDatabase(ctx, path)
		return backupMsg{action: "import", path: path, stats: stats, err: err, reqID: id}
	}
	return tea.Batch(load, m.spinner.Tick)
}

// renderLoading paints the current loading indicator into the viewport so the
// user sees the spinner in the main content area while a request is in flight.
func (m *Model) renderLoading() {
	head, headLines := m.viewportHeader()
	m.viewport.SetContent(head + m.wrap(m.spinner.View()+" 加载中: "+m.loadURL))
	m.lineLinks = make([]string, headLines)
	m.viewport.GotoTop()
}

func (m *Model) renderBackupResult(msg backupMsg) {
	title := "备份"
	if msg.action == "export" {
		title = "导出"
	}
	if msg.action == "import" {
		title = "导入"
	}
	if msg.err != nil {
		m.renderMessage(title, "错误: "+msg.err.Error())
		return
	}

	verb := "导出"
	if msg.action == "import" {
		verb = "导入"
	}
	body := fmt.Sprintf("%s完成: %s\n\nrecords: %d\nbook_sources: %d\nllm_configs: %d",
		verb,
		msg.path,
		msg.stats.Records,
		msg.stats.BookSources,
		msg.stats.LLMConfigs,
	)
	m.renderMessage(title, body)
}

func (m *Model) renderMessage(title, body string) {
	m.mode = modeContent
	m.state = &model.ChapterContent{ChapterName: title}
	m.lineLinks = nil
	head, headLines := m.viewportHeader()
	wrapped := m.wrap(body)
	m.lineLinks = make([]string, headLines)
	if wrapped != "" {
		m.lineLinks = append(m.lineLinks, make([]string, strings.Count(wrapped, "\n")+1)...)
	}
	m.viewport.SetContent(head + wrapped)
	m.viewport.GotoTop()
}

// resizeViewport updates the viewport dimensions based on the current window
// size and whether the bottom input bar is shown.
func (m *Model) resizeViewport() {
	reserve := 0
	if m.showInput {
		reserve = bottomReserve
	}
	vpH := m.height - reserve
	if vpH < 3 {
		vpH = 3
	}
	m.viewport.Width = m.width
	m.viewport.Height = vpH
}

// viewportHeader returns the chapter-title block (title + blank separator)
// that is prepended to the viewport content, along with the number of
// lineLinks slots it occupies so click maps stay aligned.
func (m *Model) viewportHeader() (string, int) {
	title := m.state.ChapterName
	if title == "" {
		title = "Reader"
	}
	wrapped := m.wrap(title)
	lines := strings.Count(wrapped, "\n") + 1
	return wrapped + "\n\n", lines + 1
}

// renderContent loads the current chapter into the viewport, recording the
// lines occupied by the "下一章: <url>" block as clickable.
func (m *Model) renderContent() {
	wrapped := m.wrap(m.state.Content)
	contentLines := 0
	if wrapped != "" {
		contentLines = strings.Count(wrapped, "\n") + 1
	}

	m.lineLinks = make([]string, contentLines)

	final := wrapped
	if m.state.NextURL != "" {
		nextLine := "下一章: " + m.state.NextURL
		wrappedNext := m.wrap(nextLine)
		nextCount := strings.Count(wrappedNext, "\n") + 1

		sep := "\n\n"
		blankLines := 1
		if final == "" {
			sep = ""
			blankLines = 0
		}
		for i := 0; i < blankLines; i++ {
			m.lineLinks = append(m.lineLinks, "")
		}
		for i := 0; i < nextCount; i++ {
			m.lineLinks = append(m.lineLinks, m.state.NextURL)
		}
		final = final + sep + wrappedNext
	}

	head, headLines := m.viewportHeader()
	m.lineLinks = append(make([]string, headLines), m.lineLinks...)
	m.viewport.SetContent(head + final)
	m.viewport.GotoTop()
}

// renderBooks lays out the bookshelf from m.books, underlining the
// keyboard-selected row and recording the URL for each displayed row.
func (m *Model) renderBooks() {
	m.mode = modeBooks
	m.state = &model.ChapterContent{ChapterName: "书架"}

	var b strings.Builder
	header := lipgloss.NewStyle().Bold(true).Render(
		fmt.Sprintf("%-24s  %-30s  %s", "Book", "Chapter", "Time"),
	)
	b.WriteString(header + "\n")
	b.WriteString(strings.Repeat("─", max(1, m.width-1)) + "\n")

	// Two non-clickable lines so far (header + separator).
	m.lineLinks = make([]string, 0, len(m.books)+2)
	m.lineLinks = append(m.lineLinks, "", "")

	for i, r := range m.books {
		line := fmt.Sprintf("%-24s  %-30s  %s",
			truncate(r.BookName, 24),
			truncate(r.ChapterName, 30),
			r.UpdateTime.Local().Format(time.DateTime))
		if i == m.bookSel {
			line = selectedBookStyle.Render(line)
		}
		b.WriteString(line + "\n")
		m.lineLinks = append(m.lineLinks, r.ChapterURL)
	}

	head, headLines := m.viewportHeader()
	m.lineLinks = append(make([]string, headLines), m.lineLinks...)
	m.viewport.SetContent(head + b.String())
}

// ensureBookVisible scrolls the viewport so the keyboard-selected bookshelf
// row stays within view as the selection moves.
func (m *Model) ensureBookVisible() {
	if len(m.books) == 0 {
		return
	}
	// Header table row + separator occupy two lines before the first record.
	_, headLines := m.viewportHeader()
	selLine := headLines + 2 + m.bookSel
	top := m.viewport.YOffset
	bottom := top + m.viewport.Height - 1
	switch {
	case selLine < top:
		m.viewport.SetYOffset(selLine)
	case selLine > bottom:
		m.viewport.SetYOffset(selLine - m.viewport.Height + 1)
	}
}

// urlAt maps an absolute screen Y (0-based from the top of the program) to a
// clickable URL recorded in lineLinks, or "" if the click hits an inert line.
func (m *Model) urlAt(screenY int) string {
	if screenY < 0 || screenY >= m.viewport.Height {
		return ""
	}
	contentLine := screenY + m.viewport.YOffset
	if contentLine < 0 || contentLine >= len(m.lineLinks) {
		return ""
	}
	return m.lineLinks[contentLine]
}

var (
	inputStyle        = lipgloss.NewStyle().Border(lipgloss.NormalBorder()).Padding(0, 1)
	hintStyle         = lipgloss.NewStyle().Faint(true)
	selectedBookStyle = lipgloss.NewStyle().Underline(true)
)

func (m *Model) View() string {
	var b strings.Builder
	b.WriteString(m.viewport.View())
	if m.showInput {
		b.WriteString("\n" + inputStyle.Render("> "+m.input) + "\n")
		b.WriteString(hintStyle.Render("/help 显示帮助"))
	}
	return b.String()
}

// wrap soft-wraps text to the viewport width, preserving ANSI escapes and
// handling wide (CJK) runes correctly.
func (m *Model) wrap(s string) string {
	w := m.viewport.Width
	if w <= 0 {
		return s
	}
	return ansi.Wrap(s, w, "")
}

func truncate(s string, n int) string {
	r := []rune(s)
	if len(r) <= n {
		return s
	}
	if n <= 1 {
		return string(r[:n])
	}
	return string(r[:n-1]) + "…"
}

func commandPath(cmd, prefix string) string {
	path := strings.TrimSpace(strings.TrimPrefix(cmd, prefix))
	return strings.Trim(path, `"'`)
}
