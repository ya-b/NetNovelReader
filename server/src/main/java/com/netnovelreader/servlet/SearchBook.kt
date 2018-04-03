package com.netnovelreader.servlet

import com.netnovelreader.service.SearchBookService
import com.netnovelreader.utils.searchResult2Json
import kotlinx.html.body
import kotlinx.html.h3
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/search")
class SearchBook : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val bookname = req.getParameter("bookname")
        if (bookname.isNullOrEmpty()) {
            resp.writer.appendHTML().html {
                body {
                    h3 {
                        text("Error")
                    }
                }
            }
        } else {
            resp.contentType = "application/json; charset=utf-8"
            SearchBookService().search(bookname).let { searchResult2Json(it) }.let {  resp.writer.append(it).close() }
        }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doPost(req, resp)
        doGet(req, resp)
    }
}