package main

import (
	"context"
	"os"
	"os/signal"
	"syscall"

	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/tui"
	"github.com/go-reader/reader/internal/web"
)

func main() {
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	if f := logger.Init(config.Get().UIType); f != nil {
		defer f.Close()
	}

	switch config.Get().UIType {
	case "webui":
		if err := web.Run(ctx, ":"+config.Get().WebUIPort); err != nil {
			logger.Log.Fatalf("webui: %v", err)
		}
	case "tui":
		if err := tui.Run(ctx); err != nil {
			logger.Log.Fatalf("tui: %v", err)
		}
	default:
		logger.Log.Fatalf("unknown UI_TYPE: %s", config.Get().UIType)
	}
}
