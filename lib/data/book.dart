class Book {
  late int id;
  late String name;
  late String url;
  late String latestUrl;
  late String catalogSelector;
  late DateTime createTime;
  late DateTime updateTime;

  Book({
    required this.id,
    required this.name,
    required this.url,
    required this.latestUrl,
    required this.catalogSelector,
    required this.createTime,
    required this.updateTime,
  });

  Map<String, Object?> toMap() {
    return {
      'id': id,
      'name': name,
      'url': url,
      'latestUrl': latestUrl,
      'catalogSelector': catalogSelector,
      'createTime': createTime,
      'updateTime': updateTime,
    };
  }

  Book.fromMap(Map<String, dynamic> map) {
    id = map['id'];
    name = map['name'];
    url = map['url'];
    latestUrl = map['latestUrl'];
    catalogSelector = map['catalogSelector'];
    createTime = DateTime.fromMillisecondsSinceEpoch(map['createTime']);
    updateTime = DateTime.fromMillisecondsSinceEpoch(map['updateTime']);
  }
}