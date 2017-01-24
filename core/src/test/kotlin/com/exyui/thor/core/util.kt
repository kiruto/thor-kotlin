package com.exyui.thor.core

import com.exyui.testkits.*
import com.exyui.thor.core.database.Comment

/**
 * Created by yuriel on 1/24/17.
 */
fun createComment(
        uri: String,
        author: String = randomAlphaNumOfLength(3, 10),
        parent: Int? = aon(getRandomComment(uri)?.id),
        email: String = randomEmail(),
        website: String = randomWebsite(),
        text: String = randomAlphaNumOfLength(3, 100),
        mode: Int = 1,
        remoteAddr: String = "127.0.0.1"): Comment {
    return Comment.create(
            author = author,
            parent = parent,
            email = email,
            website = website,
            text = text,
            mode = mode,
            remoteAddr = remoteAddr
    )
}

fun getRandomComment(uri: String): Comment? {
    val editList = Comment.fetch(uri).toList().toBlocking().single()
    val c = editList.anyOne()
    println("look for any one from uri=$uri: $c")
    return c
}

fun randomURL() = "http://test.exyui.com/${randomAlphaNumOfLength(10)}"