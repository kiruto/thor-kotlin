package com.exyui.thor.app

import javax.servlet.http.HttpServletRequest

/**
 * Created by yuriel on 1/23/17.
 */

/**
 * Check for CSRF on POST/PUT/DELETE using a <form> element and JS to execute automatically.
 *
 * When an attacker uses a <form> to downvote a comment, the browser *should*
 * add a `Content-Type: ...` header with value "application/octet-stream"
 * If the header is not sent or requests `application/octet-stream`, the request is
 * not forged (XHR is restricted by CORS separately).
 */
fun HttpServletRequest.xhr() {
    contentType?.let {
        if (it.isNotEmpty() && !it.startsWith("application/octet-stream"))
            throw ForbiddenErr("CSRF")
    }
}