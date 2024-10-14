package parser

import (
	"context"

	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/model"
	"github.com/go-reader/reader/internal/repo"
)

// Engine routes URLs to either a rule-based parser or the default parser.
type Engine struct {
	rule    *RuleParser
	defualt *DefaultParser
}

func NewEngine() *Engine {
	return &Engine{rule: NewRuleParser(), defualt: NewDefaultParser()}
}

// ParseContent resolves the best matching book source, executes its rule, and
// falls back to the default parser on failure.
func (e *Engine) ParseContent(ctx context.Context, pageURL, html string) *model.ChapterContent {
	src, err := e.findSourceRule(ctx, pageURL)
	if err != nil {
		logger.Log.Errorf("find source rule: %v", err)
	}
	if src != nil {
		parsed, perr := e.rule.ParseContent(src, pageURL, html)
		if perr == nil && parsed != nil {
			return parsed
		}
	}

	llm, _ := repo.GetEnabledLLMConfig(ctx)
	return e.defualt.ParseContent(ctx, llm, pageURL, html)
}

func (e *Engine) findSourceRule(ctx context.Context, pageURL string) (*model.BookSource, error) {
	sources, err := repo.GetEnabledBookSources(ctx)
	if err != nil {
		return nil, err
	}
	var (
		best  *model.BookSource
		score = -1
	)
	for i := range sources {
		s := matchScore(pageURL, sources[i].BookSourceURL)
		if s > score {
			score = s
			best = &sources[i]
		}
	}
	if score < 0 {
		return nil, nil
	}
	return best, nil
}
