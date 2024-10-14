import 'dart:collection';
import 'dart:convert';

import 'package:html/dom.dart' as dom;
import 'package:html/dom.dart';
import 'package:html/parser.dart';
import 'package:http/http.dart' as http;
import 'package:net_novel_reader/util/gpt.dart';

class ChapterParser {
  final systemPrompt =
      '我希望你担任高级前端开发人员。你精通html、css、js等，并且能根据html内容，写出对应的css选择器，并返回一个json格式的响应。';
  final prompt =
      '请根据以下html网页，给出章节正文内容的css选择器，并仅以json的格式返回，不要有其他内容。返回的json格式为: \n{"chapter_selector": "chapterContentSelector"}\n\n以下是网页html:\n';
  final regExp = RegExp(r'```json(.*?)```', multiLine: true, dotAll: true);

  Future<String> getSelectorByGpt(Document document) async {
    removeAll(document.body!, 'style');
    removeAll(document.body!, 'script');
    removeAll(document.body!, 'link');
    removeAll(document.body!, 'iframe');
    removeAll(document.body!, 'ul');
    removeAll(document.body!, 'svg');
    removeAll(document.body!, 'ins');
    removeAll(document.body!, 'select');
    var body = await GptUtil.chat({
      "messages": [
        {'role': 'user', 'content': '$prompt${document.body?.outerHtml}'}
      ],
      "system": systemPrompt,
      "temperature": 0.3,
    });
    String content = json.decode(body)['result'];
    var chapter = json.decode(regExp.firstMatch(content)?.group(1) ?? '{}');
    return chapter['chapter_selector'];
  }

  String getContent(Document document) {
    removeAll(document.body!, 'style');
    removeAll(document.body!, 'script');
    removeAll(document.body!, 'iframe');
    removeAll(document.body!, 'ul');
    removeAll(document.body!, 'svg');
    removeAll(document.body!, 'ins');
    removeAll(document.body!, 'select');
    var queue = Queue<Element>();
    var texts = <String>{};
    queue.addAll(document.body?.children ?? []);
    while (queue.isNotEmpty) {
      var node = queue.removeFirst();
      String text;
      if (node.querySelector('div') != null) {
        queue.addAll(node.children);
        var node_ = node.clone(false);
        removeAll(node_, 'div');
        text = node_.text;
      } else {
        text = node.text;
      }
      while (text.contains('\n\n\n')) {
        text = text.replaceAll('\n\n\n', '\n\n');
      }
      texts.add(text);
    }
    var textList = texts.toList();
    textList.sort((a, b) => b.length - a.length);
    return textList[0];
  }

  Future<String> getNextPage(Document document) async {
    String nextpage = '';
    document.querySelectorAll('a').forEach((element) {
      if ((element.text.replaceAll('\n', '').startsWith('下一') ||
              element.text.replaceAll('\n', '').startsWith('下—')) &&
          element.attributes.containsKey('href')) {
        nextpage = element.attributes['href']!;
      }
    });
    return nextpage;
  }

  void removeAll(dom.Element element, String tag) {
    while (element.querySelector(tag) != null) {
      element.querySelector(tag)?.remove();
    }
  }

  Future<(String, String)> getChapter(String url) async {
    var response = await http.get(Uri.parse(url));
    var responseBody = utf8.decode(response.bodyBytes);
    var document = parse(responseBody);
    String nextpage = await getNextPage(document);
    // String selector = await getSelectorByGpt(document);
    // var text = document.querySelector(selector)?.text ?? '';
    var text = getContent(document);
    while (text.contains('\n\n\n')) {
      text = text.replaceAll('\n\n\n', '\n\n');
    }
    text = text.replaceAll('^\n*', '');
    text = text.replaceAll('\n*\$', '');
    return (text, nextpage);
  }
}
