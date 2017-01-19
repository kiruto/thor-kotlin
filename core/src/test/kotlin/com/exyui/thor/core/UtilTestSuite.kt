package com.exyui.thor.core

import com.exyui.thor.core.ctrl.anonymize
import com.github.davidmoten.rx.Transformers
import org.junit.Test
import org.junit.Assert.*
import rx.Observable

/**
 * Created by yuriel on 1/19/17.
 */
class UtilTestSuite {
    @Test fun testAnonymize() {
        val v4 = listOf (
                "192.168.1.1", "192.168.1.4", "192.168.1.255", "192.168.2.1"
        )
        val an4 = "192.168.1.0"
        val v6 = listOf(
                "1234:2234:3234:4234:5234:6234:7234:8234",
                "1234:2234:3234:4234:523a:6234:7b34:8c34",
                "1234:2234:3234:4234:5d34:6e34:7204:8234",
                "1234:2234:3234:4235:5234:6234:7234:8234"
        )
        val an6 = "1234:2234:3234:4234:0:0:0:0"
        ipAssert(v4, an4)
        ipAssert(v6, an6)
    }

    private fun ipAssert(list: List<String>, anonymize: String) {
        Observable.from(list)
                .compose(Transformers.mapWithIndex())
                .map {
                    Pair(it.index(), it.value().toString().anonymize() == anonymize.anonymize())
                }
                .subscribe {
                    assertEquals(when(it.first) {
                        3L -> false
                        else -> true
                    }, it.second)
                }
    }
}