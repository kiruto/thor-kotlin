package com.exyui.thor.core.plugin

/**
 * Created by yuriel on 1/18/17.
 */

internal interface TARGET

internal enum class COMMENT(private val alias: String): TARGET {
    NEW("new"),
    DELETE("delete"),
    EDIT("edit");

    override fun toString() = alias
}

internal enum class LIFE(private val alias: String) {
    NULL(""),
    NEW_THREAD("new-thread"),
    BEFORE_SAVE("before-save"),
    AFTER_SAVE("after-save"),
    FINISH("finish");

    override fun toString(): String = alias
}

internal data class Event(val target: TARGET, val life: LIFE, val data: Array<out Any>) {
    fun what() = data[0]

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other?.toString()
    }

    override fun toString() = "$target:$life:$data"
}
