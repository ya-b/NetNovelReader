package repo

import (
	"context"
	"errors"

	"github.com/go-reader/reader/internal/model"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
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

// GetAllBookSources returns all book sources ordered by name then id.
func GetAllBookSources(ctx context.Context) ([]model.BookSource, error) {
	g, err := DB()
	if err != nil {
		return nil, err
	}
	var sources []model.BookSource
	if err := g.WithContext(ctx).Clauses(clause.OrderBy{
		Columns: []clause.OrderByColumn{
			{Column: clause.Column{Name: "bookSourceName"}},
			{Column: clause.Column{Name: "id"}},
		},
	}).Find(&sources).Error; err != nil {
		return nil, err
	}
	return sources, nil
}

// GetBookSource returns a single book source by id.
func GetBookSource(ctx context.Context, id int64) (*model.BookSource, error) {
	g, err := DB()
	if err != nil {
		return nil, err
	}
	var src model.BookSource
	if err := g.WithContext(ctx).First(&src, id).Error; err != nil {
		return nil, err
	}
	return &src, nil
}

// CreateBookSource inserts a new book source.
func CreateBookSource(ctx context.Context, src *model.BookSource) error {
	g, err := DB()
	if err != nil {
		return err
	}
	return g.WithContext(ctx).Create(src).Error
}

// UpdateBookSource updates an existing book source by id.
func UpdateBookSource(ctx context.Context, src *model.BookSource) error {
	g, err := DB()
	if err != nil {
		return err
	}
	return g.WithContext(ctx).Save(src).Error
}

// DeleteBookSource deletes a book source by id.
func DeleteBookSource(ctx context.Context, id int64) error {
	g, err := DB()
	if err != nil {
		return err
	}
	return g.WithContext(ctx).Delete(&model.BookSource{}, id).Error
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
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &cfg, nil
}
