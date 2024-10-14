package web

import (
	"net/http"
	"strings"

	"github.com/go-reader/reader/internal/config"
)

var whitelist = map[string]struct{}{
	"/":                  {},
	"/api/v1/login":      {},
	"/ui/login.html":     {},
}

// AuthMiddleware enforces cookie-based auth when WEBUI_TOKEN is configured.
func AuthMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		token := config.Get().WebUIToken
		if token == "" {
			next.ServeHTTP(w, r)
			return
		}
		path := r.URL.Path
		if _, ok := whitelist[path]; ok ||
			strings.HasPrefix(path, "/ui/style") ||
			strings.HasPrefix(path, "/ui/sidebar") {
			next.ServeHTTP(w, r)
			return
		}
		c, err := r.Cookie(authCookieName)
		if err != nil || c.Value != token {
			if strings.HasPrefix(path, "/api/") {
				http.Error(w, "Unauthorized", http.StatusUnauthorized)
				return
			}
			http.Redirect(w, r, "/ui/login.html", http.StatusFound)
			return
		}
		next.ServeHTTP(w, r)
	})
}
