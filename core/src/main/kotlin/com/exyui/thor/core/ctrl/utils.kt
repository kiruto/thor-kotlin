package com.exyui.thor.core.ctrl

import com.exyui.thor.core.database.Comment
import com.google.common.net.InetAddresses
import java.net.Inet4Address
import java.net.Inet6Address
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE



/**
 * Created by yuriel on 1/15/17.
 */

val VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", CASE_INSENSITIVE)!!
val VALID_URL_REGEX = Pattern.compile("^(http://|https://)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$")!!

data class VerifyResult(val valid: Boolean, val reason: String? = null)

fun Comment.verify(): VerifyResult {

    parent?.let{ if(it <= 0) return VerifyResult(false, "invalid parent id") }

    if (text.length < 3)
        return VerifyResult(false, "text is too short (minimum length: 3)")
    else if (text.length > 65535)
        return VerifyResult(false, "text is too long (maximum length: 65535)")

    email?.let {
        if (it.length > 254)
            return VerifyResult(false, "http://tools.ietf.org/html/rfc5321#section-4.5.3")
    }

    email?.let {
        val matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(it)
        if (!matcher.find())
            return VerifyResult(false, "invalid email address.")
    }

    website?.let { if (it.length > 254) return VerifyResult(false, "arbitrary length limit") }

    website?.let {
        val matcher = VALID_URL_REGEX.matcher(website)
        if (!matcher.find())
            return VerifyResult(false, "Website not valid")
    }

    return VerifyResult(true)
}

/**
 * Anonymize IPv4 and IPv6 to /24 (zero'd) and /48 (zero'd).
 */
fun String.anonymize(): String {
    val ip = InetAddresses.forString(this)
    val addr = ip.hostAddress
    return when(ip) {
        is Inet6Address -> addr.split(":").subList(0, 4).joinToString(":") { it } + ":0:0:0:0"
        is Inet4Address -> addr.split(".").subList(0, 3).joinToString(".") { it } + ".0"
        else -> "0.0.0.0"
    }
}