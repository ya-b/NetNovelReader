package com.netnovelreader.servlet

import com.netnovelreader.service.UserAuthorityService
import javax.servlet.annotation.WebServlet
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/logout")
class Logout : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        if (UserAuthorityService().getRole(req.cookies) != null) {
            req.session.setAttribute("result", "退出成功")
        } else {
            req.session.setAttribute("result", "未曾登陆")
        }
        val cookie = Cookie("name", null).apply { path = "/" }
        cookie.setMaxAge(0)
        resp.addCookie(cookie)
        req.session.setAttribute("redirect", "index.jsp")
        resp.sendRedirect("redirect.jsp")
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}