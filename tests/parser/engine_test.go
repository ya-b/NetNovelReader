package parser_test

import (
	"context"
	"strings"
	"testing"

	"github.com/go-reader/reader/internal/model"
	"github.com/go-reader/reader/internal/parser"
)

const sampleHTML = `<!DOCTYPE html>
<html>
<head>
<title>腐朽世界 第312章 312疯狂进化 四-滚开小说-全文免费阅读-速读谷</title>
<meta name="keywords" content="第312章 312疯狂进化 四,腐朽世界,精校版,无错字,滚开,速读谷"/>
<meta name="description" content="速读谷提供辰东创作的玄幻小说《腐朽世界》第312章 312疯狂进化 四在线阅读,第312章 312疯狂进化 四无错字精校版全文免费阅读。"/>
<meta http-equiv="Content-type" name="viewport" content="initial-scale=1.0, maximum-scale=1.0, user-scalable=no, width=device-width" />
<link media="all" type="text/css" href="/css/g.css" rel="stylesheet"/>
<script type="text/javascript" src="/js/jquery.min.js"></script>
<meta http-equiv="content-language" content="zh-CN">
<link media="all" type="text/css" href="/css/6/skin.css" rel="stylesheet"/>
<script type="text/javascript" src="/js/jquery.common.js"></script>
<style type="text/css">.con{font-size:20px;}</style>
<script type="text/javascript">
    var Title = "腐朽世界"; var ArticleTitle = "第312章 312疯狂进化 四";var BookID = "1921";var ArticleID = "3320276";</script>
<script type="text/javascript" src="/js/jquery.art.js"></script>
</head>

<body>

<div class="con">

<p>当然，说别人精神有问题，那是自己有一套所谓的正常参考标准。</p>

</div>
<div class="prenext"><span><a href="/1921/3320276-3.html">上一页</a></span><a href="/1921/#dir">目录</a><span><a href="/1921/3342653.html">下一章</a></span></div>
</div>
<div class="container">
<h2><a href="/xuanhuan/">相关小说</a><span><a href="/xuanhuan/">全部</a></span></h2>
</div>

</body>
</html>`

func TestRuleParserCSSRules(t *testing.T) {
	rp := parser.NewRuleParser()
	src := &model.BookSource{
		BookSourceURL:      "https://example.com/",
		BookNameRule:       `@css:title@text##^([^\s]+)\s.*##$1`,
		ChapterNameRule:    `@css:title@text##^[^\s]+\s+([^-]+)-.*##$1`,
		ContentRule:        `@css:div.con`,
		NextContentURLRule: `@css:div.prenext span:last-of-type a@href`,
	}
	got, err := rp.ParseContent(src, "https://example.com/chapter/1", sampleHTML)
	if err != nil {
		t.Fatalf("parse: %v", err)
	}
	if got.BookName != "腐朽世界" {
		t.Errorf("book_name = %q", got.BookName)
	}
	if got.ChapterName != "第312章 312疯狂进化 四" {
		t.Errorf("chapter_name = %q", got.ChapterName)
	}
	if !strings.Contains(got.Content, "当然，说别人精神有问题，那是自己有一套所谓的正常参考标准。") {
		t.Errorf("content missing: %q", got.Content)
	}
	if got.NextURL != "https://example.com/1921/3342653.html" {
		t.Errorf("next_url = %q", got.NextURL)
	}
}

func TestDefaultParserHeuristic(t *testing.T) {
	dp := parser.NewDefaultParser()
	got := dp.ParseContent(context.Background(), nil, "https://example.com/c/1", sampleHTML)
	if got.BookName == "" {
		t.Errorf("expected title fallback, got empty")
	}
	if got.NextURL != "https://example.com/1921/3342653.html" {
		t.Errorf("next_url = %q", got.NextURL)
	}
}
