package repo

import (
	"fmt"
	"sync"

	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/model"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	gormlogger "gorm.io/gorm/logger"
)

var (
	dbOnce sync.Once
	db     *gorm.DB
	dbErr  error
)

func DB() (*gorm.DB, error) {
	dbOnce.Do(func() {
		uri := config.Get().PGURI
		if uri == "" {
			dbErr = fmt.Errorf("PG_URI is not set")
			return
		}
		g, err := gorm.Open(postgres.Open(uri), &gorm.Config{
			Logger: gormlogger.New(logger.Log, gormlogger.Config{
				LogLevel: gormlogger.Warn,
			}),
		})
		if err != nil {
			dbErr = fmt.Errorf("open postgres: %w", err)
			return
		}
		db = g
	})
	return db, dbErr
}

func Migrate() error {
	g, err := DB()
	if err != nil {
		return err
	}
	return g.AutoMigrate(&model.Record{}, &model.BookSource{}, &model.LLMConfig{})
}
