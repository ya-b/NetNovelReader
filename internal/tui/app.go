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
	"github.com/go-reader/reader/internal/repo"
)

type chapterMsg struct {
	chapter *model.ChapterContent
	err     error
}

type booksMsg struct {
	records []model.Record
	err     error
}

type mode int

const (
	modeContent mode = iota
	modeBooks
)

// Model is the Bubble Tea model.
type Model struct {
	proc     *processor.Processor
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

	m := &Model{
		proc:      proc,
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
		case tea.KeyPgUp, tea.KeyPgDown, tea.KeyUp, tea.KeyDown, tea.KeyHome, tea.KeyEnd:
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
		m.loading = false
		m.loadURL = ""
		if msg.err != nil {
			m.lineLinks = nil
			m.viewport.SetContent(m.wrap("错误: " + msg.err.Error()))
			m.viewport.GotoTop()
			return m, nil
		}
		m.renderBooks(msg.records)
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

func (m *Model) handleEnter() (tea.Model, tea.Cmd) {
	cmd := strings.TrimSpace(m.input)
	m.input = ""
	switch {
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
	case strings.HasPrefix(cmd, "http"):
		return m, m.cmdOpenURL(cmd)
	}
	return m, nil
}

func (m *Model) cmdOpenURL(url string) tea.Cmd {
	m.loading = true
	m.loadURL = url
	m.lineLinks = nil
	m.renderLoading()
	load := func() tea.Msg {
		ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
		defer cancel()
		c, err := m.proc.ReadURLContent(ctx, url)
		return chapterMsg{chapter: c, err: err}
	}
	return tea.Batch(load, m.spinner.Tick)
}

func (m *Model) cmdReadCurrentContent() tea.Cmd {
	m.loading = true
	m.loadURL = m.state.ChapterURL
	m.lineLinks = nil
	m.renderLoading()
	load := func() tea.Msg {
		ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)
		defer cancel()
		c, err := m.proc.ReadContent(ctx, "")
		return chapterMsg{chapter: c, err: err}
	}
	return tea.Batch(load, m.spinner.Tick)
}

func (m *Model) cmdBooks() tea.Cmd {
	m.loading = true
	m.loadURL = "书架"
	m.lineLinks = nil
	m.renderLoading()
	load := func() tea.Msg {
		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
		defer cancel()
		recs, err := repo.GetAllRecords(ctx, "update_time DESC")
		return booksMsg{records: recs, err: err}
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

// renderBooks lays out the bookshelf, recording the URL for each displayed row.
func (m *Model) renderBooks(records []model.Record) {
	m.mode = modeBooks
	m.state = &model.ChapterContent{ChapterName: "书架"}

	var b strings.Builder
	header := lipgloss.NewStyle().Bold(true).Render(
		fmt.Sprintf("%-24s  %-30s  %s", "Book", "Chapter", "Time"),
	)
	b.WriteString(header + "\n")
	b.WriteString(strings.Repeat("─", max(1, m.width-1)) + "\n")

	// Two non-clickable lines so far (header + separator).
	m.lineLinks = make([]string, 0, len(records)+2)
	m.lineLinks = append(m.lineLinks, "", "")

	for _, r := range records {
		line := fmt.Sprintf("%-24s  %-30s  %s",
			truncate(r.BookName, 24),
			truncate(r.ChapterName, 30),
			r.UpdateTime.Local().Format(time.DateTime))
		b.WriteString(line + "\n")
		m.lineLinks = append(m.lineLinks, r.ChapterURL)
	}

	head, headLines := m.viewportHeader()
	m.lineLinks = append(make([]string, headLines), m.lineLinks...)
	m.viewport.SetContent(head + b.String())
	m.viewport.GotoTop()
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
	inputStyle = lipgloss.NewStyle().Border(lipgloss.NormalBorder()).Padding(0, 1)
	hintStyle  = lipgloss.NewStyle().Faint(true)
)

func (m *Model) View() string {
	var b strings.Builder
	b.WriteString(m.viewport.View())
	if m.showInput {
		b.WriteString("\n" + inputStyle.Render("> "+m.input) + "\n")
		b.WriteString(hintStyle.Render("/open <url> /next /refresh /reload /books /exit  |  鼠标滚轮滚动，点击书架行或“下一章”URL 打开，Esc 退出"))
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
