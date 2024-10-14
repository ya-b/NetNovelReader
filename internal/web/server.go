package web

import (
	"context"
	"net/http"

	"github.com/go-chi/chi/v5"
	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/processor"
	"github.com/go-reader/reader/internal/repo"
)

func Run(ctx context.Context, addr string) error {
	if err := repo.Migrate(); err != nil {
		logger.Log.Errorf("migrate: %v", err)
	}

	proc := processor.New(config.Get().ChromeDriver)
	if err := proc.Driver.Start(ctx); err != nil {
		logger.Log.Errorf("driver start: %v", err)
	}
	defer func() {
		if err := proc.Driver.Stop(context.Background()); err != nil {
			logger.Log.Errorf("driver stop: %v", err)
		}
	}()

	r := chi.NewRouter()
	r.Use(AuthMiddleware)

	r.Get("/", func(w http.ResponseWriter, req *http.Request) {
		http.Redirect(w, req, "/ui/index.html", http.StatusFound)
	})

	fileServer := http.StripPrefix("/ui/", http.FileServer(http.FS(Templates())))
	r.Handle("/ui/*", fileServer)

	r.Route("/api/v1", func(r chi.Router) {
		r.Post("/login", handleLogin)
		r.Get("/records", handleGetRecords)
		r.Delete("/records/{id}", handleDeleteRecord)
		r.Post("/read", handleReadURL(proc))
	})

	srv := &http.Server{Addr: addr, Handler: r}
	errCh := make(chan error, 1)
	go func() { errCh <- srv.ListenAndServe() }()

	logger.Log.Infof("WebUI listening on %s", addr)
	select {
	case <-ctx.Done():
		return srv.Shutdown(context.Background())
	case err := <-errCh:
		return err
	}
}
