package com.exyui.thor.core.ctrl

import com.exyui.thor.*
import com.exyui.thor.core.*
import com.exyui.thor.core.database.Comment
import com.exyui.thor.core.database.Thread
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4 as esc

/**
 * Created by yuriel on 1/15/17.
 */
object Controller {

    private val host = if (DEBUG) listOf(HOST_DEBUG) else HOST_RELEASE
    private val origin = if (DEBUG) listOf(ORIGIN_DEBUG) else ORIGIN_RELEASE

    private val log = Logger.getLogger(javaClass)

    init {
        BasicConfigurator.configure();
    }

    val info = mapOf(
            Pair("host", host),
            Pair("origin", origin),
            Pair("version", PACKAGE_VERSION),
            Pair("moderation", MODERATED)
    )

    @Throws(ThorBadRequest::class)
    fun insertComment(uri: String, title: String, author: String? = null, email: String? = null, website: String? = null, text: String, remoteAddr: String): Pair<Int, Comment> {
        val mode = if (MODERATED) 2 else 1

        val thread = if (uri !in Thread) {
            // todo: check title and uri by request
            val t = Thread.new(uri, title)
            log.info("comments.new:new-thread:${t.id}")
            t
        } else {
            Thread[uri]
        }

        val c = Comment.create(
                author = esc(author),
                email = esc(email),
                website = urlFor(website),
                text = esc(text),
                mode = mode,
                remoteAddr = remoteAddr
        )

        log.info("comments.new:before-save:${c.id}")
        val rv = c.insert(uri)
        log.info("comments.new:after-save:${rv.first}")
        return rv
    }

    @Throws(ThorNotFound::class)
    fun viewComment(id: Int) = Comment[id] ?: throw ThorNotFound("comment($id)")

    private fun urlFor(url: String?): String? {
        url?: return null
        if (url.startsWith("https://", true) || url.startsWith("http://", true)) return url
        return "http://$url"
    }

    fun deleteComment(id: Int) {
        Comment.delete(id)
    }
}