package com.exyui.thor.app

import com.exyui.thor.DEBUG
import com.exyui.thor.core.model.gson
import com.exyui.thor.crypto.decrypt
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

/**
 * The json data in GET request should be put into a wrapper with a key like `d`
 * POST request should not have a wrapper parameter like `d`, please send json data directly.
 */
fun <T: Any> HttpServletRequest.parse(clazz: KClass<T>, key: String = "d", encrypted: Boolean = false): T {
    return when(method) {
        "GET" -> {
            val content = if (encrypted) getParameter(key).decrypt() else getParameter(key)
            gson.fromJson(content, clazz.java)!!
        }
        else -> {
            val body = if (encrypted) reader.readText().decrypt() else reader.readText()
            gson.fromJson(body, clazz.java)!!
        }
    }
}

/**
 * An easy way to parse
 */
infix fun <T: Any> HttpServletRequest.parse(clazz: KClass<T>) = parse(clazz, "d")

/**
 * Decrypt and parse to @param <T> typed object.
 */
fun <T: Any> HttpServletRequest.parseEncrypted(clazz: KClass<T>, key: String = "d") = parse(clazz, key, true)

/**
 * An easy way to decrypt and parse
 */
infix fun <T: Any> HttpServletRequest.parseEncrypted(clazz: KClass<T>) = parseEncrypted(clazz, "d")

fun HttpServletResponse.stream() {
    contentType = "application/octet-stream"
}