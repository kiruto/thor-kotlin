package com.exyui.thor.core.cache

import com.exyui.thor.crypto.decryptWithIV
import com.exyui.thor.crypto.forceEnc

/**
 * Created by yuriel on 1/21/17.
 */
object ThorSession {
    private val sessionCache = ThorCache["session"]
    private val ivCache = ThorCache["iv"]

    /**
     * Save session
     */
    fun save(id: Int, remoteAddr: String, mail: String? = null) {
        val v = mail?: remoteAddr
        val enc = v.forceEnc()
        ivCache[id] = enc.iv
        sessionCache[id] = enc.r
    }

    /**
     * Get session
     */
    fun check(id: Int, remoteAddr: String, mail: String? = null): Boolean {
        val iv = ivCache[id] ?: return false
        val v = mail?: remoteAddr
        val session = sessionCache[id]?: return false
        return decryptWithIV(session, iv) == v
    }

    fun delete(id: Int) {
        ivCache.delete(id)
        sessionCache.delete(id)
    }
}