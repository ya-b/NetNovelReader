package processor

import (
	"context"
	"strings"
	"time"

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
	cache  *lruCache
}

func New(driverName string) *Processor {
	return &Processor{
		Driver: webdriver.New(driverName),
		Engine: parser.NewEngine(),
		cache:  newLRUCache(100),
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
	if val, ok := p.cache.Get(url); ok {
		logger.Log.Infof("cache hit for url %s", url)
		return val, nil
	}
	logger.Log.Infof("cache miss for url %s", url)

	ctx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()
	if err := p.Driver.Get(ctx, url); err != nil {
		return "", err
	}
	src, err := p.Driver.PageSource(ctx)
	if err != nil {
		return "", err
	}
	p.cache.Add(url, src)
	return src, nil
}

// PreviewSource fetches url and runs the given (possibly unsaved) book source's
// rules against it, returning the extracted chapter. It skips engine
// source-matching, persists no record, and applies no post-processing — it is
// for book-source authoring (Preview), not reading.
func (p *Processor) PreviewSource(ctx context.Context, src *model.BookSource, url string) (*model.ChapterContent, error) {
	html, err := p.loadURL(ctx, url)
	if err != nil {
		return nil, err
	}
	c, err := parser.NewRuleParser().ParseContent(src, url, html)
	if err != nil {
		return nil, err
	}
	c.ChapterURL = url
	return c, nil
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
	c.Content = postprocess.ToSimplified(c.Content)
	return c
}
