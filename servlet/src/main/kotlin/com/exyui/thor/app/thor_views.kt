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

/**
 * Check encrypt proto for debug
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

/**
 * Check decrypt proto for debug
 */
const val URL_TEST_DECRYPT = "/thor/decrypt/debug"
@WebServlet(name = "TestDecrypt", value = URL_TEST_DECRYPT) class DecryptDebugView: HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        debugOrThrow()
        val content = req.getParameter("content")
        val key = req.getParameter("key")
        resp.writer.write(decrypt(content, key))
    }
}

/**
 * Encrypted version of {@link #NewCommentDebugView}
 */
const val URL_NEW_COMMENT = "/thor/new"
@WebServlet(name = "NewComment", value = URL_NEW_COMMENT) class NewCommentView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resq: HttpServletResponse) {
        req.xhr()
        resq.stream()
        resq.writer.write(
                newComment(
                        req parseEncrypted NewCommentParameter::class,
                        req.remoteAddr.anonymize()
                ).encrypt()
        )
    }
}

/**
 * GET request parameter:
 *  d: json string of struct {@link NewCommentParameter}
 * POST request should not have a wrapper parameter like `d`, please send json data directly.
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
        val remote = req.remoteAddr.anonymize()
        val param = req parse NewCommentParameter::class
        val result = newComment(param, remote)
        resq.writer.write(result)
    }
}

private fun newComment(param: NewCommentParameter, remote: String): String {
    val insertResult = param.let {
        Controller.insertComment(it.uri, it.title, it.author, it.email, it.website, it.text, remote, it.parent)
    }
    val id = insertResult.first
    val comment = insertResult.second
    val token = ThorSession.save(comment.id!!, remote, param.email)
    return NewCommentResult(id, comment, remote, token).toJson()
}

/**
 * GET: view comment
 * PUT: edit comment
 * DELETE: delete comment
 * POST: other actions
 */
const val URL_EDIT_COMMENT = "/thor/id"
@WebServlet(name = "EDIT_COMMENT", value = URL_EDIT_COMMENT) class CommentView: HttpServlet() {
    override fun doPut(req: HttpServletRequest?, resp: HttpServletResponse?) {

    }
}