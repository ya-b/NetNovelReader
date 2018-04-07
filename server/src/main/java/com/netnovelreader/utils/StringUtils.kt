package com.netnovelreader.utils

import com.netnovelreader.model.SitePreferenceBean

fun searchResult2Json(list: ArrayList<Array<String>>): String {
    if (list.isEmpty()) return "{\"arr\":[]}"
    val sb = StringBuilder()
    sb.append("{\n\"arr\":[\n")
    for (i in 0 until list.size) {
        sb.append("{\n")
        sb.append("\"catalog\":\"${list[i][0]}\",\n")
        sb.append("\"bookname\":\"${list[i][1]}\",\n")
        sb.append("\"image\":\"${list[i][2]}\"\n")
        sb.append("}")
        if (i != list.size - 1) {
            sb.append(",\n")
        } else {
            sb.append("\n")
        }
    }
    sb.append("]\n}")
    return sb.toString()
}

fun SitePreferenceBean.toJson(): String {
    val sb = StringBuilder()
    sb.append("{\n")
    sb.append("\"h\":\"$hostname\",\n")
    sb.append("\"cs\":\"$catalog_selector\",\n")
    sb.append("\"hs\":\"$chapter_selector\",\n")
    sb.append("\"cf\":\"$catalog_filter\",\n")
    sb.append("\"hf\":\"$chapter_filter\",\n")
    sb.append("\"su\":\"$search_url\",\n")
    sb.append("\"rf\":\"$redirect_fileld\",\n")
    sb.append("\"ru\":\"$redirect_url\",\n")
    sb.append("\"nu\":\"$no_redirect_url\",\n")
    sb.append("\"rn\":\"$redirect_name\",\n")
    sb.append("\"nn\":\"$no_redirect_name\",\n")
    sb.append("\"ri\":\"$redirect_image\",\n")
    sb.append("\"ni\":\"$no_redirect_image\",\n")
    sb.append("\"ct\":\"$charset\"\n")
    sb.append("}\n")
    return sb.toString()
}

fun siteList2Json(list: List<SitePreferenceBean>?): String {
    return if (list == null) {
        "{}"
    } else {
        val sb = StringBuilder()
        sb.append("{\n\"arr\": [\n")
        for (i in 0 until list.size) {
            sb.append(list[i].toJson())
            if (i != list.size - 1) {
                sb.append(",\n")
            } else {
                sb.append("\n")
            }
        }
        sb.append("]\n}")
        sb.toString()
    }
}

fun String.getJsonChar(): String? {
    if (!this.contains("{")) return null
    if (this.split("{").size != this.split("}").size) return null
    return substring(indexOf("{"), lastIndexOf("}") + 1)
}