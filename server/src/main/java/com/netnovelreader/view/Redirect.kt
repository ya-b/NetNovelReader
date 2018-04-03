package com.netnovelreader.view

import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.lang.StringBuilder

class Redirect {
    fun getView() = StringBuilder().appendHTML().html {
        head {
            script {
                unsafe {
                    raw("""
                                var time = 4;
                                function returnUrlByTime() {
                                    window.setTimeout('returnUrlByTime()', 1000);
                                    time = time - 1;
                                    document.getElementById("time").innerHTML = "[ " + time + " ] 秒后跳转";
                                }
                            """)
                }
            }
        }
        body {
            onLoad = "returnUrlByTime()"
            text("redirect")
            div {
                id = "time"
            }
        }
    }
}