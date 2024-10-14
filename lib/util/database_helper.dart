import 'dart:io';

import 'package:path/path.dart';
import 'package:sqflite_common_ffi/sqflite_ffi.dart';


class DatabaseHelper {
  // 单例
  static final DatabaseHelper _instance = DatabaseHelper._internal();
  factory DatabaseHelper() => _instance;

  // 私有的构造函数
  DatabaseHelper._internal();

  // 数据库实例
  static Database? _database;

  // 获取数据库实例
  Future<Database> get database async {
    if (_database != null) return _database!;

    // 如果数据库未初始化，先初始化数据库
    _database = await _initDatabase();
    return _database!;
  }

  // 初始化数据库
  Future<Database> _initDatabase() async {
    if (Platform.isWindows || Platform.isLinux || Platform.isMacOS) {
      sqfliteFfiInit();
      databaseFactory = databaseFactoryFfi;
    }
    await databaseFactory.deleteDatabase(join(await getDatabasesPath(), 'reader.db'));
    return await openDatabase(
      join(await getDatabasesPath(), 'reader.db'),
      version: 1,
      onCreate: _onCreate,
    );
  }

  // 初始化时创建表
  Future _onCreate(Database db, int version) async {
      await db.execute('CREATE TABLE book (id INTEGER PRIMARY KEY, name TEXT, url TEXT, latestUrl TEXT, catalogSelector TEXT, createTime DATETIME, updateTime DATETIME)');
      await db.execute('CREATE UNIQUE INDEX book_name ON book (name)');
      await db.execute('CREATE TABLE catalog (id INTEGER PRIMARY KEY, title TEXT, url TEXT, bookId INTEGER)');
      await db.execute('CREATE INDEX catalog_bookId ON catalog (bookId)');
      await db.execute('CREATE UNIQUE INDEX catalog_url ON catalog (url)');
      await db.execute('CREATE TABLE chapter (id INTEGER PRIMARY KEY, title TEXT, url TEXT, createTime DATETIME, content TEXT, catalogId INTEGER, bookId INTEGER)');
      await db.execute('CREATE INDEX chapter_bookId ON chapter (bookId)');
      await db.execute('CREATE UNIQUE INDEX chapter_catalogId ON chapter (catalogId)');
  }

}