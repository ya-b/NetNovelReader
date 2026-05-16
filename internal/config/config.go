package config

import (
	"os"
	"path/filepath"
	"sync"

	"github.com/caarlos0/env/v11"
	"github.com/go-reader/reader/internal/logger"
	"github.com/joho/godotenv"
)

type Settings struct {
	UIType         string `env:"UI_TYPE" envDefault:"tui"`
	WebUIToken     string `env:"WEBUI_TOKEN" envDefault:""`
	WebUIPort      string `env:"WEBUI_PORT" envDefault:"56789"`
	ChromeDriver   string `env:"CHROME_DRIVER" envDefault:"none"`
	ChromeVersion  int    `env:"CHROME_VERSION" envDefault:"147"`
	ChromeDataDir  string `env:"CHROME_DATA_DIR" envDefault:"./chrome-user-data"`
	DBURI          string `env:"DB_URI"`
}

var (
	once     sync.Once
	settings *Settings
)

func Get() *Settings {
	once.Do(func() {
		if exe, err := os.Executable(); err == nil {
			_ = godotenv.Load(filepath.Join(filepath.Dir(exe), ".env"))
		}
		s := &Settings{}
		if err := env.Parse(s); err != nil {
			logger.Log.Fatalf("parse env: %v", err)
		}
		settings = s
	})
	return settings
}
