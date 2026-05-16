package repo

import (
	"bytes"
	"encoding/csv"
	"fmt"
	"io"
	"strings"
	"sync"

	reader "github.com/go-reader/reader"
	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/model"
	"gorm.io/driver/mysql"
	"gorm.io/driver/postgres"
	"gorm.io/driver/sqlite"
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
		uri := config.Get().DBURI
		if uri == "" {
			dbErr = fmt.Errorf("DB_URI is not set")
			return
		}
		dialector, err := dialectorForURI(uri)
		if err != nil {
			dbErr = err
			return
		}
		g, err := gorm.Open(dialector, &gorm.Config{
			Logger: gormlogger.New(logger.Log, gormlogger.Config{
				LogLevel: gormlogger.Warn,
			}),
		})
		if err != nil {
			dbErr = fmt.Errorf("open database: %w", err)
			return
		}
		db = g
	})
	return db, dbErr
}

func dialectorForURI(uri string) (gorm.Dialector, error) {
	idx := strings.Index(uri, "://")
	if idx < 0 {
		return nil, fmt.Errorf("unsupported DB_URI format")
	}
	scheme := strings.ToLower(uri[:idx])
	body := uri[idx+3:]

	switch scheme {
	case "postgres", "postgresql":
		return postgres.Open(uri), nil
	case "mysql":
		return mysql.Open(body), nil
	case "sqlite", "sqlite3":
		dsn := sqliteDSN(body)
		return sqlite.Open(dsn), nil
	default:
		return nil, fmt.Errorf("unsupported DB_URI scheme %q", scheme)
	}
}

func sqliteDSN(dsn string) string {
	if dsn == "" {
		return ":memory:"
	}
	if len(dsn) >= 3 && dsn[0] == '/' && dsn[2] == ':' && isAlpha(dsn[1]) {
		dsn = dsn[1:]
	}
	return dsn
}

func isAlpha(b byte) bool {
	return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z')
}

func Migrate() error {
	g, err := DB()
	if err != nil {
		return err
	}
	if err := g.AutoMigrate(&model.Record{}, &model.BookSource{}, &model.LLMConfig{}); err != nil {
		return err
	}
	return seedBookSources(g)
}

func seedBookSources(g *gorm.DB) error {
	sources, err := parseBookSourcesCSV(reader.BookSourcesCSV)
	if err != nil {
		return err
	}
	for _, src := range sources {
		var count int64
		if err := g.Model(&model.BookSource{}).
			Where("bookSourceUrl = ?", src.BookSourceURL).
			Count(&count).Error; err != nil {
			return err
		}
		if count > 0 {
			continue
		}
		if err := g.Create(&src).Error; err != nil {
			return err
		}
	}
	return nil
}

func parseBookSourcesCSV(data []byte) ([]model.BookSource, error) {
	r := csv.NewReader(bytes.NewReader(data))
	header, err := r.Read()
	if err != nil {
		return nil, fmt.Errorf("read book sources csv header: %w", err)
	}
	if len(header) != 8 {
		return nil, fmt.Errorf("book sources csv: expected 8 columns, got %d", len(header))
	}

	var sources []model.BookSource
	for {
		record, err := r.Read()
		if err != nil {
			if err == io.EOF {
				break
			}
			return nil, fmt.Errorf("read book sources csv row: %w", err)
		}
		if len(record) != 8 {
			return nil, fmt.Errorf("book sources csv: expected 8 columns, got %d", len(record))
		}
		sources = append(sources, model.BookSource{
			BookSourceName:     record[1],
			BookSourceURL:      record[2],
			BookNameRule:       record[3],
			ChapterNameRule:    record[4],
			ContentRule:        record[5],
			NextContentURLRule: record[6],
			Enabled:            strings.EqualFold(record[7], "true"),
		})
	}
	return sources, nil
}
