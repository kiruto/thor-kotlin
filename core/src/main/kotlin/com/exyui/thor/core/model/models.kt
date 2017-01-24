package com.exyui.thor.core.model

import com.exyui.thor.core.database.Comment

/**
 * Created by yuriel on 1/22/17.
 */
data class FetchResult(val id: Int?, val totalReplies: Int, val hiddenReplies: Int, val replies: List<Comment>)
data class ResultWrapper(val code: Int, val msg: String, val d: Any)