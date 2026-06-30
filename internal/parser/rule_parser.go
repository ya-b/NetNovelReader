package parser

import (
	"errors"
	"regexp"
	"strings"

	"github.com/PuerkitoBio/goquery"
	"github.com/go-reader/reader/internal/logger"
	"github.com/go-reader/reader/internal/model"
)

// ErrRuleExecution is returned when a rule cannot be parsed or executed.
var ErrRuleExecution = errors.New("rule execution error")

// RuleParser executes a restricted subset of Legado rules.
type RuleParser struct{}

func NewRuleParser() *RuleParser { return &RuleParser{} }

// ParseContent parses a content page using the given book source rule.
func (r *RuleParser) ParseContent(src *model.BookSource, pageURL, html string) (*model.ChapterContent, error) {
	doc, err := goquery.NewDocumentFromReader(strings.NewReader(html))
	if err != nil {
		return nil, err
	}
	bookName, _ := r.extractPageValue(doc, html, pageURL, src.BookNameRule, false)
	chapterName, _ := r.extractPageValue(doc, html, pageURL, src.ChapterNameRule, false)
	content, _ := r.extractPageValue(doc, html, pageURL, src.ContentRule, true)
	nextURL, _ := r.extractPageValue(doc, html, pageURL, src.NextContentURLRule, false)

	if strings.TrimSpace(content) == "" {
		logger.Log.Warnf("content is empty: %s", pageURL)
		return nil, ErrRuleExecution
	}

	return &model.ChapterContent{
		BookName:    strings.TrimSpace(bookName),
		ChapterName: strings.TrimSpace(chapterName),
		Content:     strings.TrimSpace(content),
		NextURL:     normalizeURL(nextURL, pageURL, src.BookSourceURL),
	}, nil
}

func (r *RuleParser) extractPageValue(doc *goquery.Document, html, pageURL, rule string, join bool) (string, error) {
	rule = strings.TrimSpace(rule)
	if rule == "" {
		return "", nil
	}

	var values []string
	switch {
	case strings.HasPrefix(rule, ":"):
		matches := regexFindAll(html, rule[1:])
		for _, m := range matches {
			values = append(values, strings.TrimSpace(flattenMatch(m)))
		}
	case isSelectorRule(rule):
		values = r.extractSelectorValues(doc.Selection, rule)
	default:
		if v := r.extractDirectValueFromHTML(rule, doc); strings.TrimSpace(v) != "" {
			values = append(values, strings.TrimSpace(v))
		}
	}

	// filter empty
	clean := values[:0]
	for _, v := range values {
		if v != "" {
			clean = append(clean, v)
		}
	}
	if len(clean) == 0 {
		return "", nil
	}
	if join {
		return strings.Join(clean, "\n"), nil
	}
	first := clean[0]
	if looksLikeURLRule(rule) {
		return normalizeURL(first, pageURL, pageURL), nil
	}
	return first, nil
}

func (r *RuleParser) extractDirectValueFromHTML(rule string, doc *goquery.Document) string {
	switch rule {
	case "text", "all", "textNodes", "ownText":
		return getTextSep(doc.Selection, "\n")
	case "html":
		h, _ := doc.Html()
		return h
	}
	return rule
}

func (r *RuleParser) extractSelectorValues(scope *goquery.Selection, rule string) []string {
	switch {
	case strings.HasPrefix(rule, "@css:"):
		selector, field := parseCSSRule(rule)
		var out []string
		scope.Find(selector).Each(func(_ int, s *goquery.Selection) {
			out = append(out, strings.TrimSpace(r.extractElementValue(s, field)))
		})
		return out
	case strings.HasPrefix(rule, "@XPath:") || strings.HasPrefix(rule, "//"):
		// Fallback: convert simplified XPath to CSS where possible.
		selector, field := parseXPathRule(rule)
		css := xpathToCSS(selector)
		if css == "" {
			return nil
		}
		var out []string
		scope.Find(css).Each(func(_ int, s *goquery.Selection) {
			out = append(out, strings.TrimSpace(r.extractElementValue(s, field)))
		})
		return out
	}
	return nil
}

