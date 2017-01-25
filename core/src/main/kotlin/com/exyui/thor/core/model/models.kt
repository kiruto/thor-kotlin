package com.exyui.thor.core.model

import com.exyui.thor.core.database.Comment

/**
 * Created by yuriel on 1/22/17.
 */
data class FetchResult(var id: Int? = null, var totalReplies: Int = 0, var hiddenReplies: Int = 0, val replies: MutableList<Comment> = mutableListOf<Comment>())
data class ResultWrapper<T>(var code: Int, var msg: String, var d: T?)