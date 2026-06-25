package reading

import (
	"context"

	"github.com/go-reader/reader/internal/model"
	"github.com/go-reader/reader/internal/postprocess"
	"github.com/go-reader/reader/internal/processor"
	"github.com/go-reader/reader/internal/repo"
)

// Service is the app-facing reading backend. Both the WebUI and TUI front-ends
// adapt to it: it opens a URL into a chapter, lists the bookshelf, and re-reads
// the current page. A disguised Service wraps opened chapter content in the
// screen-share log disguise.
type Service struct {
	proc     *processor.Processor
	disguise bool
}

// Option configures a Service at construction.
type Option func(*Service)

// Disguised makes Open and ReloadCurrent wrap chapter content in the fake-log
// disguise. The TUI constructs a disguised Service; the WebUI does not.
func Disguised() Option {
	return func(s *Service) { s.disguise = true }
}

func New(proc *processor.Processor, opts ...Option) *Service {
	s := &Service{proc: proc}
	for _, o := range opts {
		o(s)
	}
	return s
}

// Open fetches url, parses it into a chapter, upserts the reading record, and
// returns the chapter.
func (s *Service) Open(ctx context.Context, url string) (*model.ChapterContent, error) {
	c, err := s.proc.ReadURLContent(ctx, url)
	if err != nil {
		return nil, err
	}
	return s.applyDisguise(c), nil
}

// ReloadCurrent re-parses the driver's current page without re-fetching it.
func (s *Service) ReloadCurrent(ctx context.Context) (*model.ChapterContent, error) {
	c, err := s.proc.ReadContent(ctx, "")
	if err != nil {
		return nil, err
	}
	return s.applyDisguise(c), nil
}

// Bookshelf returns reading records, most-recently-opened first.
func (s *Service) Bookshelf(ctx context.Context) ([]model.Record, error) {
	return repo.GetAllRecords(ctx, "update_time DESC")
}

func (s *Service) applyDisguise(c *model.ChapterContent) *model.ChapterContent {
	if s.disguise {
		c.Content = postprocess.Disguise(c.Content)
	}
	return c
}
