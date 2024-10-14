package logger

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"strings"

	"github.com/sirupsen/logrus"
)

type pythonStyleFormatter struct{}

func (f *pythonStyleFormatter) Format(entry *logrus.Entry) ([]byte, error) {
	module := "???"
	funcName := "???"
	line := 0
	for skip := 2; skip < 20; skip++ {
		pc, file, ln, ok := runtime.Caller(skip)
		if !ok {
			break
		}
		fn := runtime.FuncForPC(pc)
		if fn != nil && strings.HasPrefix(fn.Name(), "github.com/sirupsen/logrus") {
			continue
		}
		module = strings.TrimSuffix(filepath.Base(file), filepath.Ext(file))
		line = ln
		if fn != nil {
			parts := strings.Split(fn.Name(), ".")
			funcName = parts[len(parts)-1]
		}
		break
	}
	timestamp := entry.Time.Format("2006-01-02 15:04:05")
	level := strings.ToUpper(entry.Level.String())
	msg := entry.Message
	var buf strings.Builder
	buf.WriteString(fmt.Sprintf("%s [%s] %s %s - [%s,%s,%d] - %s",
		timestamp, "main", level, "logger", module, funcName, line, msg))
	for k, v := range entry.Data {
		buf.WriteString(fmt.Sprintf(" %s=%v", k, v))
	}
	buf.WriteByte('\n')
	return []byte(buf.String()), nil
}

var Log = logrus.New()

func Init() *os.File {
	exe, err := os.Executable()
	if err != nil {
		Log.Fatalf("get executable path: %v", err)
	}
	logFile := filepath.Join(filepath.Dir(exe), "app.log")
	f, err := os.OpenFile(logFile, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
	if err != nil {
		Log.Fatalf("open log file %s: %v", logFile, err)
	}
	Log.SetOutput(f)
	Log.SetFormatter(&pythonStyleFormatter{})
	Log.Infof("log output redirected to %s", logFile)
	return f
}
