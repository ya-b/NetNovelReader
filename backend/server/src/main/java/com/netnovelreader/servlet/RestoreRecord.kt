package com.netnovelreader.servlet

import java.io.File
import java.io.IOException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/download")
class RestoreRecord : BaseAuthorityServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doGet(req, resp)
        if (role == null) {
            resp.writer.append("{\"ret\":1}").close()
            return
        }
        resp.characterEncoding = "utf-8"
        resp.contentType = "application/json; charset=utf-8"
        try {
            resp.outputStream.use { out ->
                out.print("{\"ret\":0,")
                File(userService!!.getUserName(req.cookies)).inputStream().use { it.copyTo(out) }
                out.print("}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            resp.writer.append("{\"ret\":2}").close()
        }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}