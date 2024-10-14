package webdriver

import (
	"context"
	"crypto/tls"
	"fmt"
	"io"
	"net/http"
	"sync"
	"time"
)

// NoneDriver fetches pages via plain HTTP with a browser-like User-Agent.
// It is a simplified stand-in for curl_cffi impersonation.
type NoneDriver struct {
	client     *http.Client
	mu         sync.Mutex
	currentURL string
	pageSource string
}

func NewNoneDriver() *NoneDriver {
	return &NoneDriver{
		client: &http.Client{
			Timeout: 30 * time.Second,
			Transport: &http.Transport{
				TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
			},
		},
	}
}

func (d *NoneDriver) Start(_ context.Context) error { return nil }
func (d *NoneDriver) Stop(_ context.Context) error  { return nil }

func (d *NoneDriver) Get(ctx context.Context, url string) error {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return err
	}
	req.Header.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36")
	req.Header.Set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
	req.Header.Set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")

	resp, err := d.client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return err
	}
	if resp.StatusCode >= 400 {
		return fmt.Errorf("http %d: %s", resp.StatusCode, url)
	}

	d.mu.Lock()
	d.currentURL = url
	d.pageSource = string(body)
	d.mu.Unlock()
	return nil
}

func (d *NoneDriver) CurrentURL(_ context.Context) (string, error) {
	d.mu.Lock()
	defer d.mu.Unlock()
	return d.currentURL, nil
}

func (d *NoneDriver) PageSource(_ context.Context) (string, error) {
	d.mu.Lock()
	defer d.mu.Unlock()
	return d.pageSource, nil
}

func (d *NoneDriver) Title(_ context.Context) (string, error) { return "", nil }

func (d *NoneDriver) ExecuteScript(_ context.Context, _ string) (string, error) {
	return "", nil
}
