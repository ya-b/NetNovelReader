package webdriver

import "context"

// Driver abstracts a page fetcher with optional browser-like scripting.
type Driver interface {
	Start(ctx context.Context) error
	Stop(ctx context.Context) error
	Get(ctx context.Context, url string) error
	CurrentURL(ctx context.Context) (string, error)
	PageSource(ctx context.Context) (string, error)
	Title(ctx context.Context) (string, error)
	ExecuteScript(ctx context.Context, script string) (string, error)
}
