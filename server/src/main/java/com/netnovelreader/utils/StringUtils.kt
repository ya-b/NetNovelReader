package com.netnovelreader.utils

import com.netnovelreader.model.SitePreferenceBean

fun searchResult2Json(list: ArrayList<Array<String>>): String {
    if(list.isEmpty()) return "{\"arr\":[]}"
    val sb = StringBuilder()
    sb.append("{\n\"arr\":[\n")
    for (i in 0 until list.size){
        sb.append("{\n")
        sb.append("\"catalog\":\"${list[i][0]}\",\n")
        sb.append("\"bookname\":\"${list[i][1]}\",\n")
        sb.append("\"image\":\"${list[i][2]}\"\n")
        sb.append("}")
        if(i != list.size - 1){
            sb.append(",\n")
        }else{
            sb.append("\n")
        }
    }
    sb.append("]\n}")
    return sb.toString()
}

fun SitePreferenceBean.toJson(): String {
    val sb = StringBuilder()
    sb.append("{\n")
    sb.append("\"_id\":\"$_id\",\n")
    sb.append("\"hostname\":\"$hostname\",")
    sb.append("\"catalog_selector\":\"$catalog_selector\",\n")
    sb.append("\"chapter_selector\":\"$chapter_selector\",\n")
    sb.append("\"catalog_filter\":\"$catalog_filter\",\n")
    sb.append("\"chapter_filter\":\"$chapter_filter\",\n")
    sb.append("\"search_url\":\"$search_url\",\n")
    sb.append("\"redirect_fileld\":\"$redirect_fileld\",\n")
    sb.append("\"redirect_url\":\"$redirect_url\",\n")
    sb.append("\"no_redirect_url\":\"$no_redirect_url\",\n")
    sb.append("\"redirect_name\":\"$redirect_name\",\n")
    sb.append("\"no_redirect_name\":\"$no_redirect_name\",\n")
    sb.append("\"redirect_image\":\"$redirect_image\",\n")
    sb.append("\"no_redirect_image\":\"$no_redirect_image\",\n")
    sb.append("\"charset\":\"$charset\"\n")
    sb.append("}\n")
    return sb.toString()
}

fun siteList2Json(list: List<SitePreferenceBean>?): String {
    return if(list == null){
        "{}"
    }else{
        val sb = StringBuilder()
        sb.append("{\n\"arr\": [\n")
        for(i in 0 until list.size){
            sb.append(list[i].toJson())
            if(i != list.size - 1){
                sb.append(",\n")
            }else{
                sb.append("\n")
            }
        }
        sb.append("]\n}")
        sb.toString()
    }
}