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
        active(this)
    }

    override fun onActivate() {
        log.debug("core plugin activated")
    }

    override fun onDisable() {
        log.debug("core plugin disabled")
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
}