package com.exyui.thor.app

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
