class Chapter {
  late int id;
  late String title;
  late String url;
  late DateTime createTime;
  late String content;
  late int catalogId;
  late int bookId;

  Chapter({
    required this.id,
    required this.title,
    required this.url,
    required this.createTime,
    required this.content,
    required this.catalogId,
    required this.bookId,
  });

  Map<String, Object?> toMap() {
    return {
      'id': id,
      'title': title,
      'url': url,
      'createTime': createTime,
      'content': content,
      'catalogId': catalogId,
      'bookId': bookId,
    };
  }

  Chapter.fromMap(Map<String, dynamic> map) {
    id = map['id'];
    title = map['title'];
    url = map['url'];
    url = map['url'];
    createTime = DateTime.fromMillisecondsSinceEpoch(map['createTime']);
    content = map['content'];
    catalogId = map['catalogId'];
    bookId = map['bookId'];
  }
}