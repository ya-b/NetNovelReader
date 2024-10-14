import 'dart:io';

import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'package:net_novel_reader/util/parse_content.dart';

var logger = Logger(
  printer: PrettyPrinter(),
);

class ReaderPage extends StatefulWidget {
  const ReaderPage({super.key});

  @override
  State<ReaderPage> createState() => _ReaderPageState();
}

class _ReaderPageState extends State<ReaderPage> {
  ValueNotifier<String> url = ValueNotifier<String>('');
  var content = 'loading...';
  final httpClient = HttpClient();
  late final ChapterParser chapterParser;
  _ReaderPageState() {
    chapterParser = ChapterParser();
  }

  void getContent() {
    if (url.value.isEmpty) return;
    chapterParser.getChapter(url.value).then((value) {
      var (text, nextPage) = value;
      setState(() {
        content = text;
      });
    });
  }

  @override
  void initState() {
    super.initState();
    getContent();
    url.addListener(getContent);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Reader'),
      ),
      body: Text(content),
    );
  }
}
