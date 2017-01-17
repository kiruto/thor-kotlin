package com.exyui.thor.core.database

import java.sql.ResultSet

/**
 * Created by yuriel on 1/14/17.
 */
data class Thread private constructor(val id: Int, val uri: String, val title: String) {

    private constructor(rs: ResultSet): this(
            id = rs.getInt(1),
            uri = rs.getString(2),
            title = rs.getString(3)
    )

    companion object {
        operator fun contains(uri: String): Boolean {
            return 0 != Conn.observable.select("SELECT title FROM threads WHERE uri=?")
                    .parameter(uri)
                    .count()
                    .toBlocking()
                    .single()
        }

        operator fun get(uri: String): Thread {
            return Conn.observable.select("SELECT * FROM threads WHERE uri=?")
                    .parameter(uri)
                    .get(::Thread)
                    .toBlocking()
                    .single()
        }

        fun new(uri: String, title: String): Thread {
            Conn.observable.update("INSERT INTO threads (uri, title) VALUES (?, ?)")
                    .parameter(uri)
                    .parameter(title)
                    .execute()
            return this[uri]
        }
    }
}