package com.netnovelreader.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.netnovelreader.Login
import com.netnovelreader.db.User
import com.netnovelreader.db.Users
import io.ktor.application.call
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.location
import io.ktor.request.header
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.method
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.login(issuer: String, audience: String, secret: String) {
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
                var user: User? = null
                transaction {
                    user = User.find { Users.username.eq(name) }.firstOrNull()
                }
                when {
                    name.length < 4 || passwd.length < 4 ->
                        call.respond(HttpStatusCode.Forbidden, "长度不能小于4")
                    user != null && user?.password != passwd ->
                        call.respond(HttpStatusCode.Forbidden, "密码错误")
                    else -> {
                        user ?: User.new {
                            username = name
                            password = passwd
                            role = 1
                        }.also { user = it }
                        val token = makeToken(user!!, issuer, audience, secret)
                        call.response.cookies.append("username", user!!.username)
                        call.respond(token)
                    }
                }
            }
        }
    }
}

fun makeToken(user: User, issuer: String, audience: String, secret: String) = JWT.create()
    .withSubject("Authentication")
    .withIssuer(issuer)
    .withAudience(audience)
    .withClaim("username", user.username)
    .withClaim("role", user.role)
    .sign(Algorithm.HMAC256(secret))