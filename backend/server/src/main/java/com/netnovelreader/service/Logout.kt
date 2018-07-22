package com.netnovelreader.service

import com.netnovelreader.Logout
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.Cookie
import io.ktor.locations.location
import io.ktor.response.respond
import io.ktor.routing.Route

fun Route.logout() {
    location<Logout> {
        authenticate {
            handle {
                val cookie = Cookie("name", "", path = "/", maxAge = 0)
                call.response.cookies.append(cookie)
                call.respond("退出登录")
            }
        }
    }
}