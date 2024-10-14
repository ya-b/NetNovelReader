class Catalog {
  late int id;
  late String title;
  late String url;
  late int bookId;

  Catalog({
    required this.id,
    required this.title,
    required this.url,
    required this.bookId,
  });

  Map<String, Object?> toMap() {
    return {
      'id': id,
      'title': title,
      'url': url,
      'bookId': bookId,
    };
  }

  Catalog.fromMap(Map<String, dynamic> map) {
    id = map['id'];
    title = map['title'];
    url = map['url'];
    bookId = map['bookId'];
  }
}