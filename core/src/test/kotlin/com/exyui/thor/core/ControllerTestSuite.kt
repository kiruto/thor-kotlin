package com.exyui.thor.core

import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.database.Comment
import org.junit.Test
import org.junit.Assert.*
import rx.Observable

/**
 * Created by yuriel on 1/17/17.
 */

class ControllerTestSuite {
    @Test
    fun testCRUD() {
        val TEST_RECORDS = 10000
        val URI = "http://test.exyui.com/${randomAlphaNumOfLength(10)}"

        val ids = mutableSetOf<Int>()
        for (i in 0..TEST_RECORDS - 1) {
            ids.add(testInsert(URI))
        }
        assertEquals(Comment.count(URI)[0], TEST_RECORDS)
        assertEquals(ids.size, TEST_RECORDS)

        val editList = Comment.fetch(URI).toList().toBlocking().single()
        assertEquals(editList.size, TEST_RECORDS)
        Observable.from(editList).subscribe {
            assertNotNull(it.id)
            it.id?.let {
                assertTrue(it in ids)
                editComment(it)
            }
        }


        val deleteList = Comment.fetch(URI).toList().toBlocking().single()
        assertEquals(deleteList.size, TEST_RECORDS)
        Observable.from(deleteList).subscribe {
            assertNotNull(it.id)
            it.id?.let {
                assertTrue(it in ids)
                Controller.deleteComment(it)
                ids.remove(it)
            }
        }
        assertEquals(Comment.count(URI)[0], 0)
        assertEquals(ids.size, 0)
    }

    private fun createComment(): Comment {
        return Comment.create(
                author = randomAlphaNumOfLength(3, 10),
                email = getAEmail(),
                website = getAWebsite(),
                text = randomAlphaNumOfLength(3, 100),
                mode = 1,
                remoteAddr = "127.0.0.1"
        )
    }

    private fun insertComment(c: Comment, uri: String): Pair<Int, Comment> {
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

    private fun editComment(id: Int): Comment {
        return Controller.editComment(
                id = id,
                text = noa(randomAlphaNumOfLength(100)),
                website = noa(getAWebsite()),
                author = noa(randomAlphaNumOfLength(3, 10))
        )
    }

    private fun testInsert(uri: String): Int {
        val comment = createComment()
        val pair = insertComment(comment, uri)
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

    private fun getAWebsite() = "http://${randomAlphaNumOfLength(3, 10)}.com"
    private fun getAEmail() = "${randomAlphaNumOfLength(3, 10)}@${randomAlphaNumOfLength(3, 10)}.com"
}