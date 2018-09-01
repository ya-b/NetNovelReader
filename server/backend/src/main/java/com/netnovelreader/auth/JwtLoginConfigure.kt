package com.netnovelreader.auth

import com.netnovelreader.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy
import org.springframework.stereotype.Component

@Component
class JwtLoginConfigure<T : JwtLoginConfigure<T, B>, B : HttpSecurityBuilder<B>> : AbstractHttpConfigurer<T, B>() {
    @Autowired
    private lateinit var userService: UserService
    @Value("\${jwt.issuer}")
    private lateinit var issuer: String
    @Value("\${jwt.audience}")
    private lateinit var audience: String
    @Value("\${jwt.audience}")
    private lateinit var secret: String

    val authFilter = NamePasswordAuthFilter()

    override fun configure(builder: B) {
        authFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager::class.java))
        authFilter.setAuthenticationSuccessHandler(JwtLoginSuccessHandler())
        authFilter.setAuthenticationFailureHandler(JwtLoginFailureHandler())
        authFilter.setSessionAuthenticationStrategy(NullAuthenticatedSessionStrategy())
        builder.addFilterAfter(postProcess(authFilter), LogoutFilter::class.java)
    }
}