import 'package:flutter/material.dart';
import 'package:net_novel_reader/page/main_page.dart';
import 'package:net_novel_reader/util/gpt.dart';
import 'package:provider/provider.dart';

class MessageModel extends ChangeNotifier {
  int _sidebarIndex = 0;
  int _currentBookId = 0;

  int get sidebarIndex => _sidebarIndex;
  int get currentBookId => _currentBookId;

  void updateSidebarIndex(int sidebarIndex) {
    _sidebarIndex = sidebarIndex;
    notifyListeners();
  }

  void updateCurrentBookId(int currentBookId) {
    _currentBookId = currentBookId;
    notifyListeners();
  }
}

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  // GptUtil.initWithQianfan('', '');
  runApp(
    ChangeNotifierProvider(
      create: (context) => MessageModel(),
      child: const ReaderApp(),
    ),
  );
}

class ReaderApp extends StatelessWidget {
  const ReaderApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Novel Reader',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MainPage(),
    );
  }
}
