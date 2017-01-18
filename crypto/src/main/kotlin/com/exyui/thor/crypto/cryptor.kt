package com.exyui.thor.crypto

import com.exyui.thor.AES_KEY
import java.nio.charset.Charset
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.security.SecureRandom


/**
 * Created by yuriel on 1/16/17.
 */

/**
 * @see: http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters/6481658#6481658
 * @see: http://stackoverflow.com/questions/1179672/how-to-avoid-installing-unlimited-strength-jce-policy-files-when-deploying-an
 */
private val unlocked by lazy {
    val field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted")
    field.isAccessible = true
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())
    field.set(null, java.lang.Boolean.FALSE)
    true
}

private val defaultKeySpec = SecretKeySpec(AES_KEY.toBytes(), "AES")

fun encrypt(text: String, key: String = AES_KEY): String {
    unlocked
    val cipher = Cipher.getInstance("AES/CFB/NoPadding")
    val randomSecureRandom = SecureRandom.getInstance("SHA1PRNG")
    val iv = ByteArray(cipher.blockSize)
    randomSecureRandom.nextBytes(iv)
    val ivSpec = IvParameterSpec(iv)
    val keySpec = if (key == AES_KEY) defaultKeySpec else SecretKeySpec(key.toBytes(), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
    val encrypted = cipher.doFinal(text.toByteArray(Charset.forName("UTF-8")))
    val ivHex = iv.toHex()
    val ivh = ivHex.substring(0, 16)
    val ive = ivHex.substring(16)
    return ive + String(Base64.getEncoder().encode(encrypted)) + ivh
}

fun decrypt(text: String, key: String = AES_KEY): String {
    unlocked
    val iv = text.substring(text.length - 16, text.length) + text.substring(0, 16)
    val content = text.substring(16, text.length - 16)
    val bytes = Base64.getDecoder().decode(content.toByteArray(Charset.forName("UTF-8")))
    val cipher = Cipher.getInstance("AES/CFB/NoPadding")
    val keySpec = if (key == AES_KEY) defaultKeySpec else SecretKeySpec(key.toBytes(), "AES")
    val ivSpec = IvParameterSpec(iv.toBytes())
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
    val original = cipher.doFinal(bytes)
    val originalString = String(original)
    return originalString.trim()
}

private fun String.toBytes(): ByteArray {
    return DatatypeConverter.parseHexBinary(this)
}

private const val HEXES = "0123456789abcdef"
private fun ByteArray.toHex(): String {
    val hex = StringBuilder(2 * size)
    for (b in this) {
        hex.append(HEXES[b.toInt() and 0xF0 shr 4]).append(HEXES[b.toInt() and 0x0F])
    }
    return hex.toString()
}