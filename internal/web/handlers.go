package web

import (
	"encoding/json"
	"io"
	"net/http"
	"strconv"
	"time"

	"github.com/go-chi/chi/v5"
	"github.com/go-reader/reader/internal/config"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/model"
	"github.com/go-reader/reader/internal/processor"
	"github.com/go-reader/reader/internal/reading"
	"github.com/go-reader/reader/internal/repo"
)

const authCookieName = "auth_token"

type readURLRequest struct {
	URL string `json:"url"`
}

type recordResponse struct {
	ID          int64  `json:"id"`
	BookName    string `json:"book_name"`
	ChapterName string `json:"chapter_name"`
	ChapterURL  string `json:"chapter_url"`
	UpdateTime  string `json:"update_time"`
}

type bookSourceResponse struct {
	ID                 int64  `json:"id"`
	BookSourceName     string `json:"book_source_name"`
	BookSourceURL      string `json:"book_source_url"`
	BookNameRule       string `json:"book_name_rule"`
	ChapterNameRule    string `json:"chapter_name_rule"`
	ContentRule        string `json:"content_rule"`
	NextContentURLRule string `json:"next_content_url_rule"`
	Enabled            bool   `json:"enabled"`
}

type bookSourceRequest struct {
	BookSourceName     string `json:"book_source_name"`
	BookSourceURL      string `json:"book_source_url"`
	BookNameRule       string `json:"book_name_rule"`
	ChapterNameRule    string `json:"chapter_name_rule"`
	ContentRule        string `json:"content_rule"`
	NextContentURLRule string `json:"next_content_url_rule"`
	Enabled            bool   `json:"enabled"`
}

type bookSourcePreviewRequest struct {
	Source bookSourceRequest `json:"source"`
	URL    string            `json:"url"`
}

type loginRequest struct {
	Token string `json:"token"`
}

func writeJSON(w http.ResponseWriter, status int, v any) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(v)
}

func writeError(w http.ResponseWriter, status int, msg string) {
	writeJSON(w, status, map[string]string{"detail": msg})
}

// handleLogin validates the configured WEBUI_TOKEN and sets an auth cookie.
func handleLogin(w http.ResponseWriter, r *http.Request) {
	token := config.Get().WebUIToken
	if token == "" {
		writeError(w, http.StatusForbidden, "Token not configured")
		return
	}
	var req loginRequest
	body, _ := io.ReadAll(r.Body)
	if err := json.Unmarshal(body, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid body")
		return
	}
	if req.Token != token {
		writeError(w, http.StatusUnauthorized, "Token incorrect")
		return
	}
	http.SetCookie(w, &http.Cookie{
		Name:     authCookieName,
		Value:    token,
		Path:     "/",
		HttpOnly: true,
		MaxAge:   86400 * 30,
		SameSite: http.SameSiteLaxMode,
	})
	w.WriteHeader(http.StatusOK)
}

func handleGetRecords(svc *reading.Service) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		records, err := svc.Bookshelf(r.Context())
		if err != nil {
			writeError(w, http.StatusInternalServerError, err.Error())
			return
		}
		out := make([]recordResponse, 0, len(records))
		for _, rec := range records {
			out = append(out, recordResponse{
				ID:          rec.ID,
				BookName:    rec.BookName,
				ChapterName: rec.ChapterName,
				ChapterURL:  rec.ChapterURL,
				UpdateTime:  rec.UpdateTime.Local().Format(time.DateTime),
			})
		}
		writeJSON(w, http.StatusOK, out)
	}
}

func handleDeleteRecord(w http.ResponseWriter, r *http.Request) {
	idStr := chi.URLParam(r, "id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid id")
		return
	}
	if err := repo.DeleteRecord(r.Context(), id); err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func handleListBookSources(w http.ResponseWriter, r *http.Request) {
	sources, err := repo.GetAllBookSources(r.Context())
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	out := make([]bookSourceResponse, 0, len(sources))
	for _, src := range sources {
		out = append(out, toBookSourceResponse(src))
	}
	writeJSON(w, http.StatusOK, out)
}

func handleGetBookSource(w http.ResponseWriter, r *http.Request) {
	id, err := parseIDParam(r, "id")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid id")
		return
	}
	src, err := repo.GetBookSource(r.Context(), id)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, toBookSourceResponse(*src))
}

