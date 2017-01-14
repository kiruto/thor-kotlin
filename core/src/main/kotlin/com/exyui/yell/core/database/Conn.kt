package com.exyui.yell.core.database

import com.exyui.yell.core.SQLITE_FILE
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import com.github.davidmoten.rx.jdbc.Database



/**
 * Created by yuriel on 1/14/17.
 */
object Conn {

    private val conn: Connection by lazy { DriverManager.getConnection("jdbc:sqlite:$SQLITE_FILE") }
    val observable by lazy { Database.from(conn) }

    fun stmt(sql: String, vararg args: Any): PreparedStatement {
        val stmt = conn.prepareStatement(sql)
        for (i in 1..args.size) {
            stmt.setString(i, args[i - 1].toString())
        }
        return stmt
    }

    private fun init() {

    }

    private fun createComment() {
        stmt(CREATE_COMMENT).execute()
    }
    private fun createThread() {}
    private fun createPreference() {}

}