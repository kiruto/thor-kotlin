package com.exyui.thor.core

import org.apache.commons.lang3.StringUtils.leftPad
import java.lang.Math.round
import java.lang.Math.random
import java.lang.Math.pow
import java.lang.Math.abs
import java.lang.Math.min
import java.util.Random

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

/**
 * @return true or false randomly
 */
fun tof(): Boolean = Random().nextBoolean()

/**
 * @return null or 1 randomly
 */
fun no1(): Int? = if (tof()) 1 else null

/**
 * @return null or any gives randomly
 */
fun <T>noa(a: T): T? = if (tof()) a else null