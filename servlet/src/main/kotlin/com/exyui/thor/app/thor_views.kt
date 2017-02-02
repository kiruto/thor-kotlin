package com.exyui.thor.app

import com.exyui.thor.core.ThorNotFound
import com.exyui.thor.core.cache.ThorSession
import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.ctrl.anonymize
import com.exyui.thor.core.model.createArray
import com.exyui.thor.core.model.toJson
import com.exyui.thor.crypto.decrypt
import com.exyui.thor.crypto.encrypt
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND

/**
 * Created by yuriel on 1/23/17.
 * Thor views
 */

const val URL_INFO = "/thor/info"
@WebServlet(name = "Info", value = URL_INFO) class InfoView: HttpServlet() {
    override fun doGet(req: HttpServletRequest?, resp: HttpServletResponse) {
        resp.writer.write(Controller.info.toJson())
    }
}

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
        resq.stream()
        req.xhr()
                .parseEncrypted(NewCommentParameter::class)
                .execute(req.remoteAddr.anonymize())
                .encrypt()
                .send(resq)
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
        req.parse(NewCommentParameter::class)
                .execute(req.remoteAddr.anonymize())
                .send(resq)
    }
}

private fun NewCommentParameter.execute(remote: String): String {
    val insertResult = Controller.insertComment(uri, title, author, email, website, text, remote, parent)
    val id = insertResult.first
    val comment = insertResult.second
    val token = ThorSession.save(comment.id!!, remote, email)
    return NewCommentResult(id, comment, remote, token).toJson()
}

/**
 * GET: view comment
 * PUT: edit comment
 * DELETE: delete comment
 * POST: other actions
 */
const val URL_COMMENT = "/thor/id"
@WebServlet(name = "Comment", value = URL_COMMENT) class CommentView: HttpServlet() {

    /**
     * View a comment.
     *
     * param:
     *  id: Comment id
     */
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        req.xhr()
        resp.stream()
        try {
            Controller.viewComment(req.getParameter("id").toInt())
                    .toJson()
                    .send(resp)
        } catch (e: ThorNotFound) {
            resp.sendError(SC_NOT_FOUND)
        } catch (e: NumberFormatException) {
            resp.forbidden("Input id is NaN.")
        }
    }

    /**
     * Edit comment.
     *
     * param {@link EditCommentParameter}
     */
    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.stream()
        req.xhr()
                .parseEncrypted(EditCommentParameter::class)
                .execute(req.remoteAddr.anonymize(), resp)
                ?.encrypt()
                ?.send(resp)
    }

    /**
     * Delete comment.
     *
     * param {@link DeleteCommentParameter}
     */
    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.stream()
        req.xhr()
                .parseEncrypted(DeleteCommentParameter::class)
                .execute(resp)
                ?.encrypt()
                ?.send(resp)
    }
}

const val URL_COMMENT_DEBUG = "/thor/id/debug"
@WebServlet(name = "CommentDebug", value = URL_COMMENT_DEBUG) class CommentDebugView: HttpServlet() {

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        debugOrThrow()
        val id = req.getParameter("id").toInt()
        try {
            Controller.viewComment(id)
                    .toJson()
                    .send(resp)
        } catch (e: ThorNotFound) {
            resp.sendError(SC_NOT_FOUND)
        }
    }

    override fun doPut(req: HttpServletRequest, resp: HttpServletResponse) {
        debugOrThrow()
        req.parse(EditCommentParameter::class)
                .execute(req.remoteAddr, resp)
                ?.send(resp)
    }

    override fun doDelete(req: HttpServletRequest, resp: HttpServletResponse) {
        debugOrThrow()
        req.parse(DeleteCommentParameter::class)
                .execute(resp)
                ?.send(resp)
    }
}

private fun EditCommentParameter.execute(remote: String, resp: HttpServletResponse): String? {
    if (!ThorSession.check(id, session)) {
        resp.forbidden("bad signature", "needed: ${ThorSession[id]}", "gaven: $session")
        return null
    }
    return EditCommentResult(
            Controller.editComment(id, text, author, website),
            remote,
            ThorSession.renew(id, session)).toJson()
}

private fun DeleteCommentParameter.execute(resp: HttpServletResponse): String? {
    if (!ThorSession.check(id, session)) {
        resp.forbidden("bad signature", "needed: ${ThorSession[id]}", "gaven: $session")
        return null
    }
    return Controller.deleteComment(id)?.toJson()
}

/**
 * GET:
 *  param:
 *      p: json string of struct {@link FetchCommentParameter}
 */
const val URL_FETCH = "/thor"
@WebServlet(name = "Fetch", value = URL_FETCH) class FetchView: HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.stream()
        req.xhr()
                .getFetchParameter()
                .execute()
                .encrypt()
                .send(resp)
    }
}

const val URL_FETCH_DEBUG = "/thor/debug"
@WebServlet(name = "FetchDebug", value = URL_FETCH_DEBUG) class FetchDebugView: HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        req.getFetchParameter()
                .execute()
                .send(resp)
    }
}

private fun HttpServletRequest.getFetchParameter(): FetchCommentParameter {
    return FetchCommentParameter(
            getParameter("uri"),
            getParameter("after")?.toDouble(),
            getParameter("parent")?.toInt(),
            getParameter("limit")?.toInt(),
            getParameter("plain")?.toInt(),
            getParameter("nestedLimit")?.toInt()
    )
}

private fun FetchCommentParameter.execute(): String {
    val p = if (null == parent) -1 else if (0 == parent) null else parent
    return Controller.fetch(uri, after?: .0, p, limit, plain, nestedLimit).toJson()
}

/**
 * POST:
 *  param:
 *      id: (Int)comment id
 *      like: (Boolean) like or dislike
 */
const val URL_VOTE = "/thor/vote"
@WebServlet(name = "Vote", value = URL_VOTE) class VoteView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.stream()
        req.xhr()
                .parseEncrypted(VoteParameter::class)
                .execute(req)
                .encrypt()
                .send(resp)
    }
}

const val URL_VOTE_DEBUG = "/thor/vote/debug"
@WebServlet(name = "VoteDebug", value = URL_VOTE_DEBUG) class VoteDebugView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.parse(VoteParameter::class)
                .execute(req)
                .send(resp)
    }
}

private fun VoteParameter.execute(req: HttpServletRequest): String {
    return if (like) {
        Controller.like(id, req.remoteAddr.anonymize())
    } else {
        Controller.dislike(id, req.remoteAddr.anonymize())
    }.toJson()
}

const val URL_COUNTS = "/thor/counts"
@WebServlet(name = "Counts", value = URL_COUNTS) class CountsView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.stream()
        req.xhr()
                .getParameter("uri")
                .decrypt()
                .createArray<String>()
                .let { Controller.counts(*it) }
                .toJson()
                .encrypt()
                .send(resp)
    }
}

const val URL_COUNTS_DEBUG = "/thor/counts/debug"
@WebServlet(name = "Counts", value = URL_COUNTS_DEBUG) class CountsDebugView: HttpServlet() {
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        req.getParameter("uri")
                .createArray<String>()
                .let { Controller.counts(*it) }
                .toJson()
                .send(resp)
    }
}

/**
 * Append string to response body
 */
private infix fun String.send(resp: HttpServletResponse) {
    resp.writer.write(this)
}