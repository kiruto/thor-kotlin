package com.exyui.thor.app

import com.exyui.thor.core.cache.ThorSession
import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.ctrl.anonymize
import com.exyui.thor.core.model.toJson
import com.exyui.thor.crypto.encrypt
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 1/23/17.
 */

@WebServlet(name = "NewComment", value = "/thor/new") class NewComment: HttpServlet() {
    override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
        req.xhr()
        res.stream()
        res.writer.write(newComment(req).encrypt())
    }
}

@WebServlet(name = "NewCommentDebug", value = "/thor/new/debug") class NewCommentDebug: HttpServlet() {
    override fun doPost(req: HttpServletRequest, res: HttpServletResponse) {
        debugOrThrow()
        doRequest(req, res)
    }

    override fun doGet(req: HttpServletRequest, res: HttpServletResponse) {
        debugOrThrow()
        doRequest(req, res)
    }

    private fun doRequest(req: HttpServletRequest, res: HttpServletResponse) {
        val result = newComment(req)
        res.writer.write(result)
    }
}

private fun newComment(req: HttpServletRequest): String {
    val remote = req.remoteAddr.anonymize()
    val param = req parse NewCommentParameter::class
    val insertResult = param.let {
        Controller.insertComment(it.uri, it.title, it.author, it.email, it.website, it.text, remote, it.parent)
    }
    val id = insertResult.first
    val comment = insertResult.second
    val token = ThorSession.save(comment.id!!, remote, param.email)
    return NewCommentResult(id, comment, remote, token).toJson()
}