package com.netnovelreader.servlet.rule

import com.netnovelreader.service.SitePreferenceService
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/AddSitePreference")
class AddSitePreference : BaseAuthorityServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doGet(req, resp)
        SitePreferenceService()
            .run { addPreference(getBean(req)) }
            .let {
                if (it) {
                    resp.sendRedirect("index.jsp")
                } else {
                    resp.sendRedirect("error.html")
                }
            }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doPost(req, resp)
        doGet(req, resp)
    }
}