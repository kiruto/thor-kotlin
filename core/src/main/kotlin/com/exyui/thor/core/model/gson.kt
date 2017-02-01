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
            .registerTypeAdapter(Comment::class.java, CommentTypeAdapter)
            .registerTypeAdapter(FetchResult::class.java, FetchResultTypeAdapter)
    if (DEBUG) result.setPrettyPrinting()
    result
}

val gson = gsonBuilder.create()!!

fun Comment.toJson() = gson.toJson(this)!!
fun String.createComment(): Comment? = gson.fromJson(this, Comment::class.java)
fun <T: Any> String.createObject(clazz: KClass<T>) = gson.fromJson<T>(this, clazz.java)!!
fun Any.toJson() = gson.toJson(this)!!
fun Map<String, *>.toJson() = gson.toJson(this)!!