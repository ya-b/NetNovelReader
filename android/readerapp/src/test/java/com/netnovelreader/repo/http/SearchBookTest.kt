package com.netnovelreader.repo.http

import org.junit.Test
import java.util.regex.Pattern

class SearchBookTest {

    @Test
    fun getTIMEOUT() {
        val str = WebService.readerAPI
            .request("http://www.yunlaige.com/")
            .execute()
            .body()?.bytes() ?: "".toByteArray()
        val matcher = Pattern.compile("<meta(?!\\s*(?:name|value)\\s*=)[^>]*?charset\\s*=[\\s\"']*([^\\s\"'/>]*)", Pattern.CASE_INSENSITIVE)
            .matcher(String(str))
        if(matcher.find()) {
            println(matcher.group(1))
        }
    }
}