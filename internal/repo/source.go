package repo

import (
	"context"

	"github.com/go-reader/reader/internal/model"
)

// GetEnabledBookSources returns all book sources with enabled=true.
func GetEnabledBookSources(ctx context.Context) ([]model.BookSource, error) {
	g, err := DB()
	if err != nil {
		return nil, err
	}
	var sources []model.BookSource
	if err := g.WithContext(ctx).Where("enabled = ?", true).Find(&sources).Error; err != nil {
		return nil, err
	}
	return sources, nil
}

// GetEnabledLLMConfig returns the first enabled LLM config, or nil if none.
func GetEnabledLLMConfig(ctx context.Context) (*model.LLMConfig, error) {
	g, err := DB()
	if err != nil {
		return nil, err
	}
	var cfg model.LLMConfig
	err = g.WithContext(ctx).Where("enabled = ?", true).First(&cfg).Error
	if err != nil {
		return nil, nil
	}
	return &cfg, nil
}
