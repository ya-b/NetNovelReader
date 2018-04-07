package com.netnovelreader.servlet

import com.netnovelreader.service.UserAuthorityService
import javax.servlet.annotation.WebServlet
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/login")
class Login : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val service = UserAuthorityService()
        if (service.getRole(req.cookies) != null) {
            req.session.apply {
                setAttribute("result", "已经登陆，请退出再试")
                setAttribute("redirect", "index.jsp")
            }
            resp.sendRedirect("redirect.jsp")
            return
        }
        val username = req.getParameter("username")
        val passwd = req.getParameter("password")
        val result = service.loginOrRegister(username, passwd)

        when (result) {
            service.LOGIN_SUCCESS -> {
                resp.addCookie(Cookie("name", "$username=|=$passwd").apply { path = "/" })
                resp.writer.append("0").close()
            }
            service.REGISTER_SUCCESS -> {
                resp.addCookie(Cookie("name", "$username=|=$passwd").apply { path = "/" })
                resp.writer.append("1").close()
            }
            service.TEXT_IS_NULL -> req.session.apply {
                resp.writer.append("2").close()
            }
            service.LOGIN_FAIL -> req.session.apply {
                resp.writer.append("3").close()
            }
        }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}