import 'dart:convert';

import 'package:html/dom.dart';
import 'package:html/parser.dart';
import 'package:http/http.dart' as http;
import 'package:net_novel_reader/util/gpt.dart';

class CatalogParser {
  final prompt =
      '请根据以下html网页，给出小说目录所有章节的css选择器，可以根据该css选择器获取整个目录。\n结果仅以json的格式返回css选择器，不要有其他内容。例如: \n{"catalog_selector": "catalogSelector"}\n\n以下是网页html:\n';
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
      "temperature": 0.3,
    });
    String content = json.decode(body)['result'];
    var chapter = json.decode(regExp.firstMatch(content)?.group(1) ?? '{}');
    return chapter['catalog_selector'];
  }

  void removeAll(Element element, String tag) {
    while (element.querySelector(tag) != null) {
      element.querySelector(tag)?.remove();
    }
  }

  String getCssSelector(Element? element) {
    List<String> selectors = [];

    while (element != null && element.localName != 'html' && element.localName != null) {
      String selector = element.localName!;

      // 如果元素有 id，直接使用 id 作为选择器
      if (element.id.isNotEmpty) {
        selector += '#${element.id}';
        selectors.insert(0, selector);
        break; // 有 id 就可以唯一定位，直接终止循环
      }

      // 如果元素有 class，添加类名
      if (element.classes.isNotEmpty) {
        selector += '.${element.classes.join('.')}';
      }

      // 获取元素在父级中的索引，以确保唯一性
      Element? parent = element.parent;
      if (parent != null) {
        var sameSelector = parent.children.where((e) {
          if (e.localName != element?.localName) {
            return false;
          }
          if (e.classes.length != element?.classes.length) {
            return false;
          }
          return e.classes.containsAll(element?.classes ?? []);
        }).toList();
        if (sameSelector.length > 1) {
          int index = parent.children.toList().indexOf(element) + 1;
          selector += ':nth-child($index)';
        }
      }
      selectors.insert(0, selector);
      element = parent;
    }
    return selectors.join(' > ');
  }

  Map<String, String> getContent(Document document, String selector) {
    var catalog = <String, String>{};
    List<Element> ulList = [];
    if (selector.isNotEmpty) {
      ulList = document.querySelectorAll(selector);
    }
    if (ulList.isEmpty) {
      ulList = document.querySelectorAll('ul');
    }
    ulList.sort((a, b) => b.children.length - a.children.length);
    for (var ele in ulList[0].children) {
      catalog.remove(ele.text);
      catalog[ele.text] = ele.querySelector('a')?.attributes['href'] ?? '';
    }
    return catalog;
  }

  String getCatalogSelector(Document document) {
    var ulList = document.querySelectorAll('ul');
    ulList.sort((a, b) => b.children.length - a.children.length);
    if (ulList.isEmpty) {
      return '';
    }
    var selector = getCssSelector(ulList[0]);
    return selector;
  }

  String getBookName(Document document) {
    var bookNames = <String>[];
    document.querySelectorAll('h1').forEach((element) {
      bookNames.add(element.text.trim());
    });
    document.querySelectorAll('h2').forEach((element) {
      bookNames.add(element.text.trim());
    });
    RegExp(r'《(.*?)》', multiLine: true, dotAll: true)
        .allMatches(document.outerHtml)
        .forEach((match) {
      bookNames.add(match.group(1) ?? '');
    });
    Map<String, int> itemCount = {};
    for (var item in bookNames) {
      if (itemCount.containsKey(item)) {
        itemCount[item] = itemCount[item]! + 1;
      } else {
        itemCount[item] = 1;
      }
    }
    var entries = itemCount.entries.toList();
    entries.sort((a, b) => b.value - a.value);
    return entries[0].key;
  }

  Future<(String, String, Map<String, String>)> getNameAndCatalog(String url) async {
    var response = await http.get(Uri.parse(url));
    var responseBody = utf8.decode(response.bodyBytes);
    var document = parse(responseBody);
    var bookName = getBookName(document);
    var catalogSelector = getCatalogSelector(document);
    return (bookName, catalogSelector, getContent(document, catalogSelector));
  }

  String getNextPage(Document document) {
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

  Stream<(String, String, String)> getCatalog(String url, String selector) async* {
    Uri uri = Uri.parse(url);
    while (true) {
      var response = await http.get(uri);
      var responseBody = utf8.decode(response.bodyBytes);
      var document = parse(responseBody);
      for (var entries in getContent(document, selector).entries) {
        yield (uri.toString(), entries.key, entries.value);
      }
      var nextpage = getNextPage(document);
      if (nextpage.isEmpty) {
        break;
      }
      uri = Uri.parse(url).resolve(nextpage);
    }
    // String selector = await getSelectorByGpt(document);
    // print(selector);
    // Element? catalogElement = document.querySelector(selector);
    // var catalog = <String, String>{};
    // for (Element ele in catalogElement?.querySelectorAll('a') ?? []) {
    //   catalog.remove(ele.text);
    //   catalog[ele.text] = ele.attributes['href'] ?? '';
    // }
  }
}
