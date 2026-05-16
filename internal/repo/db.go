package repo

import (
	"fmt"
	"strings"
	"sync"

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
	return g.AutoMigrate(&model.Record{}, &model.BookSource{}, &model.LLMConfig{})
}
