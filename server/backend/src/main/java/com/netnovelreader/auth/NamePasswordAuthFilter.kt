package com.netnovelreader.auth

import com.netnovelreader.utils.JwtUtil
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class NamePasswordAuthFilter : UsernamePasswordAuthenticationFilter() {

    @Throws(AuthenticationException::class, IOException::class, ServletException::class)
    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication {
        val username = request?.getParameter("username")?.trim() ?: ""
        val password = request?.getParameter("password")?.trim() ?: ""
        val auth = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(auth)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        chain: FilterChain?,
        authResult: Authentication?
    ) {
        val username = (authResult?.principal as User?)?.username ?: ""

    }
}