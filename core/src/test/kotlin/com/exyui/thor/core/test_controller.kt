package com.exyui.thor.core

import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.database.Comment
import org.junit.Test
import org.junit.Assert.*
import rx.Observable

/**
 * Created by yuriel on 1/17/17.
 */

class TestComment {
    private fun create(): Comment {
        return Comment.create(
                author = randomAlphaNumOfLength(3, 10),
                email = "${randomAlphaNumOfLength(3, 10)}@${randomAlphaNumOfLength(3, 10)}.${randomAlphaNumOfLength(3)}",
                website = "http://${randomAlphaNumOfLength(3, 10)}.com",
                text = randomAlphaNumOfLength(3, 100),
                mode = 1,
                remoteAddr = "127.0.0.1"
        )
    }

    private fun insert(c: Comment, uri: String): Pair<Int, Comment> {
//        return c.insert(uri).first
        return Controller.insertComment(
                uri = uri,
                title = randomAlphaNumOfLength(3, 10),
                author = c.author,
                email = c.email,
                website = c.website,
                text = c.text,
                remoteAddr = c.remoteAddr
        )
    }

    private fun testInsert(uri: String): Int {
        val comment = create()
        val pair = insert(comment, uri)
        println("created: $comment")
        println("result: ${pair.first}: ${pair.second}")
        val result = Comment[pair.second.id!!]
        assertNotNull(result)
        result?.let {
            assertEquals(comment.author, it.author)
            assertEquals(comment.email, it.email)
            assertEquals(comment.website, it.website)
            assertEquals(comment.text, it.text)
            assertEquals(comment.mode, it.mode)
            assertEquals(comment.remoteAddr, it.remoteAddr)
        }
        return result!!.id!!
    }

    @Test
    fun testCRUD() {
        val TEST_RECORDS = 10000
        val URI = "http://text.exyui.com/${randomAlphaNumOfLength(10)}"

        val ids = IntArray(TEST_RECORDS)
        for (i in 0..TEST_RECORDS - 1) {
            ids[i] = testInsert(URI)
        }
        assertEquals(Comment.count(URI)[0], TEST_RECORDS)
        val list = Comment.fetch(URI).toList().toBlocking().single()
        assertEquals(list.size, TEST_RECORDS)
        Observable.from(list).subscribe { Comment.delete(it.id!!) }
        assertEquals(Comment.count(URI)[0], 0)
    }
}