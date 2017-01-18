package com.exyui.thor.core.cache

import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.config.builders.CacheManagerBuilder

/**
 * Created by yuriel on 1/18/17.
 */
class ThorCache internal constructor(cacheManager: CacheManager, alias: String) {

    internal val cache: Cache<String, String?> = cacheManager.getCache(alias, String::class.java, String::class.java)

    internal companion object {
        private val cacheManager by lazy {
            val m = CacheManagerBuilder.newCacheManagerBuilder().build()
            m.init()
            m
        }

        private val cache = mutableMapOf<String, ThorCache>()

        private fun create(alias: String): ThorCache {
            cacheManager.createCache(alias, cacheConfig)
            val c = ThorCache(cacheManager, alias)
            this.cache.put(alias, c)
            return c
        }

        operator fun get(alias: String): ThorCache = cache[alias]?: create(alias)

        fun destroy() {
            cache.forEach {
                it.value.close()
                cacheManager.removeCache(it.key)
            }
            cacheManager.close()
        }
    }

    operator fun get(key: String): String? = cache.get(key)
    operator fun set(key: String, value: String) {
        cache.put(key, value)
    }

    fun delete(key: String) {
        cache.remove(key)
    }

    fun close() {
        cache.clear()
    }
}