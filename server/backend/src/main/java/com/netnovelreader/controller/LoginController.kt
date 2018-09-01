package com.netnovelreader.controller

import com.netnovelreader.database.mapper.UserMapper
import com.netnovelreader.service.UserService
import com.netnovelreader.vo.Result
import io.swagger.annotations.ApiOperation
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class LoginController {

    @Autowired
    lateinit var userService: UserService

    @RequestMapping("/login")
    @ApiOperation(value = "用户登录")
    fun login(request: HttpServletRequest, response: HttpServletResponse): Result {
        val username = request.getParameter("username")
        val password = request.getParameter("password")
        val subject = SecurityUtils.getSubject()
        val token = UsernamePasswordToken(username, password)
        try {
            subject.login(token)
        } catch (e: AuthenticationException) {
            return Result(-1, e.toString())
        }
        response.setHeader("x-auth-token", userService.generateJwtToken(username!!))
        return Result(0, "ok")
    }
}