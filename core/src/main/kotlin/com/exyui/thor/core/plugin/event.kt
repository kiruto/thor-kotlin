package com.exyui.thor.core.plugin

/**
 * Created by yuriel on 1/18/17.
 */

internal interface TARGET

internal enum class COMMENT(private val alias: String): TARGET {
    NEW("new");

    override fun toString() = alias
}

internal enum class LIFE(private val alias: String) {
    NEW_THREAD("new-thread"),
    BEFORE_SAVE("before-save"),
    AFTER_SAVE("after-save");

    override fun toString(): String = alias
}

internal data class Event(val target: TARGET, val life: LIFE, val data: Any) {
    override fun toString() = "$target:$life:$data"
}