func (r *RuleParser) extractElementValue(s *goquery.Selection, rule string) string {
	base, replacements := splitReplacements(rule)
	value := r.applyElementRule(s, base)
	return applyReplacements(value, replacements)
}

func (r *RuleParser) applyElementRule(s *goquery.Selection, rule string) string {
	rule = strings.TrimSpace(rule)
	switch {
	case strings.HasPrefix(rule, "@css:"):
		selector, field := parseCSSRule(rule)
		children := s.Find(selector)
		if children.Length() == 0 {
			return ""
		}
		return r.extractElementValue(children.First(), field)
	case strings.HasPrefix(rule, "@XPath:") || strings.HasPrefix(rule, "//"):
		selector, field := parseXPathRule(rule)
		css := xpathToCSS(selector)
		if css == "" {
			return ""
		}
		children := s.Find(css)
		if children.Length() == 0 {
			return ""
		}
		if field == "text" {
			return getTextSep(children.First(), "\n")
		}
		if strings.HasPrefix(field, "@") {
			v, _ := children.First().Attr(strings.TrimPrefix(field, "@"))
			return v
		}
		return r.extractElementValue(children.First(), field)
	case rule == "" || rule == "text":
		return getTextSep(s, "\n")
	case rule == "textNodes", rule == "ownText":
		var parts []string
		s.Contents().Each(func(_ int, c *goquery.Selection) {
			if goquery.NodeName(c) == "#text" {
				t := strings.TrimSpace(c.Text())
				if t != "" {
					parts = append(parts, t)
				}
			}
		})
		return strings.Join(parts, "\n")
	case rule == "html":
		h, _ := s.Html()
		return h
	case rule == "all":
		h, _ := goquery.OuterHtml(s)
		return h
	case strings.HasPrefix(rule, "@"):
		v, _ := s.Attr(strings.TrimPrefix(rule, "@"))
		return v
	case rule == "href", rule == "src":
		v, _ := s.Attr(rule)
		return v
	}
	v, _ := s.Attr(rule)
	return v
}

// Helpers.

func splitReplacements(rule string) (string, [][2]string) {
	parts := strings.Split(rule, "##")
	base := parts[0]
	var replacements [][2]string
	i := 1
	for i < len(parts) {
		pat := parts[i]
		repl := ""
		if i+1 < len(parts) {
			repl = parts[i+1]
		}
		repl = strings.TrimSuffix(repl, "###")
		replacements = append(replacements, [2]string{pat, repl})
		i += 2
	}
	return base, replacements
}

var dollarGroup = regexp.MustCompile(`\$(\d+)`)

func applyReplacements(value string, replacements [][2]string) string {
	for _, pair := range replacements {
		pat, repl := pair[0], pair[1]
		re, err := regexp.Compile(pat)
		if err != nil {
			continue
		}
		// Convert $1 to ${1} for Go regex replace template.
		goRepl := dollarGroup.ReplaceAllString(repl, "${$1}")
		value = re.ReplaceAllString(value, goRepl)
	}
	return value
}

func regexFindAll(html, pattern string) [][]string {
	re, err := regexp.Compile("(?s)" + pattern)
	if err != nil {
		return nil
	}
	return re.FindAllStringSubmatch(html, -1)
}

func flattenMatch(m []string) string {
	if len(m) == 0 {
		return ""
	}
	if len(m) == 1 {
		return m[0]
	}
	// Join capture groups (skip index 0 = full match) if any, else return full match.
	var b strings.Builder
	for _, g := range m[1:] {
		b.WriteString(g)
	}
	if b.Len() > 0 {
		return b.String()
	}
	return m[0]
}

func isSelectorRule(rule string) bool {
	return strings.HasPrefix(rule, "@css:") || strings.HasPrefix(rule, "@XPath:") || strings.HasPrefix(rule, "//")
}

func looksLikeURLRule(rule string) bool {
	for _, token := range []string{"href", "src", "Url", "URL", "url", "@href", "@src"} {
		if strings.Contains(rule, token) {
			return true
		}
	}
	return false
}

