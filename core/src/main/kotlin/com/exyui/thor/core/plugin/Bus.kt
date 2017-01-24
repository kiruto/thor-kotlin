package com.exyui.thor.core.plugin

import com.exyui.thor.core.database.Comment
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.exyui.thor.core.database.Thread

/**
 * Created by yuriel on 1/18/17.
 */
internal object Bus {
    private val bus = EventBus(javaClass.name)

    private val newCommentPlugin = mutableListOf<OnNewComment>()
    private val editCommentPlugin = mutableListOf<OnEditComment>()
    private val deleteCommentPlugin = mutableListOf<OnDeleteComment>()

    init {
        bus.register(this)
    }

    fun addPlugin(plugin: Plugin) {
        when(plugin) {
            is OnNewComment -> {
                newCommentPlugin.add(plugin)
                plugin.onActivate(plugin.name())
            }
            is OnEditComment -> {
                editCommentPlugin.add(plugin)
                plugin.onActivate(plugin.name())
            }
            is OnDeleteComment -> {
                deleteCommentPlugin.add(plugin)
                plugin.onActivate(plugin.name())
            }
        }
    }

    fun removePlugin(plugin: Plugin) {
        when(plugin) {
            is OnNewComment -> {
                newCommentPlugin.remove(plugin)
                plugin.onDisable(plugin.name())
            }
            is OnEditComment -> {
                editCommentPlugin.remove(plugin)
                plugin.onDisable(plugin.name())
            }
            is OnDeleteComment -> {
                deleteCommentPlugin.remove(plugin)
                plugin.onDisable(plugin.name())
            }
        }
    }

    fun p(event: Event) {
        bus.post(event)
    }

    fun p(target: TARGET, life: LIFE, vararg data: Any) {
        p(Event(target, life, data))
    }

    @Suppress("unused")
    @Subscribe fun receive(event: Event) {
        when(event.target) {
            COMMENT.NEW -> {
                when(event.life) {
                    LIFE.NULL -> { /* Nothing needed to do here */ }
                    LIFE.NEW_THREAD -> newCommentPlugin.forEach { it.onNewThread(event.what() as Thread) }
                    LIFE.BEFORE_SAVE -> newCommentPlugin.forEach {
                        val t = event.data[0] as Thread
                        val c = event.data[1] as Comment
                        it.beforeSave(t, c)
                    }
                    LIFE.AFTER_SAVE -> newCommentPlugin.forEach { it.afterSave(event.what() as Comment) }
                    LIFE.FINISH -> newCommentPlugin.forEach {
                        val t = event.data[0] as Thread
                        val c = event.data[1] as Comment
                        it.beforeSave(t, c)
                    }
                }
            }
            COMMENT.EDIT -> {
                editCommentPlugin.forEach { it.onEdit(event.what() as Comment) }
            }
            COMMENT.DELETE -> {
                deleteCommentPlugin.forEach { it.onDelete(event.what() as Int) }
            }
        }
    }
}