package com.exyui.thor.core

import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.database.Comment
import com.exyui.testkits.*
import org.junit.Test
import org.junit.Assert.*
import rx.Observable

/**
 * Created by yuriel on 1/17/17.
 */

class ControllerTestSuite {
    @Test fun testSingleThreadCRUD() {
        testSingleThreadCRUD(getAURL())
    }

    @Test fun testMultipleThreadCRUD() {
        Observable.from(0..10)
                .map { getAURL() }
                .subscribe { testSingleThreadCRUD(it) }
    }

    @Test fun testLike() {
        val url = getAURL()
        val comment = insertComment(createComment(url), url).second
        assertNotNull(comment.id)
        val id = comment.id!!
        assertNotEquals(id, 0)
        assertEquals(comment.likes, 0)
        assertEquals(comment.dislikes, 0)

        Observable.from(listOf("192.168.1.1", "192.168.1.4", "192.168.1.255", "192.168.2.1"))
                .map {
                    Controller.like(id, it)
                    it
                }
                .toList()
                .flatMap {
                    val c = Controller.viewComment(id)
                    assertEquals(c.likes, 2)
                    assertEquals(c.dislikes, 0)
                    Observable.from(it)
                }
                .map { Controller.dislike(id, it) }
                .subscribe {
                    val c = Controller.viewComment(id)
                    assertEquals(c.likes, 2)
                    assertEquals(c.dislikes, 0)
                }

        Observable.from(listOf("1234:2234:3234:4234:5234:6234:7234:8234",
                "1234:2234:3234:4234:523a:6234:7b34:8c34",
                "1234:2234:3234:4234:5d34:6e34:7204:8234",
                "1234:2234:3234:4235:5234:6234:7234:8234"))
                .map {
                    Controller.dislike(id, it)
                    it
                }
                .toList()
                .flatMap {
                    val c = Controller.viewComment(id)
                    assertEquals(c.likes, 2)
                    assertEquals(c.dislikes, 2)
                    Observable.from(it)
                }
                .map { Controller.like(id, it) }
                .subscribe {
                    val c = Controller.viewComment(id)
                    assertEquals(c.likes, 2)
                    assertEquals(c.dislikes, 2)
                }
    }

    private fun testSingleThreadCRUD(uri: String) {
        val TEST_RECORDS = 100

        val ids = mutableSetOf<Int>()
        for (i in 0..TEST_RECORDS - 1) {
            ids.add(testInsert(uri))
        }
        assertEquals(Comment.count(uri)[0], TEST_RECORDS)
        assertEquals(ids.size, TEST_RECORDS)

        val editList = Comment.fetch(uri).toList().toBlocking().single()
        assertEquals(editList.size, TEST_RECORDS)
        Observable.from(editList.shuffle()).subscribe ({
            assertNotNull(it.id)
            it.id?.let {
                assertTrue(it in ids)
                editComment(it)
            }
        })

        val deleteList = Comment.fetch(uri).toList().toBlocking().single()
        assertEquals(deleteList.size, TEST_RECORDS)
        Observable.from(deleteList.shuffle()).subscribe {
            assertNotNull(it.id)
            it.id?.let {
                assertTrue(it in ids)
                Controller.deleteComment(it)
                ids.remove(it)
            }
        }
        assertEquals(Comment.count(uri)[0], 0)
        assertEquals(ids.size, 0)
    }

    private fun createComment(uri: String): Comment {
        return Comment.create(
                author = randomAlphaNumOfLength(3, 10),
                parent = aon(getRandomComment(uri)?.id),
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
                remoteAddr = c.remoteAddr,
                parent = c.parent
        )
    }

    private fun editComment(id: Int): Comment {
        val text = aon(randomAlphaNumOfLength(100))
        val website = aon(getAWebsite())
        val author = aon(randomAlphaNumOfLength(3, 10))
        val edit = Controller.editComment(id = id, text = text, author = author, website = website)
        val result = Controller.viewComment(id)
        try {
            text?.let { assertEquals(it, result.text) }
            website?.let { assertEquals(it, result.website) }
            author?.let { assertEquals(it, result.author) }
            assertNotNull(result.modified)
            assertNotEquals(result.modified, .0)
            return edit
        } catch (e: Exception) {
            println("from id: $id")
            println("edit to: text: $text, website: $website, author: $author")
            println("result: $result")
            throw e
        }
    }

    private fun testInsert(uri: String): Int {
        val comment = createComment(uri)
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

    private fun getRandomComment(uri: String): Comment? {
        val editList = Comment.fetch(uri).toList().toBlocking().single()
        val c = editList.anyOne()
        println("look for any one from uri=$uri: $c")
        return c
    }

    private fun getAWebsite() = "http://${randomAlphaNumOfLength(3, 10)}.com"
    private fun getAURL() = "http://test.exyui.com/${randomAlphaNumOfLength(10)}"
    private fun getAEmail() = "${randomAlphaNumOfLength(3, 10)}@${randomAlphaNumOfLength(3, 10)}.com"
}