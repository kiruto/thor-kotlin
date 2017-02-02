package com.exyui.thor.core.model

import com.exyui.thor.DEBUG
import com.exyui.thor.core.database.Comment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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

/**
 * Convert a comment to json string
 */
fun Comment.toJson() = gson.toJson(this)!!

/**
 * Create a comment from json string
 */
fun String.createComment(): Comment? = gson.fromJson(this, Comment::class.java)

/**
 * Create a typed object from json string
 */
fun <T: Any> String.createObject(clazz: KClass<T>) = gson.fromJson<T>(this, clazz.java)!!

/**
 * Create a typed array from json string
 */
fun <T: Any> String.createArray(): Array<T> {
    val type = object: TypeToken<Array<T>>() {}.type
    return gson.fromJson(this, type)
}

/**
 * Convert an object to json string
 */
fun Any.toJson() = gson.toJson(this)!!

/**
 * Convert a Map<String, *> object to json string
 */
fun Map<String, *>.toJson() = gson.toJson(this)!!