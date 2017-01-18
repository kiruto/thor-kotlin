package com.exyui.thor.core.plugin

import com.exyui.thor.core.database.Comment
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.exyui.thor.core.database.Thread

/**
 * Created by yuriel on 1/18/17.
 */
internal object Bus {
    private val bus = EventBus()

    private val commentPlugin = mutableListOf<OnNewComment>()

    init {
        bus.register(this)
    }

    fun addPlugin(plugin: Plugin) {
        if (plugin is OnNewComment) {
            commentPlugin.add(plugin)
            plugin.onActivate()
        }
    }

    fun removePlugin(plugin: Plugin) {
        if (plugin is OnNewComment) {
            commentPlugin.remove(plugin)
            plugin.onDisable()
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
                    LIFE.NEW_THREAD -> commentPlugin.forEach { it.onNewThread(event.data as Thread) }
                    LIFE.BEFORE_SAVE -> commentPlugin.forEach {
                        val t = (event.data as Array<*>)[0] as Thread
                        val c = event.data[1] as Comment
                        it.beforeSave(t, c)
                    }
                    LIFE.AFTER_SAVE -> commentPlugin.forEach { it.afterSave(event.data as Comment) }
                }
            }
        }
    }
}