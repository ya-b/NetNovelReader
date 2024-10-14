

import 'package:net_novel_reader/util/qianfan.dart';

class Gpt {

  Future<String> chat(dynamic body) async {
    return Future.value('');
  }
}

class GptUtil {
  static List<Gpt> models = [];

  static void initWithQianfan(String accessKey, String secretKey) {
    models.add(Qianfan(accessKey: accessKey, secretKey: secretKey));
  }

  static Future<String> chat(dynamic body) async {
    return await models[0].chat(body);
  }
}