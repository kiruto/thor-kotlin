package com.exyui.thor.app

import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 1/16/17.
 */
@WebServlet(name = "Test", value = "/thor")
class TestController : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resq: HttpServletResponse) {
        resq.writer.write("I am Thor!")
    }
}