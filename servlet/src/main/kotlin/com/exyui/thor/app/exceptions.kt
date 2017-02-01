package com.exyui.thor.app

import com.exyui.thor.DEBUG
import java.io.IOException
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 1/23/17.
 */
class ForbiddenErr(msg: String): IOException(msg) {
    val code = HttpServletResponse.SC_FORBIDDEN
}

fun HttpServletResponse.forbidden(msg: String) {
    sendError(HttpServletResponse.SC_FORBIDDEN, msg)
}

fun HttpServletResponse.forbidden(msg: String, vararg debugInfo: String) {
    if (!DEBUG) {
        forbidden(msg)
    } else {
        var result = msg + "\n"
        debugInfo.forEach {
            result += it + "\n"
        }
        forbidden(result)
    }
}