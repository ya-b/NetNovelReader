package repo

import (
	"context"
	"time"

	"github.com/go-reader/reader/internal/model"
	"gorm.io/gorm/clause"
)

// GetAllRecords returns all reading records ordered by the given clause.
func GetAllRecords(ctx context.Context, orderBy string) ([]model.Record, error) {
	g, err := DB()
	if err != nil {
		return nil, err
	}
	if orderBy == "" {
		orderBy = "id ASC"
	}
	var records []model.Record
	if err := g.WithContext(ctx).Order(orderBy).Find(&records).Error; err != nil {
		return nil, err
	}
	return records, nil
}

// UpdateRecord upserts a reading record keyed by book_name.
func DeleteRecord(ctx context.Context, id int64) error {
	g, err := DB()
	if err != nil {
		return err
	}
	return g.WithContext(ctx).Delete(&model.Record{}, id).Error
}

func UpdateRecord(ctx context.Context, c *model.ChapterContent) error {
	if c.BookName == "" || c.ChapterURL == "" {
		return nil
	}
	g, err := DB()
	if err != nil {
		return err
	}
	var existing model.Record
	err = g.WithContext(ctx).Where("book_name = ?", c.BookName).First(&existing).Error
	if err == nil {
		existing.UpdateTime = time.Now()
		existing.ChapterName = c.ChapterName
		existing.ChapterURL = c.ChapterURL
		return g.WithContext(ctx).Save(&existing).Error
	}
	return g.WithContext(ctx).Clauses(clause.OnConflict{
		Columns:   []clause.Column{{Name: "book_name"}},
		DoUpdates: clause.AssignmentColumns([]string{"chapter_name", "chapter_url", "update_time"}),
	}).Create(&model.Record{
		BookName:    c.BookName,
		ChapterName: c.ChapterName,
		ChapterURL:  c.ChapterURL,
	}).Error
}
