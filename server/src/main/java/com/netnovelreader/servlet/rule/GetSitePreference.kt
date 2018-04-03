package com.netnovelreader.servlet.rule

import com.netnovelreader.service.SitePreferenceService
import com.netnovelreader.utils.siteList2Json
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/GetSitePreference")
class GetSitePreference : HttpServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val str = SitePreferenceService().getPreference(req.getParameter("hostname")).let { siteList2Json(it) }
        resp.characterEncoding = "utf-8"
        resp.contentType = "application/json; charset=utf-8"
        resp.writer.append(str).close()
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}