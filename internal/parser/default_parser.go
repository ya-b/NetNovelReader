package parser

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"regexp"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/PuerkitoBio/goquery"
	"github.com/go-reader/reader/internal/model"
	"golang.org/x/net/html"
)

// DefaultParser implements the heuristic + LLM fallback parser.
type DefaultParser struct {
	httpClient *http.Client
}

func NewDefaultParser() *DefaultParser {
	return &DefaultParser{httpClient: &http.Client{Timeout: 60 * time.Second}}
}

// ParseContent extracts chapter info via LLM (if configured) or heuristics.
func (p *DefaultParser) ParseContent(ctx context.Context, llm *model.LLMConfig, pageURL, html string) *model.ChapterContent {
	doc, err := goquery.NewDocumentFromReader(strings.NewReader(html))
	if err != nil {
		return &model.ChapterContent{}
	}

	var (
		wg      sync.WaitGroup
		content string
		nextURL string
		book    string
		chapter string
	)

	wg.Add(2)
	go func() {
		defer wg.Done()
		content, nextURL = p.readContent(doc)
	}()
	go func() {
		defer wg.Done()
		if llm != nil {
			b, c := p.findTitleByLLM(ctx, llm, doc)
			book, chapter = b, c
			return
		}
		book, chapter = p.findTitleBySoup(doc)
	}()
	wg.Wait()

	if nextURL != "" && !strings.HasPrefix(nextURL, "http") {
		nextURL = normalizeURL(nextURL, pageURL, pageURL)
	}

	return &model.ChapterContent{
		BookName:    book,
		ChapterName: chapter,
		Content:     content,
		NextURL:     nextURL,
	}
}

func (p *DefaultParser) findTitleBySoup(doc *goquery.Document) (string, string) {
	t := strings.TrimSpace(doc.Find("title").First().Text())
	return t, t
}

type llmRequest struct {
	Model    string                   `json:"model"`
	Messages []map[string]string      `json:"messages"`
	Stream   bool                     `json:"stream"`
	Extra    map[string]any           `json:"-"`
}

func (p *DefaultParser) findTitleByLLM(ctx context.Context, cfg *model.LLMConfig, doc *goquery.Document) (string, string) {
	pageText := doc.Text()
	snippet := pageText
	n := len(pageText) / 2
	if n > 1000 {
		n = 1000
	}
	if n < len(pageText) {
		snippet = pageText[:n]
	}

	payload := map[string]any{
		"model":  cfg.Model,
		"stream": false,
		"messages": []map[string]string{
			{
				"role": "user",
				"content": strings.Join([]string{
					"从下面的网页中找到小说书名和章节名并以json回复，例如：",
					`{"book": "神的模仿犯", "chapter": "第144章 规则的博弈"}`,
					`如果未找到书名或章节名，请返回null, 例如:{"book": null, "chapter": null}`,
					"网页内容如下：",
					snippet,
				}, "\n"),
			},
		},
	}
	if cfg.ExtraBody != "" && cfg.ExtraBody != "{}" {
		var extra map[string]any
		if err := json.Unmarshal([]byte(cfg.ExtraBody), &extra); err == nil {
			for k, v := range extra {
				payload[k] = v
			}
		}
	}

	body, _ := json.Marshal(payload)
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, cfg.BaseURL+"/chat/completions", bytes.NewReader(body))
	if err != nil {
		return "", ""
	}
	req.Header.Set("Authorization", "Bearer "+cfg.APIKey)
	req.Header.Set("Content-Type", "application/json")

	resp, err := p.httpClient.Do(req)
	if err != nil {
		return "", ""
	}
	defer resp.Body.Close()
	if resp.StatusCode >= 400 {
		return "", ""
	}
	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", ""
	}

	var parsed struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
		} `json:"choices"`
	}
	if err := json.Unmarshal(data, &parsed); err != nil || len(parsed.Choices) == 0 {
		return "", ""
	}

	text := parsed.Choices[0].Message.Content
	re := regexp.MustCompile(`(?s)\{.*\}`)
	match := re.FindString(text)
	if match == "" {
		return "", ""
	}
	var info struct {
		Book    *string `json:"book"`
		Chapter *string `json:"chapter"`
	}
	if err := json.Unmarshal([]byte(match), &info); err != nil {
		return "", ""
	}
	book := ""
	chapter := ""
	if info.Book != nil {
		book = *info.Book
	}
	if info.Chapter != nil {
		chapter = *info.Chapter
	}
	return book, chapter
}

var nextAnchorText = map[string]struct{}{
	"下一章": {}, "下—章": {}, "下一页": {}, "下—页": {},
}

func (p *DefaultParser) readContent(doc *goquery.Document) (string, string) {
	// Strip noise.
	doc.Find("div#copyright, script, link, iframe, svg, ins, select").Remove()

	// Capture next-chapter anchor, then remove all anchors.
	nextURL := ""
	doc.Find("a").Each(func(_ int, a *goquery.Selection) {
		t := strings.TrimSpace(a.Text())
		if _, ok := nextAnchorText[t]; ok && nextURL == "" {
			if href, ok := a.Attr("href"); ok {
				nextURL = href
			}
		}
	})
	doc.Find("a").Remove()

	// Collect text content per non-leaf tag (excluding nested div text), mimicking
	// the Python behaviour of "decompose inner divs, then get_text".
	var divContent []string
	seen := map[string]struct{}{}
	doc.Find("*").Each(func(_ int, s *goquery.Selection) {
		tag := goquery.NodeName(s)
		if tag == "html" || tag == "script" || tag == "style" || tag == "meta" {
			return
		}
		clone := s.Clone()
		clone.Find("div").Remove()
		text := strings.TrimSpace(getTextSep(clone, "\n"))
		if text == "" {
			return
		}
		if _, ok := seen[text]; ok {
			return
		}
		seen[text] = struct{}{}
		divContent = append(divContent, text)
	})

	if len(divContent) == 0 {
		return "", nextURL
	}

	sort.Slice(divContent, func(i, j int) bool {
		return len(divContent[i]) > len(divContent[j])
	})

	prevLen := len(divContent[0])
	prev := divContent[0]
	for _, r := range divContent {
		if prevLen > 0 && float64(prevLen-len(r))*100/float64(prevLen) < 15 {
			prev = r
			continue
		}
		break
	}
	return prev, nextURL
}

// suppress unused import warning for fmt (kept for possible debug use).
var _ = fmt.Sprintf

// getTextSep mimics BeautifulSoup's Tag.get_text(separator): it walks all
// descendant text nodes in document order and joins their (non-empty, stripped)
// values with sep. This preserves logical line breaks that would otherwise be
// lost when flattening nested <p>/<br>/inline elements via goquery's Text().
func getTextSep(s *goquery.Selection, sep string) string {
	var parts []string
	var walk func(*html.Node)
	walk = func(n *html.Node) {
		if n == nil {
			return
		}
		if n.Type == html.TextNode {
			t := strings.TrimSpace(n.Data)
			if t != "" {
				parts = append(parts, t)
			}
			return
		}
		for c := n.FirstChild; c != nil; c = c.NextSibling {
			walk(c)
		}
	}
	for _, n := range s.Nodes {
		walk(n)
	}
	return strings.Join(parts, sep)
}
