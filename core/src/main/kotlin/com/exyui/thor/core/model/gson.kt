package com.exyui.thor.core.model

import com.exyui.thor.DEBUG
import com.exyui.thor.core.database.Comment
import com.google.gson.GsonBuilder
import kotlin.reflect.KClass

/**
 * Created by yuriel on 1/24/17.
 */
private val gsonBuilder by lazy {
    val result = GsonBuilder()
            .registerTypeAdapter(Comment::class.java, CommentTypeAdapter())
    if (DEBUG) result.setPrettyPrinting()
    result
}

val gson = gsonBuilder.create()

fun Comment.toJson() = gson.toJson(this)
fun String.createComment() = gson.fromJson(this, Comment::class.java)
fun String.createObject(clazz: KClass<*>) = gson.fromJson(this, clazz.java)