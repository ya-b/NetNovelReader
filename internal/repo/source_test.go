package repo

import (
	"context"
	"os"
	"testing"

	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/model"
)

func TestBookSourceCRUD(t *testing.T) {
	origDB, origErr, origOnce := db, dbErr, dbOnce
	t.Cleanup(func() {
		db, dbErr, dbOnce = origDB, origErr, origOnce
		config.ResetForTest()
	})

	_ = os.Setenv("DB_URI", "sqlite://:memory:")
	config.ResetForTest()

	ctx := context.Background()
	if err := Migrate(); err != nil {
		t.Fatalf("migrate: %v", err)
	}

	src := &model.BookSource{
		BookSourceName:     "测试源",
		BookSourceURL:      "https://example.com",
		BookNameRule:       "@css:title@text",
		ChapterNameRule:    "@css:h1@text",
		ContentRule:        "@css:.content@text",
		NextContentURLRule: "@css:a.next@href",
		Enabled:            true,
	}
	if err := CreateBookSource(ctx, src); err != nil {
		t.Fatalf("create: %v", err)
	}
	if src.ID == 0 {
		t.Fatalf("create did not set id")
	}

	got, err := GetBookSource(ctx, src.ID)
	if err != nil {
		t.Fatalf("get: %v", err)
	}
	if got.BookSourceName != src.BookSourceName {
		t.Fatalf("got name %q, want %q", got.BookSourceName, src.BookSourceName)
	}

	got.ContentRule = "@css:.body@text"
	got.Enabled = false
	if err := UpdateBookSource(ctx, got); err != nil {
		t.Fatalf("update: %v", err)
	}

	got2, err := GetBookSource(ctx, src.ID)
	if err != nil {
		t.Fatalf("get updated: %v", err)
	}
	if got2.ContentRule != "@css:.body@text" || got2.Enabled {
		t.Fatalf("update not persisted: %+v", got2)
	}

	list, err := GetAllBookSources(ctx)
	if err != nil {
		t.Fatalf("list: %v", err)
	}
	found := false
	for _, item := range list {
		if item.ID == src.ID {
			found = true
			break
		}
	}
	if !found {
		t.Fatalf("created source missing from list: %+v", list)
	}

	if err := DeleteBookSource(ctx, src.ID); err != nil {
		t.Fatalf("delete: %v", err)
	}
}
