import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:net_novel_reader/util/gpt.dart';
import 'package:intl/intl.dart';
import 'package:http/http.dart' as http;

class Qianfan extends Gpt {
  final String accessKey;
  final String secretKey;
  final String? authVersion;
  final int? expirationInSeconds;

  Qianfan({
    required this.accessKey,
    required this.secretKey,
    this.authVersion = '1',
    this.expirationInSeconds = 1800,
  });

  (String, String) generateCanonicalHeaders(Map<String, String> headers) {
    List<String> defaultHeaders = [
      "host",
      "content-length",
      "content-type",
      "content-md5"
    ];
    List<String> keyStrList = headers.entries
        .map((ele) => ele.key.toLowerCase())
        .where((key) =>
            defaultHeaders.contains(key.toLowerCase()) ||
            key.toLowerCase().startsWith("x-bce-"))
        .toList();
    keyStrList.sort();
    List<String> usedHeaderStrList = keyStrList
        .map((key) =>
            '${Uri.encodeComponent(key)}:${Uri.encodeComponent(headers[key]!.trim())}')
        .toList();
    return (keyStrList.join(';'), usedHeaderStrList.join('\n'));
  }

  String generateAuthorization(
      Uri url, String method, Map<String, String> headers) {
    String currentTimestamp = DateFormat('yyyy-MM-dd\'T\'HH:mm:ss\'Z\'')
        .format(DateTime.now().toUtc());
    String signingKeyStr =
        "bce-auth-v$authVersion/$accessKey/$currentTimestamp/$expirationInSeconds";
    var signingKey = Hmac(sha256, utf8.encode(secretKey))
        .convert(utf8.encode(signingKeyStr))
        .toString();
    var (signedHeaderStr, canonicalHeaders) = generateCanonicalHeaders(headers);
    var canonicalRequests = <String>[];
    canonicalRequests.add(method.toUpperCase());
    canonicalRequests.add(url.path);
    canonicalRequests.add(url.query);
    canonicalRequests.add(canonicalHeaders);
    String canonicalRequest = canonicalRequests.join('\n');
    var signature = Hmac(sha256, utf8.encode(signingKey))
        .convert(utf8.encode(canonicalRequest))
        .toString();
    return "$signingKeyStr/$signedHeaderStr/$signature";
  }

  @override
  Future<String> chat(dynamic body) async {
    var url = Uri.parse(
        'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_speed');
    var method = 'POST';
    var request = http.Request(method, url);
    request.headers.addAll({
      'host': url.host,
      'content-type': 'application/json',
      'x-bce-date': DateFormat('yyyy-MM-dd\'T\'HH:mm:ss\'Z\'')
          .format(DateTime.now().toUtc())
    });
    request.body = jsonEncode(body);
    request.headers.addAll(
        {'Authorization': generateAuthorization(url, method, request.headers)});
    var response = await request.send();
    var responseBody = response.stream.transform(utf8.decoder).join();
    return responseBody;
  }
}
