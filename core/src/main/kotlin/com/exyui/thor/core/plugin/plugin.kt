package com.exyui.thor.core.plugin

import com.exyui.thor.core.database.Comment
import com.exyui.thor.core.database.Thread

/**
 * Created by yuriel on 1/18/17.
 */

interface Plugin {
    fun onActivate()
    fun onDisable()
}

interface OnNewComment: Plugin {
    fun onNewThread(thread: Thread)
    fun beforeSave(thread: Thread, comment: Comment)
    fun afterSave(comment: Comment)
}

fun active(plugin: Plugin) {
    Bus.addPlugin(plugin)
}