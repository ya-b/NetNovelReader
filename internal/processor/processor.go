package processor

import (
	"context"
	"strings"
	"time"

	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/model"
	"github.com/go-reader/reader/internal/parser"
	"github.com/go-reader/reader/internal/postprocess"
	"github.com/go-reader/reader/internal/repo"
	"github.com/go-reader/reader/internal/webdriver"
)

// Processor orchestrates the webdriver, parser engine, and record persistence.
type Processor struct {
	Driver webdriver.Driver
	Engine *parser.Engine
}

func New(driverName string) *Processor {
	return &Processor{
		Driver: webdriver.New(driverName),
		Engine: parser.NewEngine(),
	}
}

// ReadURLContent loads a URL, parses it, and persists a record.
func (p *Processor) ReadURLContent(ctx context.Context, url string) (*model.ChapterContent, error) {
	p.Driver.ExecuteScript(ctx, "document.title = 'loading'")
	src, err := p.loadURL(ctx, url)
	logger.Log.Infof("load url %s: %v", url, err)
	if err != nil {
		return nil, err
	}
	p.Driver.ExecuteScript(ctx, "document.title2 = document.title")
	p.Driver.ExecuteScript(ctx, "document.title = 'k8s获取pod日志'")
	return p.ReadContent(ctx, src)
}

// ReadContent parses an already-fetched page source (or fetches from driver).
func (p *Processor) ReadContent(ctx context.Context, pageSource string) (*model.ChapterContent, error) {
	if pageSource == "" {
		p.Driver.ExecuteScript(ctx, "document.title = document.title2 ? document.title2 : document.title")
		s, err := p.Driver.PageSource(ctx)
		p.Driver.ExecuteScript(ctx, "document.title2 = document.title")
		p.Driver.ExecuteScript(ctx, "document.title = 'k8s获取pod日志'")
		if err != nil {
			return nil, err
		}
		pageSource = s
	}
	current, _ := p.Driver.CurrentURL(ctx)
	chapter := p.Engine.ParseContent(ctx, current, pageSource)
	chapter = p.postProcess(chapter)
	chapter.ChapterURL = current

	err := repo.UpdateRecord(ctx, chapter)
	logger.Log.Infof("update record %s %s: %v", chapter.BookName, chapter.ChapterName, err)
	return chapter, nil
}

func (p *Processor) loadURL(ctx context.Context, url string) (string, error) {
	ctx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()
	if err := p.Driver.Get(ctx, url); err != nil {
		return "", err
	}
	return p.Driver.PageSource(ctx)
}

func (p *Processor) postProcess(c *model.ChapterContent) *model.ChapterContent {
	c.BookName = postprocess.ToSimplified(c.BookName)
	c.ChapterName = postprocess.ToSimplified(c.ChapterName)

	if c.Content != "" {
		for strings.Contains(c.Content, "\n\n") {
			c.Content = strings.ReplaceAll(c.Content, "\n\n", "\n")
		}
		c.Content = strings.ReplaceAll(c.Content, "\n", "\n\n")
	}
	if config.Get().UIType == "tui" && c.Content != "" {
		var out []string
		for _, line := range strings.Split(c.Content, "\n") {
			out = append(out, postprocess.GenLog()+line)
		}
		c.Content = strings.Join(out, "\n")
	}
	return c
}
