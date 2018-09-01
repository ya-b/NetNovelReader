package com.netnovelreader.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import org.apache.shiro.authc.AuthenticationToken

object JwtUtil {

    private val EXPIRE_TIME = (5 * 60 * 1000).toLong()

    fun verify(token: String, username: String, issuer: String, audience: String, secret: String): Boolean {
        try {
            val verifier = JWT.require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", username)
                .build()
            val jwt = verifier.verify(token)
            return true
        } catch (exception: Exception) {
            return false
        }

    }

    fun getUsername(token: String): String? {
        try {
            val jwt = JWT.decode(token)
            return jwt.getClaim("username").asString()
        } catch (e: JWTDecodeException) {
            return null
        }
    }

    fun sign(username: String, issuer: String, audience: String, secret: String) = JWT.create()
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("username", username)
        .sign(Algorithm.HMAC256(secret))

    data class JwtToken(var token: String) : AuthenticationToken {

        override fun getCredentials() = token

        override fun getPrincipal() = token
    }
}