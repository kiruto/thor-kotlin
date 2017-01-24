package com.exyui.thor.app

import com.exyui.thor.core.ctrl.Controller
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 1/23/17.
 */

@WebServlet(name = "Test", value = "/thor/new") class NewComment : HttpServlet() {
    override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
        req.xhr()

    }
}