package com.netnovelreader.servlet

import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import java.io.File
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet("/upload")
class SaveRecord : BaseAuthorityServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        super.doGet(req, resp)
        if (role == null) {
            resp.writer.append("1").close()
            return
        }
        if (!ServletFileUpload.isMultipartContent(req)) return
        DiskFileItemFactory().apply { repository = File("./") }
            .let { ServletFileUpload(it) }.apply { sizeMax = 2000000 }
            .parseRequest(req)
            .takeIf { it.size == 1 }
            ?.forEach {
                try {
                    it.write(File(userService!!.getUserName(req.cookies)))
                    resp.writer.append("0").close()
                } catch (e: Exception) {
                    e.printStackTrace()
                    resp.writer.append("1").close()
                }
            } ?: run{ resp.writer.append("2").close() }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        doGet(req, resp)
    }
}