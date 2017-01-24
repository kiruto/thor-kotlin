package com.exyui.thor.core.plugin

import com.exyui.thor.core.database.Comment
import com.exyui.thor.core.database.Thread

/**
 * Created by yuriel on 1/18/17.
 */

interface Plugin {
    fun onActivate(part: String)
    fun onDisable(part: String)
}

const val PART_ON_NEW_COMMENT = "OnNewComment"
interface OnNewComment: Plugin {
    fun onNewThread(thread: Thread)
    fun beforeSave(thread: Thread, comment: Comment)
    fun afterSave(comment: Comment)
    fun finishSave(thread: Thread, comment: Comment)
}
fun OnNewComment.name() = PART_ON_NEW_COMMENT

const val PART_ON_EDIT_COMMENT = "OnEditComment"
interface OnEditComment: Plugin {
    fun onEdit(comment: Comment)
}
fun OnEditComment.name() = PART_ON_EDIT_COMMENT

const val PART_ON_DELETE_COMMENT = "OnDeleteComment"
interface OnDeleteComment: Plugin {
    fun onDelete(id: Int)
}
fun OnDeleteComment.name() = PART_ON_DELETE_COMMENT

fun Plugin.active() {
    Bus.addPlugin(this)
}

fun Plugin.deactive() {
    Bus.removePlugin(this)
}