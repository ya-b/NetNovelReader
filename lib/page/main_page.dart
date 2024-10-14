import 'package:flutter/material.dart';
import 'package:logger/logger.dart';
import 'package:net_novel_reader/main.dart';
import 'package:net_novel_reader/page/catalog_page.dart';
import 'package:net_novel_reader/page/reader_page.dart';
import 'package:net_novel_reader/page/shelf_page.dart';
import 'package:provider/provider.dart';

var logger = Logger(
  printer: PrettyPrinter(),
);

class MainPage extends StatefulWidget {
  const MainPage({super.key});

  @override
  State<MainPage> createState() => _MainPageState();
}

class _MainPageState extends State<MainPage> {

  Widget selectPage(int index) {
    Widget page;
    switch (index) {
      case 0:
        page = const ShelfPage();
        break;
      case 1:
        page = const CatalogPage();
        break;
      case 2:
        page = const ReaderPage();
        break;
      default:
        throw UnimplementedError('no widget for $index');
    }
    return page;
  }


  @override
  Widget build(BuildContext context) {

    return Scaffold(
      body: Row(
        children: [
          SafeArea(
            child: NavigationRail(
              backgroundColor: const Color.fromARGB(255, 222, 222, 222),
              extended: true,
              destinations: const [
                NavigationRailDestination(
                  icon: Icon(Icons.home),
                  label: Text('书架'),
                ),
                NavigationRailDestination(
                  icon: Icon(Icons.article),
                  label: Text('目录'),
                ),
                NavigationRailDestination(
                  icon: Icon(Icons.auto_stories),
                  label: Text('阅读'),
                ),
              ],
              selectedIndex: Provider.of<MessageModel>(context, listen: false).sidebarIndex,
              onDestinationSelected: (value) {
                logger.i('selected: $value');
                setState(() {
                  Provider.of<MessageModel>(context, listen: false).updateSidebarIndex(value);
                });
              },
            ),
          ),
          Expanded(
            child: Container(
              color: Theme.of(context).colorScheme.primaryContainer,
              child: Consumer<MessageModel>(
                builder: (context, model, child) => selectPage(model.sidebarIndex),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
