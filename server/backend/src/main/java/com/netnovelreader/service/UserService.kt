package com.netnovelreader.service

import com.netnovelreader.database.mapper.UserMapper
import com.netnovelreader.database.mapper.UserPopedomMapper
import com.netnovelreader.database.mapper.UserRoleMapper
import com.netnovelreader.utils.JwtUtil
import com.netnovelreader.utils.checkNotNullOrEmpty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.sql.SQLException

@Service
class UserService {

    @Autowired
    private lateinit var userMapper: UserMapper
    @Autowired
    private lateinit var userPopedomMapper: UserPopedomMapper
    @Autowired
    private lateinit var userRoleMapper: UserRoleMapper

    @Value("\${jwt.issuer}")
    private lateinit var issuer: String
    @Value("\${jwt.audience}")
    private lateinit var audience: String
    @Value("\${jwt.audience}")
    private lateinit var secret: String

    fun findUser(username: String) = userMapper.getUser(username)

    /**
     * 用户注册
     */
    fun registUser(username: String?, password: String?, email: String?) {
        checkNotNullOrEmpty(username, password, email)
        var result = userMapper.insert(username!!, password!!, email!!)
        if (result < 1) {
            throw SQLException("error occored on registUser")
        }
        //TODO 用户-角色 表
    }

    fun getPopedom(username: String) = userPopedomMapper.getUserPopedoms(username)

    fun getRole(username: String) = userRoleMapper.getRole(username)

    fun generateJwtToken(username: String) = JwtUtil.sign(username, issuer, audience, secret)
}