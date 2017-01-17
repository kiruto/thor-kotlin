package com.exyui.thor.core

import com.exyui.thor.core.database.Comment
import org.junit.Test
import org.junit.Assert.*

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

    private fun insert(c: Comment, uri: String): Int {
        return c.insert(uri).first
    }

    private fun testInsert(uri: String): Int {
        val comment = create()
        val id = insert(comment, uri)
        val result = Comment[id]
        assertNotNull(result)
        result?.let {
            assertEquals(id, it.id)
            assertEquals(comment.author, it.author)
            assertEquals(comment.email, it.email)
            assertEquals(comment.website, it.website)
            assertEquals(comment.text, it.website)
            assertEquals(comment.mode, it.mode)
            assertEquals(comment.remoteAddr, it.remoteAddr)
        }
        return id
    }

    @Test
    fun testCRUD() {
        val TEST_RECORDS = 10000
        val URI = "http://text.exyui.com/1"

        val ids = IntArray(TEST_RECORDS)
        for (i in 0..TEST_RECORDS - 1) {
            ids[i] = testInsert(URI)
        }
        assertEquals(Comment.count(URI)[0], TEST_RECORDS)
        Comment.fetch(URI).subscribe {
            assertNotNull(it.id)
            Comment.delete(it.id!!)
        }
        assertEquals(Comment.count(URI)[0], 0)
    }
}