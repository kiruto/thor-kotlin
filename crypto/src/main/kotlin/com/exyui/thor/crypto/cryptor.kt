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


private const val BLOCK_SIZE = 16   // bytes

/**
 * Created by yuriel on 1/16/17.
 * @see: http://exyui.com/article/4/%E4%BD%BF%E7%94%A8crypto-js%E4%B8%8EPyCrypto%E6%89%93%E9%80%A0%E7%9B%B8%E5%AF%B9%E5%AE%89%E5%85%A8%E7%9A%84%E4%BC%A0%E8%BE%93%E5%8D%8F%E8%AE%AE
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

/**
 * @return: pair of Base64(result) and HEX(iv)
 */
fun encryptWithIV(text: String, iv: ByteArray, key: String = AES_KEY): Result {
    unlocked
    val cipher = Cipher.getInstance("AES/CFB/NoPadding")
    val ivSpec = IvParameterSpec(iv)
    val keySpec = if (key == AES_KEY) defaultKeySpec else SecretKeySpec(key.toBytes(), "AES")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
    val encrypted = cipher.doFinal(text.toByteArray(Charset.forName("UTF-8")))
    val ivHex = iv.toHex()
    return Result(String(Base64.getEncoder().encode(encrypted)), ivHex)
}

private fun getRandomIV(): ByteArray {
    val randomSecureRandom = SecureRandom.getInstance("SHA1PRNG")
    val iv = ByteArray(BLOCK_SIZE)
    randomSecureRandom.nextBytes(iv)
    return iv
}

fun String.forceEnc() = encryptWithIV(this, getRandomIV())
fun String.encryptWith(iv: String) = encryptWithIV(this, iv.toBytes()).r

/**
 * Decrypt string with a special IV
 */
fun decryptWithIV(text: String, iv: String, key: String = AES_KEY): String {
    unlocked
    val ivSpec = IvParameterSpec(iv.toBytes())
    val bytes = Base64.getDecoder().decode(text.toByteArray(Charset.forName("UTF-8")))
    val cipher = Cipher.getInstance("AES/CFB/NoPadding")
    val keySpec = if (key == AES_KEY) defaultKeySpec else SecretKeySpec(key.toBytes(), "AES")
    try {
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return String(cipher.doFinal(bytes)).trim()
    } catch (e: Exception) {
        throw BadSignatureException()
    }
}

fun String.decryptWith(iv: String) = decryptWithIV(this, iv)

/**
 * Can be decrypt with a KEY
 */
fun encrypt(text: String, key: String = AES_KEY): String {
    val e = encryptWithIV(text, getRandomIV(), key)
    val encrypted = e.r
    val ivHex = e.iv
    val ivh = ivHex.substring(0, 16)
    val ive = ivHex.substring(16)
    return "$ive$encrypted$ivh"
}

fun String.encrypt(): String = encrypt(this)

/**
 * Can only decrypt texts what encrypted by {@link #encrypt(String, String)}
 */
fun decrypt(text: String, key: String = AES_KEY): String {
    val iv = text.substring(text.length - 16, text.length) + text.substring(0, 16)
    val content = text.substring(16, text.length - 16)
    return decryptWithIV(content, iv, key)
}

fun String.decrypt() = decrypt(this)

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

class BadSignatureException: Exception()
data class Result(val r: String, val iv: String)