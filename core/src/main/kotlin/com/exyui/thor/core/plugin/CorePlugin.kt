package com.exyui.thor.core.plugin

import com.exyui.thor.DEBUG
import com.exyui.thor.core.database.Comment
import com.exyui.thor.core.database.Thread
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout

/**
 * Created by yuriel on 1/19/17.
 */
internal object CorePlugin: OnNewComment {

    init {
        active(this)
    }

    private val log = Logger.getRootLogger()

    override fun onActivate() {
        val console = ConsoleAppender() //create appender
        //configure the appender
        val PATTERN = "%d [%p|%c|%C{1}] %m%n"
        console.layout = PatternLayout(PATTERN)
        if (DEBUG) {
            console.threshold = Level.DEBUG
        } else {
            console.threshold = Level.FATAL
        }
        console.activateOptions()
        //add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(console)
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