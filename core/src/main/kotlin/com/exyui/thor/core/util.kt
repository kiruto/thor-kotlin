package com.exyui.thor.core

/**
 * Created by yuriel on 1/27/17.
 */
fun String.empty(): String? = if (isEmpty()) null else this
infix fun String.ifEmpty(default: String) = if(isEmpty()) default else this