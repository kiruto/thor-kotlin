package com.exyui.thor.app

import com.exyui.thor.core.cache.ThorSession
import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.ctrl.anonymize
import com.exyui.thor.core.model.toJson
import com.exyui.thor.crypto.decrypt
import com.exyui.thor.crypto.encrypt
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 1/23/17.
 */

const val URL_TEST_ENCRYPT = "/thor/encrypt/debug"
@WebServlet(name = "TestEncrypt", value = URL_TEST_ENCRYPT) class EncryptDebugView: HttpServlet(){
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        debugOrThrow()
        val content = req.getParameter("content")
        val key = req.getParameter("key")
        resp.writer.write(encrypt(content, key))
    }
}

const val URL_TEST_DECRYPT = "/thor/decrypt/debug"
@WebServlet(name = "TestDecrypt", value = URL_TEST_DECRYPT) class DecryptDebugView: HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        debugOrThrow()
        val content = req.getParameter("content")
        val key = req.getParameter("key")
        resp.writer.write(decrypt(content, key))
    }
}

const val URL_NEW_COMMENT = "/thor/new"
@WebServlet(name = "NewComment", value = URL_NEW_COMMENT) class NewCommentView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resq: HttpServletResponse) {
        req.xhr()
        resq.stream()
        resq.writer.write(newComment(req).encrypt())
    }
}

/**
 * request parameter:
 *  d: json string of struct {@link NewCommentParameter}
 */
const val URL_NEW_COMMENT_DEBUG = "/thor/new/debug"
@WebServlet(name = "NewCommentDebug", value = URL_NEW_COMMENT_DEBUG) class NewCommentDebugView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resq: HttpServletResponse) {
        debugOrThrow()
        doRequest(req, resq)
    }

    override fun doGet(req: HttpServletRequest, resq: HttpServletResponse) {
        debugOrThrow()
        doRequest(req, resq)
    }

    private fun doRequest(req: HttpServletRequest, resq: HttpServletResponse) {
        val result = newComment(req)
        resq.writer.write(result)
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