package web

import (
	"embed"
	"io/fs"
)

//go:embed all:templates
var templatesFS embed.FS

// Templates returns the embedded templates directory rooted at "templates".
func Templates() fs.FS {
	sub, err := fs.Sub(templatesFS, "templates")
	if err != nil {
		return templatesFS
	}
	return sub
}
