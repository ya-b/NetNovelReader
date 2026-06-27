package processor

import (
	"context"
	"strings"
	"sync"
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
	Driver  webdriver.Driver
	Engine  *parser.Engine
	cache   *lruCache
	fetchMu sync.Mutex // serializes Driver.Get + Driver.PageSource sequences
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
	src, err := p.loadURL(ctx, url)
	logger.Log.Infof("load url %s: %v", url, err)
	if err != nil {
		return nil, err
	}

	chapter := p.Engine.ParseContent(ctx, url, src)
	chapter = p.postProcess(chapter)
	chapter.ChapterURL = url

	err = repo.UpdateRecord(ctx, chapter)
	logger.Log.Infof("update record %s %s: %v", chapter.BookName, chapter.ChapterName, err)
	return chapter, nil
}

// Prefetch fetches the URL in the background to warm the LRU cache.
// When the user later navigates to this URL, loadURL will get a cache hit
// and skip the network request.
func (p *Processor) Prefetch(ctx context.Context, url string) {
	_, _ = p.loadURL(ctx, url)
}

func (p *Processor) loadURL(ctx context.Context, url string) (string, error) {
	if val, ok := p.cache.Get(url); ok {
		logger.Log.Infof("cache hit for url %s", url)
		return val, nil
	}

	p.fetchMu.Lock()
	defer p.fetchMu.Unlock()

	// Double-check after acquiring the lock.
	if val, ok := p.cache.Get(url); ok {
		logger.Log.Infof("cache hit for url %s", url)
		return val, nil
	}
	logger.Log.Infof("cache miss for url %s", url)

	ctx, cancel := context.WithTimeout(ctx, 60*time.Second)
	defer cancel()

	p.Driver.ExecuteScript(ctx, "document.title = 'loading'")
	if err := p.Driver.Get(ctx, url); err != nil {
		return "", err
	}
	src, err := p.Driver.PageSource(ctx)
	if err != nil {
		return "", err
	}
	p.cache.Add(url, src)
	p.Driver.ExecuteScript(ctx, "document.title = 'test'")
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
