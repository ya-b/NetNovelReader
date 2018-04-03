package com.netnovelreader.servlet

import com.netnovelreader.service.UserAuthorityService
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
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
                setAttribute("redirect", "login.html")
            }
            resp.writer.appendHTML().html {

            }
            return
        }
        val username = req.getParameter("username")
        val passwd = req.getParameter("password")
        val result = service.loginOrRegister(username, passwd)

        when (result) {
            service.TEXT_IS_NULL -> req.session.apply {
                setAttribute("result", "用户名或密码长度需大于4")
                setAttribute("redirect", "login.html")
            }
            service.LOGIN_FAIL -> req.session.apply {
                setAttribute("result", "密码错误")
                setAttribute("redirect", "login.html")
            }
            service.LOGIN_SUCCESS -> {
                resp.addCookie(Cookie("name", "$username=|=$passwd").apply { path = "/" })
                req.session.apply {
                    setAttribute("result", "登陆成功")
                    setAttribute("redirect", "index.jsp")
                }
            }
            service.REGISTER_SUCCESS -> {
                resp.addCookie(Cookie("name", "$username=|=$passwd").apply { path = "/" })
                req.session.apply {
                    setAttribute("result", "注册成功")
                    setAttribute("redirect", "index.jsp")
                }
            }
        }
        resp.sendRedirect("redirect.jsp")
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}