func parseCSSRule(rule string) (string, string) {
	raw := strings.TrimPrefix(rule, "@css:")
	selector, field := raw, "text"
	if i := strings.LastIndex(raw, "@"); i >= 0 {
		selector = raw[:i]
		field = raw[i+1:]
	}
	selector = strings.TrimSpace(selector)
	field = strings.TrimSpace(field)
	if field == "" {
		field = "text"
	}
	return selector, field
}

var (
	xpathAttrRe = regexp.MustCompile(`^(.*?)/@([A-Za-z0-9:_-]+)$`)
	xpathTextRe = regexp.MustCompile(`^(.*?)/text\(\)$`)
)

func parseXPathRule(rule string) (string, string) {
	raw := strings.TrimPrefix(rule, "@XPath:")
	if m := xpathAttrRe.FindStringSubmatch(raw); m != nil {
		return strings.TrimSpace(m[1]), "@" + m[2]
	}
	if m := xpathTextRe.FindStringSubmatch(raw); m != nil {
		return strings.TrimSpace(m[1]), "text"
	}
	return strings.TrimSpace(raw), "text"
}

// xpathToCSS converts a small subset of XPath (descendant //tag, tag[@attr='v'],
// tag[contains(@attr,'v')], tag[n]) to an equivalent CSS selector. Returns "" if
// the expression is not convertible.
var (
	xpathTagRe        = regexp.MustCompile(`^(\*|[A-Za-z_][A-Za-z0-9:_-]*)$`)
	xpathAttrEqRe     = regexp.MustCompile(`^@([A-Za-z0-9:_-]+)\s*=\s*['"](.*?)['"]$`)
	xpathAttrContains = regexp.MustCompile(`^contains\(\s*@([A-Za-z0-9:_-]+)\s*,\s*['"](.*?)['"]\s*\)$`)
	xpathNumRe        = regexp.MustCompile(`^\d+$`)
)

func xpathToCSS(selector string) string {
	s := strings.TrimSpace(selector)
	if s == "" {
		return ""
	}
	descendant := strings.HasPrefix(s, "//")
	s = strings.TrimPrefix(s, "//")
	s = strings.TrimPrefix(s, "/")

	var parts []string
	for _, step := range splitXPathSteps(s) {
		css := xpathStepToCSS(step)
		if css == "" {
			return ""
		}
		parts = append(parts, css)
	}
	sep := " > "
	if descendant {
		sep = " "
	}
	return strings.Join(parts, sep)
}

func splitXPathSteps(path string) []string {
	var steps []string
	depth := 0
	var cur strings.Builder
	for _, ch := range path {
		switch ch {
		case '/':
			if depth == 0 {
				if cur.Len() > 0 {
					steps = append(steps, cur.String())
					cur.Reset()
				}
				continue
			}
		case '[':
			depth++
		case ']':
			if depth > 0 {
				depth--
			}
		}
		cur.WriteRune(ch)
	}
	if cur.Len() > 0 {
		steps = append(steps, cur.String())
	}
	return steps
}

var xpathStepRe = regexp.MustCompile(`^(?P<tag>\*|[A-Za-z_][A-Za-z0-9:_-]*)(?P<preds>(\[[^\]]+\])*)$`)

func xpathStepToCSS(step string) string {
	step = strings.TrimSpace(step)
	m := xpathStepRe.FindStringSubmatch(step)
	if m == nil {
		return ""
	}
	tag := m[1]
	var css strings.Builder
	css.WriteString(tag)

	preds := regexp.MustCompile(`\[([^\]]+)\]`).FindAllStringSubmatch(m[2], -1)
	for _, p := range preds {
		pred := strings.TrimSpace(p[1])
		if xpathNumRe.MatchString(pred) {
			css.WriteString(":nth-of-type(" + pred + ")")
			continue
		}
		if mm := xpathAttrEqRe.FindStringSubmatch(pred); mm != nil {
			css.WriteString("[" + mm[1] + "='" + mm[2] + "']")
			continue
		}
		if mm := xpathAttrContains.FindStringSubmatch(pred); mm != nil {
			css.WriteString("[" + mm[1] + "*='" + mm[2] + "']")
			continue
		}
		// Unsupported predicate.
		return ""
	}
	return css.String()
}
