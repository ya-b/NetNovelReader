package com.netnovelreader.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.netnovelreader.Login
import com.netnovelreader.db.UserDao
import com.netnovelreader.model.UserBean
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.locations.location
import io.ktor.request.header
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.method

fun Route.login(issuer: String, audience: String, secret: String, dao: UserDao) {
    location<Login> {
        method(HttpMethod.Get) {
            handle {
                if (call.request.header("Authorization").isNullOrEmpty()) {
                    call.respondRedirect("login.html")
                } else {
                    call.respondRedirect("index.html")
                }
            }
        }
        method(HttpMethod.Post) {
            handle {
                val parameters = call.receiveParameters()
                val name = parameters["username"] ?: ""
                val passwd = parameters["password"] ?: ""
                var user = dao.getUser(name)
                when {
                    name.length < 4 || passwd.length < 4 -> call.respond("")
                    user != null && user.password != passwd -> call.respond("")
                    else -> {
                        if(user == null) {
                            user = UserBean().apply {
                                username = name
                                password = passwd
                                role = 1
                            }
                            dao.addUser(user)
                        }
                        val token = makeToken(user, issuer, audience, secret)
                        call.response.cookies.append("username", "${user.username}")
                        call.respond(token)
                    }
                }
            }
        }
    }
}

fun makeToken(user: UserBean, issuer: String, audience: String, secret: String) = JWT.create()
    .withSubject("Authentication")
    .withIssuer(issuer)
    .withAudience(audience)
    .withClaim("username", user.username)
    .withClaim("role", user.role)
    .sign(Algorithm.HMAC256(secret))