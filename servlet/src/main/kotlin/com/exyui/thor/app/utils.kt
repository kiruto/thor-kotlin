package com.exyui.thor.app

import com.exyui.thor.DEBUG
import com.exyui.thor.core.model.gson
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass

/**
 * Created by yuriel on 1/23/17.
 */

fun debugOrThrow() = if (DEBUG) DEBUG else throw ForbiddenErr("Release")

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

fun <T: Any> HttpServletRequest.parse(clazz: KClass<T>, key: String = "d") = gson.fromJson(getParameter(key), clazz.java)!!

infix fun <T: Any> HttpServletRequest.parse(clazz: KClass<T>) = parse(clazz, "d")

fun HttpServletResponse.stream() {
    contentType = "application/octet-stream"
}