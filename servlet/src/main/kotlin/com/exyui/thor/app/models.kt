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

/**
 * param:
 *  newToken: IV where will be used when encrypt the old session for next request.
 */
data class EditCommentResult(
        val comment: Comment, val ip: String, val newToken: String
)

/**
 * param:
 *  session: Token encrypted user {@link Comment#user}
 *  id: comment id
 */
data class DeleteCommentParameter(
        val session: String,
        val id: Int
)

/**
 * param:
 *  parent:
 *      null: Don't care if comments have parent
 *      0: Comments don't have a parent
 *      any: Comments' parent
 */
data class FetchCommentParameter(
        val uri: String,
        val after: Double?,
        val parent: Int?,
        val limit: Int?,
        val plain: Int?,
        val nestedLimit: Int?
)

data class VoteParameter(val id: Int, val like: Boolean)