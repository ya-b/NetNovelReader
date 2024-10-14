package postprocess

import (
	"sync"

	"github.com/longbridgeapp/opencc"
)

var (
	conv     *opencc.OpenCC
	convOnce sync.Once
)

// ToSimplified converts Traditional Chinese text to Simplified Chinese (t2s).
func ToSimplified(s string) string {
	if s == "" {
		return s
	}
	convOnce.Do(func() {
		c, err := opencc.New("t2s")
		if err == nil {
			conv = c
		}
	})
	if conv == nil {
		return s
	}
	out, err := conv.Convert(s)
	if err != nil {
		return s
	}
	return out
}
