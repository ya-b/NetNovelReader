package repo

import (
	"context"
	"os"
	"path/filepath"
	"sync"
	"testing"

	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/model"
	"gorm.io/gorm"
)

func TestExportImportDatabase(t *testing.T) {
	ctx := setupBackupTestDB(t)
	g, err := DB()
	if err != nil {
		t.Fatalf("db: %v", err)
	}

	if err := UpdateRecord(ctx, &model.ChapterContent{
		BookName:    "测试书",
		ChapterName: "第一章",
		ChapterURL:  "https://example.com/book/1",
	}); err != nil {
		t.Fatalf("create record: %v", err)
	}
	src := &model.BookSource{
		BookSourceName:     "测试导出源",
		BookSourceURL:      "https://backup.example.com",
		BookNameRule:       "@css:title@text",
		ChapterNameRule:    "@css:h1@text",
		ContentRule:        "@css:.content@text",
		NextContentURLRule: "@css:a.next@href",
		Enabled:            true,
	}
	if err := CreateBookSource(ctx, src); err != nil {
		t.Fatalf("create source: %v", err)
	}
	if err := g.WithContext(ctx).Create(&model.LLMConfig{
		BaseURL:   "https://llm.example.com",
		APIKey:    "secret",
		Model:     "reader-test",
		ExtraBody: `{"temperature":0}`,
		Enabled:   true,
	}).Error; err != nil {
		t.Fatalf("create llm config: %v", err)
	}

	path := filepath.Join(t.TempDir(), "backup.json")
	exportStats, err := ExportDatabase(ctx, path)
	if err != nil {
		t.Fatalf("export: %v", err)
	}
	if exportStats.Records != 1 || exportStats.LLMConfigs != 1 || exportStats.BookSources < 1 {
		t.Fatalf("unexpected export stats: %+v", exportStats)
	}

	if err := UpdateRecord(ctx, &model.ChapterContent{
		BookName:    "导出后新增",
		ChapterName: "第二章",
		ChapterURL:  "https://example.com/book/2",
	}); err != nil {
		t.Fatalf("create extra record: %v", err)
	}
	if err := CreateBookSource(ctx, &model.BookSource{
		BookSourceName: "导出后新增源",
		BookSourceURL:  "https://extra.example.com",
		Enabled:        true,
	}); err != nil {
		t.Fatalf("create extra source: %v", err)
	}

	importStats, err := ImportDatabase(ctx, path)
	if err != nil {
		t.Fatalf("import: %v", err)
	}
	if importStats != exportStats {
		t.Fatalf("import stats = %+v, want %+v", importStats, exportStats)
	}

	records, err := GetAllRecords(ctx, "book_name ASC")
	if err != nil {
		t.Fatalf("records: %v", err)
	}
	if len(records) != 1 || records[0].BookName != "测试书" {
		t.Fatalf("records not restored: %+v", records)
	}

	var extraSourceCount int64
	if err := g.WithContext(ctx).Model(&model.BookSource{}).
		Where(&model.BookSource{BookSourceURL: "https://extra.example.com"}).
		Count(&extraSourceCount).Error; err != nil {
		t.Fatalf("count extra source: %v", err)
	}
	if extraSourceCount != 0 {
		t.Fatalf("extra source survived import")
	}

	var llm model.LLMConfig
	if err := g.WithContext(ctx).First(&llm).Error; err != nil {
		t.Fatalf("llm config: %v", err)
	}
	if llm.APIKey != "secret" || !llm.Enabled {
		t.Fatalf("llm config not restored: %+v", llm)
	}
}

func TestImportInvalidJSONDoesNotOverwrite(t *testing.T) {
	ctx := setupBackupTestDB(t)
	if err := UpdateRecord(ctx, &model.ChapterContent{
		BookName:    "保留书",
		ChapterName: "第一章",
		ChapterURL:  "https://example.com/keep",
	}); err != nil {
		t.Fatalf("create record: %v", err)
	}

	path := filepath.Join(t.TempDir(), "broken.json")
	if err := os.WriteFile(path, []byte("{broken"), 0o644); err != nil {
		t.Fatalf("write broken backup: %v", err)
	}
	if _, err := ImportDatabase(ctx, path); err == nil {
		t.Fatalf("import invalid json: expected error")
	}

	records, err := GetAllRecords(ctx, "")
	if err != nil {
		t.Fatalf("records: %v", err)
	}
	if len(records) != 1 || records[0].BookName != "保留书" {
		t.Fatalf("records were overwritten: %+v", records)
	}
}

func setupBackupTestDB(t *testing.T) context.Context {
	t.Helper()

	origDB, origErr, origOnce := db, dbErr, dbOnce
	t.Cleanup(func() {
		db, dbErr, dbOnce = origDB, origErr, origOnce
		config.ResetForTest()
	})

	t.Setenv("DB_URI", "sqlite://:memory:")
	config.ResetForTest()
	db, dbErr, dbOnce = nil, nil, sync.Once{}

	ctx := context.Background()
	if err := Migrate(); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	g, err := DB()
	if err != nil {
		t.Fatalf("db: %v", err)
	}
	if err := g.WithContext(ctx).Session(&gorm.Session{AllowGlobalUpdate: true}).
		Delete(&model.Record{}).Error; err != nil {
		t.Fatalf("clear records: %v", err)
	}
	if err := g.WithContext(ctx).Session(&gorm.Session{AllowGlobalUpdate: true}).
		Delete(&model.LLMConfig{}).Error; err != nil {
		t.Fatalf("clear llm configs: %v", err)
	}
	return ctx
}
