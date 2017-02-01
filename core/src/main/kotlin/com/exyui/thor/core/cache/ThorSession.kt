package com.exyui.thor.core.cache

import com.exyui.thor.core.cache.CoreCache.*
import com.exyui.thor.crypto.encryptWithRandomIV

/**
 * Created by yuriel on 1/21/17.
 */
object ThorSession {
    private val sessionCache = SESSION.cache

    /**
     * Save a session
     * todo: 客户端拿到这个IV后，首先应该用自己的mail和remoteAddr进行加密，并改写该session的cookie。之后用加密后的token进行对话
     * todo: 注意，客户端可能除了IV外还需要知道自己的remote address
     *
     * !!warning!! remote address must be anonymized before use.
     *
     * @return: IV. Use to check the contents after encrypted by this
     */
    fun save(id: Int, remoteAddr: String, mail: String? = null): String {
        val v = mail?: remoteAddr
        return renew(id, v)
    }

    /**
     * Renew token from old session token.
     */
    fun renew(id: Int, old: String): String {
        val enc = old.encryptWithRandomIV()
        sessionCache[id] = enc.r
        return enc.iv
    }

    operator fun get(id: Int) = sessionCache[id]

    fun check(id: Int, session: String): Boolean {
        val s = this[id]?: return false
        return s == session
    }

    fun delete(id: Int) {
        sessionCache.delete(id)
    }
}