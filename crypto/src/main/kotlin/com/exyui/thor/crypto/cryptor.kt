package com.exyui.thor.crypto

import java.lang.Character.digit
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

/**
 * Created by yuriel on 1/16/17.
 */

fun encrypt(text: String, key: String): String {

    return ""
}

/**
 * @see http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters/6481658#6481658
 */
fun disEncrypt(text: String, key: String): String {
    val iv = text.substring(text.length - 16, text.length) + text.substring(0, 16)
    val content = text.substring(16, text.length-16)
    val bytes = Base64.getDecoder().decode(content.toByteArray(Charset.forName("UTF-8")))
    val cipher = Cipher.getInstance("AES/CFB/NoPadding")
    println(key.toBytes().size)
    println(iv.toBytes().size)
    val keyspec = SecretKeySpec(key.toBytes(), "AES")
    val ivspec = IvParameterSpec(iv.toBytes())
    cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec)
    val original = cipher.doFinal(bytes)
    val originalString = String(original)
    return originalString
}

private fun String.toBytes(): ByteArray {
    return DatatypeConverter.parseHexBinary(this)
}