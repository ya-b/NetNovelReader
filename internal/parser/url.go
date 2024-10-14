package parser

import (
	"net/url"
	"strings"
)

// matchScore mirrors py-reader's ParserEngine._match_score.
func matchScore(pageURL, sourceURL string) int {
	if sourceURL == "" {
		return -1
	}
	page, err1 := url.Parse(pageURL)
	src, err2 := url.Parse(sourceURL)
	if err1 != nil || err2 != nil {
		return -1
	}
	if src.Host == "" || page.Host != src.Host {
		return -1
	}
	srcPath := strings.TrimRight(src.Path, "/")
	pagePath := strings.TrimRight(page.Path, "/")
	if srcPath == "" {
		return len(src.Host)
	}
	if pagePath == srcPath || strings.HasPrefix(pagePath, srcPath+"/") {
		return len(src.Host) + len(srcPath)
	}
	return -1
}

// normalizeURL resolves a possibly relative URL against base/page URLs,
// stripping trailing Legado-style ",{...}" option markers.
func normalizeURL(value, pageURL, baseURL string) string {
	value = strings.TrimSpace(stripURLOptions(value))
	if value == "" {
		return ""
	}
	if strings.HasPrefix(value, "http://") || strings.HasPrefix(value, "https://") {
		return value
	}
	if strings.HasPrefix(value, "//") {
		scheme := "http:"
		if strings.HasPrefix(pageURL, "https://") {
			scheme = "https:"
		}
		return scheme + value
	}
	ref := baseURL
	if ref == "" {
		ref = pageURL
	}
	base, err := url.Parse(ref)
	if err != nil {
		return value
	}
	rel, err := url.Parse(value)
	if err != nil {
		return value
	}
	return base.ResolveReference(rel).String()
}

func stripURLOptions(v string) string {
	const marker = ",{\""
	if i := strings.Index(v, marker); i >= 0 {
		return v[:i]
	}
	return v
}
