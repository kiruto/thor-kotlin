package com.exyui.thor.core

import org.apache.commons.lang3.StringUtils.leftPad
import java.lang.Math.round
import java.lang.Math.random
import java.lang.Math.pow
import java.lang.Math.abs
import java.lang.Math.min
import java.util.*


/**
 * Created by yuriel on 1/17/17.
 */
fun randomAlphaNumOfLength(from: Int, to: Int = 0): String {
    val length = if (to - from > 0)
        (Math.random() * 100 % (to - from)).toInt() + from
    else
        from
    val sb = StringBuffer()
    var i = length
    while (i > 0) {
        val n = min(12, abs(i))
        sb.append(leftPad(java.lang.Long.toString(round(random() * pow(36.0, n.toDouble())), 36), n, '0'))
        i -= 12
    }
    return sb.toString()
}

fun <T> List<T>.anyOne(): T? {
    if (isEmpty()) return null
    val i = Random().nextInt(size)
    return this[i]
}

fun <T> List<T>.aon(): T? = aon(this.anyOne())
fun <T> List<T>.shuffle(): List<T> {
    val seed = System.nanoTime()
    Collections.shuffle(this, Random(seed))
    return this
}

/**
 * @return true or false randomly
 */
fun tof(): Boolean = Random().nextBoolean()

/**
 * @return true or null randomly
 */
fun ton(): Boolean? = if (tof()) true else null

/**
 * @return an Object gives or null randomly
 */
fun <T> aon(a: T?): T? = if (tof()) a else null

fun getRandomHexString(numchars: Int): String {
    val r = Random()
    val sb = StringBuffer()
    while (sb.length < numchars) {
        sb.append(Integer.toHexString(r.nextInt()))
    }

    return sb.toString().substring(0, numchars)
}