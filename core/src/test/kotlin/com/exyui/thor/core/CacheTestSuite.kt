package com.exyui.thor.core

import com.exyui.thor.core.cache.ThorCache
import com.exyui.testkits.*
import org.junit.Test
import org.junit.Assert.*
import rx.Observable

/**
 * Created by yuriel on 1/18/17.
 */
class CacheTestSuite {
    @Test fun insert() {
        Observable.from(0..10)
                .flatMap {
                    val cache = randomAlphaNumOfLength(3, 10)
                    Observable.from(0..1000)
                            .map {
                                val key = randomAlphaNumOfLength(3, 10)
                                val value = randomAlphaNumOfLength(1000)
                                println("creating cache for $key@$cache")
                                ThorCache[cache][key] = value
                                assertEquals(ThorCache[cache][key], value)
                                Pair(cache, key)
                            }
                }
                .map {
                    val edit = randomAlphaNumOfLength(10000)
                    println("edit cache for ${it.second}@${it.first}")
                    ThorCache[it.first][it.second] = edit
                    assertEquals(ThorCache[it.first][it.second], edit)
                    it
                }
                .subscribe {
                    println("delete cache ${it.second}@${it.first}")
                    ThorCache[it.first].delete(it.second)
                    assertEquals(ThorCache[it.first][it.second], null)
                }
    }
}