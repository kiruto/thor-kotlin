package com.exyui.yell.core.database

import java.util.*


/**
 * Created by yuriel on 1/14/17.
 */
class Preference {

    val pref = mutableMapOf<String, String>()

    init {
        val r = Random()
        val sb = StringBuffer()
        while (sb.length < 24) {
            sb.append(Integer.toHexString(r.nextInt()))
        }
        pref.put("session-key", sb.toString().substring(0, 24))
        pref.map {
            val v = get(it.key)
            if (v.isEmpty())
                set(it.key, it.value)
            else
                pref[it.key] = v
        }
    }

    companion object {
        operator fun get(key: String, default: String = ""): String {
            return Conn.observable.select("SELECT value FROM preferences WHERE key=?")
                    .parameter(key)
                    .getAs(String::class.java)
                    .toBlocking()
                    .single() ?: default
        }

        operator fun set(key: String, value: String) {
            Conn.observable.update("INSERT INTO preferences (key, value) VALUES (?, ?)")
                    .parameter(key)
                    .parameter(value)
                    .execute()
        }
    }
}