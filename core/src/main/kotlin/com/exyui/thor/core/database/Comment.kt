package com.exyui.thor.core.database

import com.exyui.thor.core.Accept
import com.exyui.thor.core.PublicApi
import com.github.davidmoten.rx.jdbc.Database
import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnels
import rx.Observable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import java.sql.ResultSet
import javax.xml.bind.DatatypeConverter

/**
 * Created by yuriel on 1/14/17.
 *
 * @param id : comment id (unique per website).
 * @param parent : parent id reference, may be null.
 * @param text : required, comment written in Markdown.
 * @param mode :
 *  1 – accepted
 *  2 – in moderation queue
 *  4 – deleted, but referenced.
 * @param hash : user identication, used to generate identicons. PBKDF2 from email or IP address (fallback).
 * @param author : author's name, may be null.
 * @param website : author's website, may be null.
 * @param likes : upvote count, defaults to 0.
 * @param dislikes : downvote count, defaults to 0.
 * @param created : time in seconds since UNIX time.
 * @param modified : last modification since UNIX time, may be null.
 */
data class Comment private constructor(val tid: Int? = null,
                                       @PublicApi val id: Int? = null,
                                       @PublicApi @Accept val parent: Int? = null,
                                       @PublicApi val created: Double? = null,
                                       @PublicApi val modified: Double? = null,
                                       val mode: Int = 0,
                                       val remoteAddr: String,
                                       @PublicApi @Accept val text: String = "",
                                       @PublicApi @Accept val author: String? = null,
                                       @Accept val email: String? = null,
                                       @PublicApi @Accept val website: String? = null,
                                       @PublicApi val likes: Int = 0,
                                       @PublicApi val dislikes: Int = 0,
                                       val voters: ByteArray? = null) {

    val user = (email?: remoteAddr).toLowerCase()

    var userIdentity: String? = null
        set(value) {
            if (null == field && value != null) field = value
        }

        get() {
            if (null == field) {
                val b = MessageDigest.getInstance("MD5").digest(user.toByteArray(Charset.forName("UTF-8")))
                field = DatatypeConverter.printHexBinary(b)
            }
            return field
        }

    var totalReplies: Int? = null

    var hiddenReplies: Int? = null

    val replies = mutableListOf<Comment>()

    private constructor(rs: ResultSet): this(
            tid = rs.getInt(1),
            id = rs.getInt(2),
            parent = rs.getInt(3),
            created = rs.getDouble(4),
            modified = rs.getDouble(5),
            mode = rs.getInt(6),
            remoteAddr = rs.getString(7),
            text = rs.getString(8),
            author = rs.getString(9),
            email = rs.getString(10),
            website = rs.getString(11),
            likes = rs.getInt(12),
            dislikes = rs.getInt(13),
            voters = rs.getBytes(14)
    )

    internal companion object {

        val fieldSize = 14

        fun create(author: String?, parent: Int?, email: String?, website: String?, text: String, mode: Int, remoteAddr: String): Comment {
            return Comment(
                    author = author,
                    parent = parent,
                    email = email,
                    website = website,
                    text = text,
                    mode = mode,
                    remoteAddr = remoteAddr)
        }

        /**
         * Activate comment id if pending.
         */
        fun activate(id: Int) {
            Conn.observable
                    .update("UPDATE comments SET mode=1 WHERE id=? AND mode=2;")
                    .parameter(id)
                    .execute()
        }

        fun update(id: Int, data: Comment): Comment {
            Conn.observable
                    .update("UPDATE comments SET ? WHERE id=?;")
                    .parameter(data.toKVString())
                    .parameter(id)
                    .execute()
            return get(id)!!
        }

        /**
         * Update comment
         * @param: id
         * @return updated comment.
         */
        fun update(id: Int, text: String? = null, author: String? = null, website: String? = null): Comment {
            val kv = StringBuilder()
            text?.let { kv.append("text=?, ") }
            author?.let{ kv.append("author=?, ") }
            website?.let{ kv.append("website=?, ") }
            kv.append("modified=${System.currentTimeMillis()}")
            val ob = Conn.observable.update("UPDATE comments SET $kv WHERE id=?;")
            text?.let { ob.parameter(it) }
            author?.let { ob.parameter(it) }
            website?.let { ob.parameter(it) }
            ob.parameter(id).execute()

            return get(id)!!
        }

        /**
         * Search for comment
         * @param: id
         * @return a comment.
         */
        operator fun get(id: Int): Comment? {
            val r = Conn.observable
                    .select("SELECT * FROM comments WHERE id=?")
                    .parameter(id)
                    .get(::Comment)
                    .toList()
                    .toBlocking()
                    .single()
            return if (r.isEmpty()) null else r[0]
        }

        /**
         * Return comments observable
         * @param: uri
         * @param: mode
         */
        fun fetch(uri: String, mode: Int = 5, after: Double = .0, parent: Int? = -1, orderBy: String = "id", limit: Int? = null): Observable<Comment> {
            var sql = "SELECT comments.* FROM comments INNER JOIN threads ON " +
                    "threads.uri=? AND comments.tid=threads.id AND (? | comments.mode) = ? " +
                    "AND comments.created > ? "
            val args = mutableListOf(uri, mode, mode, after)

            if (parent == null) {
                sql += "AND comments.parent IS NULL "
            } else if (parent != -1) {
                sql += "AND comments.parent=? "
                args.add(parent)
            }

            val order = if (orderBy in listOf("id", "created", "modified", "likes", "dislikes")) orderBy else "id"
            sql += "ORDER BY $order ASC "

            limit?.let {
                sql += "LIMIT ? "
                args.add(limit)
            }

            val builder = Conn.observable.select(sql)
            for (p in args) {
                builder.parameter(p)
            }
            return builder[::Comment]
        }

        /**
         * Delete a comment. There are two distinctions: a comment is referenced
         * by another valid comment's parent attribute or stand-a-lone. In this
         * case the comment can't be removed without losing depending comments.
         * Hence, delete removes all visible data such as text, author, email,
         * website sets the mode field to 4.
         * In the second case this comment can be safely removed without any side
         * effects.
         */
        fun delete(id: Int) {
            Conn.observable
                    .select("SELECT * FROM comments WHERE parent=?")
                    .parameter(id)
                    .get(::Comment)
                    .isEmpty
                    .subscribe {
                        if (it) {
                            Conn.observable.update("DELETE FROM comments WHERE id=?").parameter(id).execute()
                            removeStale()
                        } else {
                            Conn.observable.update("UPDATE comments SET text=? WHERE id=?")
                                    .parameter("")
                                    .parameter(id)
                                    .execute()
                            Conn.observable.update("UPDATE comments SET mode=? WHERE id=?")
                                    .parameter(4)
                                    .parameter(id)
                                    .execute()
                            Conn.observable.update("UPDATE comments SET author=? WHERE id=?")
                                    .parameter(null)
                                    .parameter(id)
                                    .execute()
                            Conn.observable.update("UPDATE comments SET website=? WHERE id=?")
                                    .parameter(null)
                                    .parameter(id)
                                    .execute()
                            removeStale()
                        }
                    }
        }

        private fun removeStale() {
            val sql = "DELETE FROM comments WHERE mode=4 AND id NOT IN (SELECT parent FROM comments WHERE parent IS NOT NULL)"
            while (0 != Conn.observable.update(sql).execute());
        }

        /**
         * +1 a given comment. Returns the new like count (may not change because
         * the creater can't vote on his/her own comment and multiple votes from the
         * same ip address are ignored as well).
         */
        fun vote(upvote: Boolean, id: Int, remoteAddr: String): Vote {
            val c = Conn.observable
                    .select("SELECT likes, dislikes, voters FROM comments WHERE id=?")
                    .parameter(id)
                    .get { Comment(likes = it.getInt(1), dislikes = it.getInt(2), voters = it.getBytes(3), remoteAddr = "") }
                    .toBlocking()
                    .first()
            if (c.likes + c.dislikes >= 142)
                return Vote(c.likes, c.dislikes)

            val bf: BloomFilter<ByteArray>
            if (null != c.voters) {
                bf = readBloomFilter(c.voters)
                if (bf.mightContain(remoteAddr.toByteArray()))
                    return Vote(c.likes, c.dislikes)
            } else {
                bf = createBloomFilter(c.likes + c.dislikes)
            }

            bf.put(c.voters)
            bf.put(remoteAddr.toByteArray())
            val output = ByteArrayOutputStream()
            bf.writeTo(output)

            val voteQry = if (upvote) "likes = likes + 1" else "dislikes = dislikes + 1"
            Conn.observable.update("UPDATE comments SET $voteQry, voters = ? WHERE id=?;")
                    .parameter(Database.toSentinelIfNull(output.toByteArray()))
                    .parameter(id)
                    .execute()
            if (upvote)
                return Vote(c.likes + 1, c.dislikes)
            else
                return Vote(c.likes, c.dislikes + 1)
        }

        private fun readBloomFilter(bytes: ByteArray): BloomFilter<ByteArray> {
            return BloomFilter.readFrom(ByteArrayInputStream(bytes), Funnels.byteArrayFunnel())
        }

        private fun createBloomFilter(size: Int): BloomFilter<ByteArray> {
            return BloomFilter.create(Funnels.byteArrayFunnel(), size)
        }

        /**
         * Return comment count for main thread and all reply threads for one url.
         */
        fun replyCount(url: String, mode: Int = 5, after: Double = .0): MutableMap<Int, Int> {
            val sql = "SELECT comments.parent,count(*) FROM comments " +
                    "INNER JOIN threads ON threads.uri=? AND comments.tid=threads.id AND (? | comments.mode = ?) AND comments.created > ? " +
                    "GROUP BY comments.parent"
            return Conn.observable.select(sql)
                    .parameter(url)
                    .parameter(mode)
                    .parameter(mode)
                    .parameter(after)
                    .get { Pair(it.getInt(0), it.getInt(1)) }
                    .reduce(mutableMapOf<Int, Int>()) { map, pair ->
                        map.put(pair.first, pair.second)
                        map
                    }
                    .toBlocking()
                    .single()
        }

        /**
         * Return comment count for one ore more urls..
         */
        fun count(vararg urls: String): IntArray {
            val sql = "SELECT threads.uri, COUNT(comments.id) FROM comments " +
                    "LEFT OUTER JOIN threads ON threads.id = tid AND comments.mode = 1 " +
                    "GROUP BY threads.uri"
            return Conn.observable.select(sql)
                    .get { Pair(it.getString(1), it.getInt(2)) }
                    .reduce(mutableMapOf<String, Int>()) { map, pair ->
                        map.put(pair.first, pair.second)
                        map
                    }
                    .reduce(IntArray(urls.size)) { array, map ->
                        urls.mapIndexed { i, s -> array[i] = map[s]?: 0 }
                        array
                    }
                    .toBlocking()
                    .single()
        }

        /**
         * Remove comments older than delta.
         * @param delta
         */
        fun purge(delta: Double) {
            Conn.observable.update("DELETE FROM comments WHERE mode = 2 AND ? - created > ?;")
                    .parameter(System.currentTimeMillis())
                    .parameter(delta)
                    .execute()
            removeStale()
        }
    }

    /**
     * Add new comment to DB and return a comment and database values.
     */
    internal fun insert(uri: String): Pair<Int, Comment> {
        var p: Int? = parent
        parent?.let {
            val ref = Comment[it]
            ref?.let {
                p = it.parent
            }
        }

        val bf = createBloomFilter(1)
        bf.put(remoteAddr.toByteArray())
        val output = ByteArrayOutputStream()
        bf.writeTo(output)

        Conn.observable.update(
                "INSERT INTO comments (" +
                "    tid, parent," +
                "    created, modified, mode, remote_addr," +
                "    text, author, email, website, voters )" +
                "SELECT" +
                "    threads.id, ?," +
                "    ?, ?, ?, ?," +
                "    ?, ?, ?, ?, ?" +
                "FROM threads WHERE threads.uri = ?;")
                .parameter(p)
                .parameter(created?: System.currentTimeMillis())
                .parameter(null)
                .parameter(mode)
                .parameter(remoteAddr)
                .parameter(text)
                .parameter(author)
                .parameter(email)
                .parameter(website)
                .parameter(Database.toSentinelIfNull(output.toByteArray()))
                .parameter(uri)
                .execute()
        return Conn.observable.select("SELECT *, MAX(c.id) FROM comments AS c INNER JOIN threads ON threads.uri = ?")
                .parameter(uri)
                .get { Pair(it.getInt(fieldSize + 1), Comment(it)) }
                .toBlocking()
                .single()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other?.toString()
    }

    fun toKVString(): String {
        return tid?.let { "tid = $tid, " }.orEmpty() +
                id?.let { "id = $id, " }.orEmpty() +
                parent?.let { "parent = $parent, " }.orEmpty() +
                created?.let { "created = $created, " }.orEmpty() +
                modified?.let { "modified = $modified, " }.orEmpty() +
                "mode = $mode, " +
                "remoteAddr = $remoteAddr, " +
                "text = $text, " +
                author?.let { "author = $author, " }.orEmpty() +
                email?.let { "email = $email, " }.orEmpty() +
                website?.let { "website = $website, " }.orEmpty() +
                "likes = $likes, " +
                "dislikes = $dislikes"
    }
}

