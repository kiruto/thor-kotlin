package com.exyui.thor.core.database

import com.exyui.thor.SQLITE_FILE
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import com.github.davidmoten.rx.jdbc.Database

/**
 * Created by yuriel on 1/14/17.
 *
 * DB-dependend wrapper around SQLite3.
 * Runs migration if `user_version` is older than `MAX_VERSION` and register
 * a trigger for automated orphan removal.
 */
object Conn {

    private val conn: Connection by lazy { DriverManager.getConnection("jdbc:sqlite:$SQLITE_FILE") }
    private val version by lazy {
        observable.select("PRAGMA user_version").getAs(Int::class.java).toBlocking().single()
    }
    val observable: Database by lazy { Database.from(conn) }

    fun stmt(sql: String, vararg args: Any): PreparedStatement {
        val stmt = conn.prepareStatement(sql)
        for (i in 1..args.size) {
            stmt.setString(i, args[i - 1].toString())
        }
        return stmt
    }

    init {
        createComment()
        createThread()
        createPreference()

        val rv = observable
                .select("SELECT name FROM sqlite_master WHERE type='table' AND name IN ('threads', 'comments', 'preferences')")
                .getAs(String::class.java)
                .toList()
                .toBlocking()
                .single()

        if (rv?.isEmpty()?: true) {
            stmt("PRAGMA user_version = $SQL_VERSION").execute()
        } else {
            migrate(to = SQL_VERSION)
        }

        stmt("CREATE TRIGGER IF NOT EXISTS remove_stale_threads " +
                "AFTER DELETE ON comments " +
                "BEGIN " +
                "DELETE FROM threads WHERE id NOT IN (SELECT tid FROM comments);" +
                "END").execute()
    }

    private fun createComment() = stmt(CREATE_COMMENT).execute()
    private fun createThread() = stmt(CREATE_THREAD).execute()
    private fun createPreference() = stmt(CREATE_PREFERENCE).execute()

    /**
     * For update database
     */
    private fun migrate(to: Int) {
        if (version > to)
            return
    }

}