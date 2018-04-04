package com.netnovelreader.template

import io.ktor.html.Placeholder
import io.ktor.html.Template
import io.ktor.html.insert
import kotlinx.html.*

class BaseTemplate : Template<HTML> {
    val content = Placeholder<HtmlBlockTag>()

    override fun HTML.apply() {
        head {
            title { + "NetNovelReader Server" }
        }
        body {
            insert(content)
        }
    }
}