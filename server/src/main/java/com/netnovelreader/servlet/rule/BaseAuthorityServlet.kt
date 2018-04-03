package com.netnovelreader.servlet.rule

import com.netnovelreader.service.UserAuthorityService
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class BaseAuthorityServlet : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        req.characterEncoding = "utf-8"
        resp.characterEncoding = "utf-8"
        val role = UserAuthorityService().getRole(req.cookies)
        if (role == null) {
            req.session.apply {
                setAttribute("result", "请登陆")
                setAttribute("redirect", "login.html")
            }
        } else if (role != 100) {
            req.session.apply {
                setAttribute("result", "没有此权限")
                setAttribute("redirect", "index.jsp")
            }
        }
        resp.sendRedirect("redirect.jsp")
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}