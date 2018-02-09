package com.netnovelreader.api.bean

/**
 * 文件： QuerySuggest
 * 描述：
 * 作者： YangJunQuan   2018/2/5.
 */

data class QuerySuggest(var isOk: Boolean = false,
                        var keywords: List<KeywordsBean>? = null)

/**
 * keywords : [{"text":"腹黑","tag":"tag"},{"text":"烽火戏诸侯","tag":"bookauthor","contentType":"txt"},{"text":"凤回巢","tag":"bookname","id":"5836323138bcc1c615e5832b","author":"寻找失落的爱情","contentType":"txt"},{"text":"符皇","tag":"bookname","id":"531609b439c7575d6301e494","author":"萧瑾瑜","contentType":"txt"},{"text":"腹黑双胞胎：抢个总裁做爹地","tag":"bookname","id":"57b87b832bf5b5e77a3d873b","author":"时今","contentType":"txt"},{"text":"锋行天下","tag":"bookname","id":"587a3ccb47b2bfcf4640070c","author":"静物JW","contentType":"txt"},{"text":"烽皇","tag":"bookname","id":"57ba7e3459af2d534596196e","author":"瑞根","contentType":"txt"},{"text":"腹黑老公宠小妻","tag":"bookname","id":"59fc19132655af4561c37727","author":"落落","contentType":"txt"},{"text":"腹黑老公溺宠：老婆不准躲","tag":"bookname","id":"5637429990a0daa642fa515a","author":"望月存雅","contentType":"txt"},{"text":"凤凰错：替嫁弃妃","tag":"bookname","id":"51bebc7353e597de28000199","author":"阿彩","contentType":"txt"}]
 * ok : true
 */


data class KeywordsBean(var text: String? = null,
                        var contentType: String? = null)
/**
 * text : 腹黑
 * tag : tag
 * contentType : txt
 * id : 5836323138bcc1c615e5832b
 * author : 寻找失落的爱情
 */