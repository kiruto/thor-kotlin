package com.exyui.thor.app

import com.exyui.thor.core.database.Comment
import com.exyui.thor.core.database.Thread
import com.exyui.thor.core.plugin.OnNewComment
import com.exyui.thor.core.plugin.active
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Created by yuriel on 1/19/17.
 */
internal object ViewPlugin : OnNewComment {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    init {
        active()
    }

    override fun onActivate(part: String) {
        log.debug("core plugin: $part activated")
    }

    override fun onDisable(part: String) {
        log.debug("core plugin: $part disabled")
    }

    override fun onNewThread(thread: Thread) {
        log.debug("new thread: $thread")
    }

    override fun beforeSave(thread: Thread, comment: Comment) {
        log.debug("start-save: $comment")
        log.debug("at: $thread")
    }

    override fun afterSave(comment: Comment) {
        log.debug("saved: $comment")
    }

    override fun finishSave(thread: Thread, comment: Comment) {
        log.debug("saved: $comment")
        log.debug("at: $thread")
    }
}