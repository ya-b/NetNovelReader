package com.netnovelreader.auth

import com.netnovelreader.database.mapper.UserRoleMapper
import com.netnovelreader.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class JwtUserService : UserDetailsService {

    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    override fun loadUserByUsername(username: String?): UserDetails {
        val user = username?.let { userService.findUser(it) } ?: throw UsernameNotFoundException("不存在此用户")
        return User.builder()
            .username(username)
            .password(user.password)
            .roles(username.let { userRoleMapper.getRole(it) })
            .build()
    }
}