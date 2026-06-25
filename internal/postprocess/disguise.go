package postprocess

import (
	"math/rand"
	"strings"
	"time"
)

var logLevels = []string{"DEBUG", "INFO", "WARN", "ERROR"}

var classNames = []string{
	"sqlalchemy.pool.impl.AsyncAdaptedQueuePool",
	"app.parser.engine",
	"c.z.s.j.UpdateServiceJob",
	"o.s.w.s.DispatcherServlet",
	"c.z.s.k.ComputeResourceUtils",
	"c.z.s.c.s.RemoteSessionHelper",
	"o.s.c.s.AbstractApplicationContext",
	"o.s.b.f.s.DefaultListableBeanFactory",
	"o.s.b.f.a.AutowiredAnnotationBeanPostProcessor",
	"c.z.s.c.f.s.b.f.a.AutowiredAnnotationBeanPostProcessor",
	"c.z.s.c.centos.read.hello.world.next.word.RemoteSessionHelper",
	"o.m.driver.cluster [cluster-ClusterId{value='66b19afcf0130258e9782343'}-service-mongodb:27017]",
}

// GenLog returns a fake Java/Spring-style log line prefix.
func GenLog() string {
	lvl := logLevels[rand.Intn(len(logLevels))]
	cls := classNames[rand.Intn(len(classNames))]
	return time.Now().Format("2006-01-02 15:04:05") + " " + lvl + " " + cls + ": "
}

// Disguise prepends a fake log-line prefix (GenLog) to every line of content so
// a shared terminal looks like a build log rather than a novel. Blank lines are
// prefixed too, matching the line count of the input.
func Disguise(content string) string {
	if content == "" {
		return content
	}
	lines := strings.Split(content, "\n")
	for i, line := range lines {
		lines[i] = GenLog() + line
	}
	return strings.Join(lines, "\n")
}
