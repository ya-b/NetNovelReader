package com.netnovelreader.servlet.rule

import com.google.gson.Gson
import com.netnovelreader.model.SitePreferenceJsonBean
import com.netnovelreader.service.SitePreferenceService
import com.netnovelreader.servlet.BaseAuthorityServlet
import com.netnovelreader.utils.getJsonChar
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/ImportSitePreference")
class ImportSitePreference : BaseAuthorityServlet() {
    @Throws(ServletException::class, IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doGet(req, resp)
        if (role != 100) {
            resp.sendRedirect("redirect.jsp")
            return
        }
        val sps = SitePreferenceService()
        req.reader.readText()
            .getJsonChar()
            ?.let {
                try {
                    Gson().fromJson(it, SitePreferenceJsonBean::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            ?.arr
            ?.forEach { sps.addPreference(it) }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}