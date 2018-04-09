package com.netnovelreader.servlet

import com.netnovelreader.service.UserAuthorityService
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class BaseAuthorityServlet : HttpServlet() {
    var role: Int? = null
    var userService: UserAuthorityService? = null
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "utf-8"
        resp.characterEncoding = "utf-8"
        userService = UserAuthorityService()
        role = userService!!.getRole(req.cookies)
        if (role == null) {
            req.session.apply {
                setAttribute("result", "请登陆")
                setAttribute("redirect", "index.jsp")
            }
        } else if (role != 100) {
            req.session.apply {
                setAttribute("result", "没有此权限")
                setAttribute("redirect", "index.jsp")
            }
        }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}