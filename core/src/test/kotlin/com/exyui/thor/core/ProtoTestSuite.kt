package com.exyui.thor.core

import com.exyui.testkits.ron
import com.exyui.thor.core.database.Comment
import com.exyui.thor.core.model.createComment
import com.exyui.thor.core.model.toJson
import org.junit.Assert.*
import org.junit.Test
import rx.Observable
import java.util.*

/**
 * Created by yuriel on 1/24/17.
 */
class ProtoTestSuite {
    val url = randomURL()

    @Test fun testJson() {
        Observable.from(0..10)
                .map {
                    val comment = createComment(url)
                    repeat(Random().nextInt(10)) { nest(comment) }
                    comment
                }
                .subscribe {
                    println(it)
                    println(it.toJson())
                    it.toJson().createComment() eq it
                }
    }

    private fun nest(comment: Comment) {
        val new = createComment(url)
        comment.replies.add(new)
        ron { nest(new) }
    }

    private infix fun Comment.eq(o: Comment) {
        assertTrue(id == o.id
        && parent == o.parent
        && created == o.created
        && modified == o.modified
        && text == o.text
        && author == o.author
        && website == o.website
        && likes == o .likes
        && dislikes == o.dislikes
        && userIdentity == o.userIdentity
        && totalReplies == o.totalReplies
        && hiddenReplies == o.hiddenReplies)
        replies.forEachIndexed { i, comment ->
            comment eq o.replies[i]
        }
    }
}