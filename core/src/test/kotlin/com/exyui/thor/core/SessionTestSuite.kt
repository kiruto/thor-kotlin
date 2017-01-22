package com.exyui.thor.core

import com.exyui.testkits.*
import com.exyui.thor.core.cache.ThorSession
import com.exyui.thor.core.ctrl.anonymize
import com.exyui.thor.crypto.encrypt
import com.exyui.thor.crypto.encryptWith
import org.junit.Test
import org.junit.Assert.*
import rx.Observable
import java.util.*

/**
 * Created by yuriel on 1/21/17.
 */
class SessionTestSuite {
    @Test fun test() {
        Observable.from(0..10)
                .subscribe {
                    with(getSession()) {
                        val iv = ThorSession.save(id, remote.anonymize(), mail)
                        val hash = (mail?: remote.anonymize()).encryptWith(iv)
                        assertTrue(ThorSession.check(id, hash))
                        ThorSession.delete(id)
                        assertFalse(ThorSession.check(id, hash))
                    }
                }
    }

    private fun getSession(): Session {
        val id = Random().nextInt()
        val mail = aon(randomEmail())
        val remote = randomIP()
        return Session(id, remote, mail)
    }

    private data class Session(val id: Int, val remote: String, val mail: String?)
}