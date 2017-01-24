package com.exyui.testkits

import org.apache.commons.lang3.StringUtils.leftPad
import org.junit.Assert.*
import java.lang.Math.round
import java.lang.Math.random
import java.lang.Math.pow
import java.lang.Math.abs
import java.lang.Math.min
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass


/**
 * Created by yuriel on 1/17/17.
 */

fun randomIPV4(): String = (0..3).joinToString(".") { Random().nextInt(255).toString() }
fun randomIPV6(): String = (0..7).joinToString(":") { Integer.toHexString(Random().nextInt(65535)) }
fun randomIP(): String = aon(randomIPV4())?: randomIPV6()

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

fun randomRange(bound: Int, from: Int = 0) = from..(from + Random().nextInt(bound))

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
 * @return result or null
 */
fun <T>ron(func: () -> (T)): T? = if (tof()) func.invoke() else null

/**
 * @return true or null randomly
 */
fun ton(): Boolean? = if (tof()) true else null

/**
 * @return Return an Object gave or null randomly
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

fun randomWebsite() = "http://${randomAlphaNumOfLength(3, 10)}.com"
fun randomEmail() = "${randomAlphaNumOfLength(3, 10)}@${randomAlphaNumOfLength(3, 10)}.com"

fun err(e: KClass<out Throwable>, func: () -> Any): Boolean {
    try {
        func.invoke()
    } catch (ex: Throwable) {
        println(ex.message)
        return e.java == ex.javaClass
    }
    return false
}

fun assertErr(func: () -> Any) {
    assertErr(Throwable::class, func)
}

fun assertErr(e: KClass<out Throwable>, func: () -> Any) {
    assertTrue(err(e, func))
}

infix fun KClass<out Throwable>.mustThrowAt(func: () -> Any) {
    assertErr(this, func)
}

infix fun (() -> Any).mustThrow(throwable: KClass<out Throwable>) {
    assertErr(throwable, this)
}

infix fun Any?.mustEq(other: Any?) {
    assertEquals(this, other)
}

infix fun Any?.mustNot(other: Any?) {
    assertNotEquals(this, other)
}