package com.netnovelreader.auth

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http?.authorizeRequests()
            ?.anyRequest()
            ?.authenticated()
            ?.and()
            ?.antMatcher("/query")
            ?.anonymous()
            ?.and()
            ?.formLogin()
            ?.loginPage("/login")
            ?.permitAll()
            ?.and()
            ?.logout()
            ?.logoutUrl("/logout")
            ?.invalidateHttpSession(true)
            ?.deleteCookies("Authorization")
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        super.configure(auth)
    }
}