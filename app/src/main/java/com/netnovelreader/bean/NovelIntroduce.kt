package com.netnovelreader.bean

import java.io.Serializable

/**
 * 文件： NovelIntroduce
 * 描述：
 * 作者： YangJunQuan   2018-2-8.
 */

data class NovelIntroduce(

    /**
     * _id : 5a0d2f056de0ed3b0c7943c6
     * title : 猛兽横行
     * author : 嘿嘿嘿
     * longIntro : 猛兽横行，涂炭生灵，人类幸存者寥寥无几，几欲灭绝。
     * 人类必须死——这是世界意志对人类的愤怒审判，因为人类擅自触碰到了不该触碰的禁忌。
     * 但是，人类，永不为奴！
     * 曾让人类进化为人的力量再次觉醒，进击吧，人类！
     * majorCate : 玄幻
     * minorCate : 异界大陆
     * cover : /agent/http%3A%2F%2Fimg.1391.com%2Fapi%2Fv1%2Fbookcenter%2Fcover%2F1%2F2180509%2F2180509_a5e5d4b4231849279baa29cf101a34cc.jpg%2F
     * hiddenPackage : []
     * apptype : [0,1,2,4]
     * hasCopyright : true
     * buytype : 0
     * sizetype : -1
     * superscript :
     * currency : 0
     * contentType : txt
     * _le : false
     * allowMonthly : true
     * allowVoucher : true
     * allowBeanVoucher : true
     * hasCp : true
     * postCount : 0
     * latelyFollower : 1
     * followerCount : 0
     * wordCount : 804126
     * serializeWordCount : 0
     * retentionRatio : 0
     * updated : 2017-11-16T06:24:05.706Z
     * isSerial : true
     * chaptersCount : 275
     * lastChapter : 无
     * gender : ["male"]
     * tags : []
     * advertRead : true
     * cat : 异界大陆
     * rating : null
     * donate : false
     * _gg : false
     * discount : null
     * limit : false
     */

    var _id: String? = null,             //书名Id
    var title: String? = null,           //书名
    var author: String? = null,          //作者
    var longIntro: String? = null,       //书籍介绍
    var majorCate: String? = null,       //一级分类
    var minorCate: String? = null,       //二级分类
    var cover: String? = null,           //封面Url
    var isHasCopyright: Boolean = false,
    var buytype: Int = 0,
    var sizetype: Int = 0,
    var superscript: String? = null,
    var currency: Int = 0,
    var contentType: String? = null,
    var is_le: Boolean = false,
    var isAllowMonthly: Boolean = false,
    var isAllowVoucher: Boolean = false,
    var isAllowBeanVoucher: Boolean = false,
    var isHasCp: Boolean = false,
    var postCount: String? = null,
    var latelyFollower: String? = null,
    var followerCount: String? = null,
    var wordCount: String? = null,              //字数
    var serializeWordCount: String? = null,
    var retentionRatio: String? = null,
    var updated: String? = null,         //上次更新日期
    var isSerial: Boolean? = false,
    var chaptersCount: String? = null,
    var lastChapter: String? = null,
    var isAdvertRead: Boolean = false,
    var cat: String? = null,
    var rating: RatingBean? = RatingBean(
        -1,
        "暂无评分",
        false
    ),
    var isDonate: Boolean = false,
    var is_gg: Boolean = false,
    var discount: Any? = null,
    var isLimit: Boolean = false,
    var hiddenPackage: List<String>? = null,
    var apptype: List<Int>? = null,
    var gender: List<String>? = null,
    var tags: List<String>? = null
) : Serializable


data class RatingBean(
    /**
     * count : 27456
     * score : 9.219
     * isEffect : true
     */

    var count: Int = 0,
    var score: String = "暂无评分",
    var isIsEffect: Boolean = false
) : Serializable
