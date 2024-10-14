package webdriver

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"sync"
	"time"

	cu "github.com/Davincible/chromedp-undetected"
	"github.com/chromedp/chromedp"

	"github.com/go-reader/reader/internal/config"
)

const chromeDefaultTimeout = 30 * time.Second

// ChromeDriver drives a real Chrome instance over the Chrome DevTools Protocol
// using chromedp-undetected, which patches the CDP launch so that common
// anti-bot checks (navigator.webdriver, --enable-automation banner, test-type
// flag, etc.) don't trip on the default chromedp configuration.
type ChromeDriver struct {
	mu         sync.Mutex
	browserCtx context.Context
	cancel     context.CancelFunc
}

func NewChromeDriver() *ChromeDriver {
	return &ChromeDriver{}
}

func (d *ChromeDriver) Start(_ context.Context) error {
	d.mu.Lock()
	defer d.mu.Unlock()
	if d.browserCtx != nil {
		return nil
	}

	// NoSandbox=false avoids the --test-type flag that cu adds alongside
	// --no-sandbox, which itself is a detection signal on desktop OSes.
	// Headless is intentionally off: cu's headless mode requires Xvfb and is
	// Linux-only; on Windows we need a visible window anyway.
	cfg := cu.NewConfig(
		cu.WithUserDataDir(config.Get().ChromeDataDir),
		cu.WithNoSandbox(false),
	)

	ctx, cancel, err := cu.New(cfg)
	if err != nil {
		return fmt.Errorf("start chrome: %w", err)
	}

	// Force-launch so a misconfigured user data dir or missing Chrome surfaces
	// here instead of on the first page load.
	if err := chromedp.Run(ctx, chromedp.Navigate("about:blank")); err != nil {
		cancel()
		return fmt.Errorf("start chrome: %w", err)
	}

	d.browserCtx = ctx
	d.cancel = cancel
	return nil
}

func (d *ChromeDriver) Stop(_ context.Context) error {
	d.mu.Lock()
	defer d.mu.Unlock()
	if d.cancel != nil {
		d.cancel()
		d.cancel = nil
	}
	d.browserCtx = nil
	return nil
}

func (d *ChromeDriver) session() (context.Context, context.CancelFunc, error) {
	d.mu.Lock()
	bctx := d.browserCtx
	d.mu.Unlock()
	if bctx == nil {
		return nil, nil, errors.New("chrome driver not started")
	}
	ctx, cancel := context.WithTimeout(bctx, chromeDefaultTimeout)
	return ctx, cancel, nil
}

func (d *ChromeDriver) Get(_ context.Context, url string) error {
	ctx, cancel, err := d.session()
	if err != nil {
		return err
	}
	defer cancel()
	return chromedp.Run(ctx, chromedp.Navigate(url))
}

func (d *ChromeDriver) CurrentURL(_ context.Context) (string, error) {
	ctx, cancel, err := d.session()
	if err != nil {
		return "", err
	}
	defer cancel()
	var u string
	if err := chromedp.Run(ctx, chromedp.Location(&u)); err != nil {
		return "", err
	}
	return u, nil
}

func (d *ChromeDriver) PageSource(_ context.Context) (string, error) {
	ctx, cancel, err := d.session()
	if err != nil {
		return "", err
	}
	defer cancel()
	var html string
	if err := chromedp.Run(ctx, chromedp.OuterHTML("html", &html, chromedp.ByQuery)); err != nil {
		return "", err
	}
	return html, nil
}

func (d *ChromeDriver) Title(_ context.Context) (string, error) {
	ctx, cancel, err := d.session()
	if err != nil {
		return "", err
	}
	defer cancel()
	var title string
	if err := chromedp.Run(ctx, chromedp.Title(&title)); err != nil {
		return "", err
	}
	return title, nil
}

func (d *ChromeDriver) ExecuteScript(_ context.Context, script string) (string, error) {
	ctx, cancel, err := d.session()
	if err != nil {
		return "", err
	}
	defer cancel()
	var raw any
	if err := chromedp.Run(ctx, chromedp.Evaluate(script, &raw)); err != nil {
		return "", err
	}
	switch v := raw.(type) {
	case nil:
		return "", nil
	case string:
		return v, nil
	default:
		b, err := json.Marshal(v)
		if err != nil {
			return fmt.Sprintf("%v", v), nil
		}
		return string(b), nil
	}
}
