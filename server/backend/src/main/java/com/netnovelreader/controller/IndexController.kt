package com.netnovelreader.controller

import com.netnovelreader.database.entity.User
import com.netnovelreader.database.mapper.UserMapper
import io.swagger.annotations.ApiOperation
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IndexController {

    @Autowired
    var userMapper: UserMapper? = null

    @RequestMapping("/index")
    @ApiOperation(value = "主页")
    fun index(): String {

        return "index"
    }
}