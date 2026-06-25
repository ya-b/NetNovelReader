package postprocess

import (
	"strings"
	"testing"
)

func TestDisguiseEmpty(t *testing.T) {
	if got := Disguise(""); got != "" {
		t.Errorf("Disguise(\"\") = %q, want empty", got)
	}
}

func TestDisguisePrefixesEveryLine(t *testing.T) {
	in := "first\nsecond\nthird"
	out := Disguise(in)

	if strings.Count(out, "\n") != strings.Count(in, "\n") {
		t.Fatalf("line count changed: in had %d newlines, out has %d",
			strings.Count(in, "\n"), strings.Count(out, "\n"))
	}

	inLines := strings.Split(in, "\n")
	for i, line := range strings.Split(out, "\n") {
		if !strings.HasSuffix(line, inLines[i]) {
			t.Errorf("line %d %q does not end with original %q", i, line, inLines[i])
		}
		if !strings.Contains(line, ": ") || len(line) <= len(inLines[i]) {
			t.Errorf("line %d %q missing a log prefix", i, line)
		}
	}
}

func TestDisguiseKeepsBlankSeparators(t *testing.T) {
	// Paragraph separators are blank lines; they must still be counted (and
	// prefixed) so downstream rendering stays aligned.
	out := Disguise("para1\n\npara2")
	if got := strings.Count(out, "\n"); got != 2 {
		t.Errorf("expected 2 newlines preserved, got %d", got)
	}
	mid := strings.Split(out, "\n")[1]
	if !strings.Contains(mid, ": ") {
		t.Errorf("blank separator line %q was not prefixed", mid)
	}
}
