import 'package:async/async.dart';
import 'package:flutter/material.dart';
import 'package:net_novel_reader/data/book.dart';
import 'package:net_novel_reader/data/catalog.dart';
import 'package:net_novel_reader/main.dart';
import 'package:net_novel_reader/util/database_helper.dart';
import 'package:net_novel_reader/util/parse_catalog.dart';
import 'package:provider/provider.dart';

class CatalogPage extends StatefulWidget {
  const CatalogPage({super.key});

  @override
  State<CatalogPage> createState() => _CatalogPageState();
}

class _CatalogPageState extends State<CatalogPage> {
  bool isLoading = false;
  var catalog = <Catalog>[];
  late final CatalogParser catalogParser;
  final dbHelper = DatabaseHelper();
  StreamCloser streamCloser = StreamCloser();
  late CancelableOperation? cancelableOperation;

  _CatalogPageState() {
    catalogParser = CatalogParser();
  }

  Future<void> getContent() async {
    int bookId =
        Provider.of<MessageModel>(context, listen: false).currentBookId;
    var db = await dbHelper.database;
    var result = await db.query('book', where: 'id = ?', whereArgs: [bookId]);
    if (result.isEmpty) {
      return;
    }
    Book book = Book.fromMap(result.first);
    int maxIdx = catalog.lastOrNull?.id ?? -1;
    List<Map<String, Object?>> catalogMaps = await db.query('catalog',
        where: 'bookId = ? and id > ?', whereArgs: [bookId, maxIdx]);
    setState(() {
      catalog.addAll([for (var map in catalogMaps) Catalog.fromMap(map)]);
      isLoading = true;
    });
    var latestUrl = book.latestUrl;
    if (!streamCloser.isClosed) {
      streamCloser.close();
    }
    var getCatalogStream = catalogParser.getCatalog(book.latestUrl, book.catalogSelector);
    streamCloser.bind(getCatalogStream);
    await for (final (catalogUrl, chapterTitle, chapterUrl) in getCatalogStream) {
      if (catalog.any((c) => c.url == chapterUrl)) {
        continue;
      }
      int id = await db.insert('catalog',
          {'title': chapterTitle, 'url': chapterUrl, 'bookId': bookId});
      setState(() {
        catalog.add(Catalog(
            id: id, title: chapterTitle, url: chapterUrl, bookId: bookId));
      });
      latestUrl = catalogUrl;
    }
    if (!streamCloser.isClosed) {
      streamCloser.close();
    }
    maxIdx = catalog.lastOrNull?.id ?? -1;
    catalogMaps = await db.query('catalog', where: 'bookId >= $maxIdx');
    setState(() {
      isLoading = false;
    });
    await db.update('book', {'latestUrl': latestUrl},
        where: 'id = ?', whereArgs: [bookId]);
  }

  @override
  void initState() {
    super.initState();
    cancelableOperation = CancelableOperation.fromFuture(getContent());
  }

  @override
  void dispose() {
    if (!streamCloser.isClosed) {
      streamCloser.close();
    }
    if (!(cancelableOperation?.isCompleted ?? true)) {
      cancelableOperation?.cancel();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Row(
            children: [
              const Text('目录'),
              const SizedBox(width: 10),
              SizedBox(
                  width: 20,
                  height: 20,
                  child: isLoading
                      ? const CircularProgressIndicator()
                      : const SizedBox(
                          width: 1,
                        )),
            ],
          ),
        ),
        body: ListView.builder(
          itemCount: catalog.length,
          prototypeItem: ListTile(
            title: Text(catalog.firstOrNull?.title ?? 'loading'),
          ),
          itemBuilder: (context, index) {
            return ListTile(
              title: Text(catalog[index].title),
            );
          },
        ));
  }
}
