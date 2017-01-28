package com.exyui.thor.app

import com.exyui.thor.core.database.Comment

/**
 * Created by yuriel on 1/26/17.
 */
data class NewCommentParameter(
        val uri: String,
        val title: String,
        val text: String,
        val author: String?,
        val email: String?,
        val website: String?,
        val parent: Int?
)
data class NewCommentResult(val id: Int, val comment: Comment, val ip: String, val token: String)

/**
 * param:
 *  session: Token encrypted user {@link Comment#user}
 *  id: comment id
 *  text:
 *  author:
 *  website:
 */
data class EditCommentParameter(
        val session: String,
        val id: Int,
        val text: String?,
        val author: String?,
        val website: String?
)

data class EditCommentResult(
        val comment: Comment, val ip: String, val token: String
)