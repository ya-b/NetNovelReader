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
	"github.com/go-reader/reader/internal/processor"
	"github.com/go-reader/reader/internal/repo"
)

const authCookieName = "auth_token"

type readURLRequest struct {
	URL string `json:"url"`
}

type readURLResponse struct {
	BookName    string `json:"book_name"`
	ChapterName string `json:"chapter_name"`
	ChapterURL  string `json:"chapter_url"`
	Content     string `json:"content"`
	NextURL     string `json:"next_url"`
}

type recordResponse struct {
	ID          int64  `json:"id"`
	BookName    string `json:"book_name"`
	ChapterName string `json:"chapter_name"`
	ChapterURL  string `json:"chapter_url"`
	UpdateTime  string `json:"update_time"`
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

func handleGetRecords(w http.ResponseWriter, r *http.Request) {
	records, err := repo.GetAllRecords(r.Context(), "update_time DESC")
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

func handleReadURL(proc *processor.Processor) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var req readURLRequest
		body, _ := io.ReadAll(r.Body)
		if err := json.Unmarshal(body, &req); err != nil || req.URL == "" {
			writeError(w, http.StatusBadRequest, "invalid body")
			return
		}
		c, err := proc.ReadURLContent(r.Context(), req.URL)
		if err != nil {
			logger.Log.Errorf("read url %s: %v", req.URL, err)
			writeError(w, http.StatusInternalServerError, err.Error())
			return
		}
		writeJSON(w, http.StatusOK, readURLResponse{
			BookName:    c.BookName,
			ChapterName: c.ChapterName,
			ChapterURL:  c.ChapterURL,
			Content:     c.Content,
			NextURL:     c.NextURL,
		})
	}
}