func handleCreateBookSource(w http.ResponseWriter, r *http.Request) {
	req, ok := decodeBookSourceRequest(w, r)
	if !ok {
		return
	}
	src := bookSourceFromRequest(req)
	if err := repo.CreateBookSource(r.Context(), &src); err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	writeJSON(w, http.StatusCreated, toBookSourceResponse(src))
}

func handleUpdateBookSource(w http.ResponseWriter, r *http.Request) {
	id, err := parseIDParam(r, "id")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid id")
		return
	}
	req, ok := decodeBookSourceRequest(w, r)
	if !ok {
		return
	}
	src := bookSourceFromRequest(req)
	src.ID = id
	if err := repo.UpdateBookSource(r.Context(), &src); err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, toBookSourceResponse(src))
}

func handleDeleteBookSource(w http.ResponseWriter, r *http.Request) {
	id, err := parseIDParam(r, "id")
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid id")
		return
	}
	if err := repo.DeleteBookSource(r.Context(), id); err != nil {
		writeError(w, http.StatusInternalServerError, err.Error())
		return
	}
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func handlePreviewBookSource(proc *processor.Processor) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req bookSourcePreviewRequest
		body, _ := io.ReadAll(r.Body)
		if err := json.Unmarshal(body, &req); err != nil {
			writeError(w, http.StatusBadRequest, "invalid body")
			return
		}
		if req.URL == "" {
			writeError(w, http.StatusBadRequest, "url required")
			return
		}
		src := bookSourceFromRequest(req.Source)
		c, err := proc.PreviewSource(r.Context(), &src, req.URL)
		if err != nil {
			writeError(w, http.StatusInternalServerError, err.Error())
			return
		}
		writeJSON(w, http.StatusOK, c)
	}
}

func decodeBookSourceRequest(w http.ResponseWriter, r *http.Request) (bookSourceRequest, bool) {
	var req bookSourceRequest
	body, _ := io.ReadAll(r.Body)
	if err := json.Unmarshal(body, &req); err != nil {
		writeError(w, http.StatusBadRequest, "invalid body")
		return bookSourceRequest{}, false
	}
	return req, true
}

func bookSourceFromRequest(req any) model.BookSource {
	switch v := req.(type) {
	case bookSourceRequest:
		return model.BookSource{
			BookSourceName:     v.BookSourceName,
			BookSourceURL:      v.BookSourceURL,
			BookNameRule:       v.BookNameRule,
			ChapterNameRule:    v.ChapterNameRule,
			ContentRule:        v.ContentRule,
			NextContentURLRule: v.NextContentURLRule,
			Enabled:            v.Enabled,
		}
	case bookSourcePreviewRequest:
		return bookSourceFromRequest(v.Source)
	default:
		return model.BookSource{}
	}
}

func toBookSourceResponse(src model.BookSource) bookSourceResponse {
	return bookSourceResponse{
		ID:                 src.ID,
		BookSourceName:     src.BookSourceName,
		BookSourceURL:      src.BookSourceURL,
		BookNameRule:       src.BookNameRule,
		ChapterNameRule:    src.ChapterNameRule,
		ContentRule:        src.ContentRule,
		NextContentURLRule: src.NextContentURLRule,
		Enabled:            src.Enabled,
	}
}

func parseIDParam(r *http.Request, name string) (int64, error) {
	return strconv.ParseInt(chi.URLParam(r, name), 10, 64)
}

func handleReadURL(svc *reading.Service) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req readURLRequest
		body, _ := io.ReadAll(r.Body)
		if err := json.Unmarshal(body, &req); err != nil || req.URL == "" {
			writeError(w, http.StatusBadRequest, "invalid body")
			return
		}
		c, err := svc.Open(r.Context(), req.URL)
		if err != nil {
			logger.Log.Errorf("read url %s: %v", req.URL, err)
			writeError(w, http.StatusInternalServerError, err.Error())
			return
		}
		writeJSON(w, http.StatusOK, c)
	}
}

func handlePrefetch(svc *reading.Service) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req readURLRequest
		body, _ := io.ReadAll(r.Body)
		if err := json.Unmarshal(body, &req); err != nil || req.URL == "" {
			writeError(w, http.StatusBadRequest, "invalid body")
			return
		}
		svc.Prefetch(r.Context(), req.URL)
		writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
	}
}

