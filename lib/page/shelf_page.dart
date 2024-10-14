import 'package:flutter/material.dart';
import 'package:net_novel_reader/data/book.dart';
import 'package:net_novel_reader/main.dart';
import 'package:net_novel_reader/util/database_helper.dart';
import 'package:net_novel_reader/util/parse_catalog.dart';
import 'package:provider/provider.dart';

class ShelfPage extends StatefulWidget {
  const ShelfPage({super.key});

  @override
  State<ShelfPage> createState() => _ShelfPageState();
}

class _ShelfPageState extends State<ShelfPage> with TickerProviderStateMixin {
  var books = <Book>[];
  bool isLoading = false;
  final textController = TextEditingController();

  late final CatalogParser catalogParser;
  final dbHelper = DatabaseHelper();

  _ShelfPageState() {
    catalogParser = CatalogParser();
  }

  void refreshBooks() async {
    var db = await dbHelper.database;
    List<Map<String, Object?>> catalogMaps = await db.query('book');
    setState(() {
      books = [for (var map in catalogMaps) Book.fromMap(map)];
    });
  }

  @override
  void initState() {
    super.initState();
    refreshBooks();
  }

  @override
  void dispose() {
    textController.dispose();
    super.dispose();
  }

  void toast(String text) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(text),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Row(
            children: [
              const Text('书架'),
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
          actions: [
            IconButton(
              icon: const Icon(Icons.add_circle_outline),
              onPressed: () => showDialog<String>(
                context: context,
                builder: (BuildContext context) => Dialog(
                  child: Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: <Widget>[
                        const Text('添加书籍'),
                        const SizedBox(height: 15),
                        SizedBox(
                          width: 400,
                          child: TextField(
                            controller: textController,
                            decoration: const InputDecoration(
                              // border: OutlineInputBorder(),
                              labelText: '章节目录url',
                            ),
                          ),
                        ),
                        const SizedBox(height: 15),
                        TextButton(
                          onPressed: () async {
                            Navigator.pop(context);
                            var url = textController.text.trim();
                            if (!url.startsWith('http')) {
                              toast('地址格式错误');
                              return;
                            }
                            textController.clear();
                            setState(() {
                              isLoading = true;
                            });
                            var (name, catalogSelector, catalog) =
                                await catalogParser.getNameAndCatalog(url);
                            var db = await dbHelper.database;
                            List<Map<String, Object?>> books = await db.query(
                                'book',
                                where: 'name = ?',
                                whereArgs: [name]);
                            if (books.isNotEmpty) {
                              toast('该小说已添加');
                            } else {
                              var now = DateTime.now().millisecondsSinceEpoch;
                              int bookId = await db.insert('book', {
                                'name': name,
                                'url': url,
                                'latestUrl': url,
                                'catalogSelector': catalogSelector,
                                'createTime': now,
                                'updateTime': now
                              });
                              refreshBooks();
                              await db.transaction((txn) async {
                                for (var item in catalog.entries) {
                                  txn.insert('catalog', {'title': item.key, 'url': item.value, 'bookId': bookId});
                                }
                              });
                            }
                            setState(() {
                              isLoading = false;
                            });
                          },
                          child: const Text('Add'),
                        ),
                        TextButton(
                          onPressed: () {
                            Navigator.pop(context);
                            textController.clear();
                          },
                          child: const Text('Cancel'),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
        body: ListView.builder(
          itemCount: books.length,
          prototypeItem: ListTile(
            title: Text(books.firstOrNull?.name ?? 'loading'),
          ),
          itemBuilder: (context, index) {
            return ListTile(
                title: Text(books[index].name),
                onTap: () {
                  Provider.of<MessageModel>(context, listen: false).updateSidebarIndex(1);
                  Provider.of<MessageModel>(context, listen: false).updateCurrentBookId(books[index].id);
                },
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    IconButton(
                        icon: const Icon(Icons.delete),
                        onPressed: () => showDialog<String>(
                              context: context,
                              builder: (BuildContext context) => Dialog(
                                child: Padding(
                                  padding: const EdgeInsets.all(8.0),
                                  child: Column(
                                    mainAxisSize: MainAxisSize.min,
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: <Widget>[
                                      Text('是否删除书籍《${books[index].name}》?'),
                                      const SizedBox(height: 15),
                                      TextButton(
                                        onPressed: () async {
                                          Navigator.pop(context);
                                          var db = await dbHelper.database;
                                          await db.delete('book',
                                                  where: 'id = ?',
                                                  whereArgs: [books[index].id]);
                                          await db.delete('catalog',
                                                  where: 'bookId = ?',
                                                  whereArgs: [books[index].id]);
                                          await db.delete('chapter',
                                                  where: 'bookId = ?',
                                                  whereArgs: [books[index].id]);
                                          refreshBooks();
                                        },
                                        child: const Text('yes'),
                                      ),
                                      TextButton(
                                        onPressed: () {
                                          Navigator.pop(context);
                                        },
                                        child: const Text('no'),
                                      ),
                                    ],
                                  ),
                                ),
                              ),
                            )),
                  ],
                ));
          },
        ));
  }
}
