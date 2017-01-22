package com.exyui.thor.core

import com.exyui.thor.core.ctrl.Controller
import com.exyui.thor.core.database.Comment
import com.exyui.testkits.*
import org.junit.Test
import org.junit.Assert.*
import rx.Observable
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by yuriel on 1/17/17.
 */

class ControllerTestSuite {
    @Test fun testSingleThreadCRUD() {
        testSingleThreadCRUD(randomURL())
    }

    @Test fun testMultipleThreadCRUD() {
        Observable.from(0..10)
                .map { randomURL() }
                .subscribe { testSingleThreadCRUD(it) }
    }

    @Test fun testLike() {
        val url = randomURL()
        val comment = insertComment(createComment(url), url).second
        assertNotNull(comment.id)
        val id = comment.id!!
        assertNotEquals(id, 0)
        comment.likes mustEq 0
        comment.dislikes mustEq 0

        Observable.from(listOf("192.168.1.1", "192.168.1.4", "192.168.1.255", "192.168.2.1"))
                .map {
                    Controller.like(id, it)
                    it
                }
                .toList()
                .flatMap {
                    val c = Controller.viewComment(id)
                    c.likes mustEq 2
                    c.dislikes mustEq 0
                    Observable.from(it)
                }
                .map { Controller.dislike(id, it) }
                .subscribe {
                    val c = Controller.viewComment(id)
                    c.likes mustEq 2
                    c.dislikes mustEq 0
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
                    c.likes mustEq 2
                    c.dislikes mustEq 2
                    Observable.from(it)
                }
                .map { Controller.like(id, it) }
                .subscribe {
                    val c = Controller.viewComment(id)
                    c.likes mustEq 2
                    c.dislikes mustEq 2
                }
    }

    @Test fun testCount() {
        Observable.from(0..10)
                .map {
                    val url = randomURL()
                    val times = Random().nextInt(10)
                    countComment(url, times)
                    Pair(url, times)
                }
                .reduce(Pair(mutableListOf<String>(), mutableListOf<Int>())) { result, it ->
                    result.first.add(it.first)
                    result.second.add(it.second)
                    result
                }
                .subscribe {
                    Controller.counts(*it.first.toTypedArray()).forEachIndexed { idx, v ->
                        v mustEq it.second[idx]
                    }
                }
    }

    @Test fun testVerify() {
        val url = randomWebsite()
        Observable.from(listOf(
                createComment(url, parent = -1),
                createComment(url, text = ""),
                createComment(url, text = "a"),
                createComment(url, text = randomAlphaNumOfLength(66666)),
                createComment(url, email = ""),
                createComment(url, email = "fpobqroqbeoreo"),
                createComment(url, email = "${randomAlphaNumOfLength(254)}@123.com"),
                createComment(url, website = ""),
                createComment(url, website = "fdsialbgileb.1")))
                .subscribe {
                    assertErr(ThorBadRequest::class) {
                        insertComment(it, url)
                    }
                }
    }

    @Test fun commentShouldNotFound() {
        err(ThorNotFound::class) {
            repeat(10) {
                Controller.viewComment(Random().nextInt())
            }
        }
    }

    private fun testSingleThreadCRUD(uri: String) {
        val TEST_RECORDS = 100

        val ids = mutableSetOf<Int>()
        for (i in 0..TEST_RECORDS - 1) {
            ids.add(testInsert(uri))
        }
        Comment.count(uri)[0] mustEq TEST_RECORDS
        ids.size mustEq TEST_RECORDS

        val editList = Comment.fetch(uri).toList().toBlocking().single()
        editList.size mustEq TEST_RECORDS
        Observable.from(editList.shuffle()).subscribe ({
            assertNotNull(it.id)
            it.id?.let {
                assertTrue(it in ids)
                editComment(it)
            }
        })

        val deleteList = Comment.fetch(uri).toList().toBlocking().single()
        deleteList.size mustEq TEST_RECORDS
        Observable.from(deleteList.shuffle()).subscribe {
            assertNotNull(it.id)
            it.id?.let {
                assertTrue(it in ids)
                Controller.deleteComment(it)
                ids.remove(it)
            }
        }
        Comment.count(uri)[0] mustEq 0
        ids.size mustEq 0
    }

    private fun createComment(
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
        val website = aon(randomWebsite())
        val author = aon(randomAlphaNumOfLength(3, 10))
        val edit = Controller.editComment(id = id, text = text, author = author, website = website)
        val result = Controller.viewComment(id)
        try {
            text?.let { it mustEq result.text }
            website?.let { it mustEq result.website }
            author?.let { it mustEq result.author }
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
            comment.author mustEq it.author
            comment.email mustEq it.email
            comment.website mustEq it.website
            comment.text mustEq it.text
            comment.mode mustEq it.mode
            comment.remoteAddr mustEq it.remoteAddr
        }
        return result!!.id!!
    }

    private fun getRandomComment(uri: String): Comment? {
        val editList = Comment.fetch(uri).toList().toBlocking().single()
        val c = editList.anyOne()
        println("look for any one from uri=$uri: $c")
        return c
    }

    private fun countComment(url: String, times: Int) {
        var count = 0
        kotlin.repeat(times) {
            insertComment(createComment(url), url)
            count ++
            Controller.count(url) mustEq count
        }
    }

    private fun randomURL() = "http://test.exyui.com/${randomAlphaNumOfLength(10)}"
}