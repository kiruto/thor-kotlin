package com.exyui.thor.core.cache

import com.exyui.thor.HEAP_SIZE
import com.exyui.thor.OFF_HEAP_SIZE
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder.heap
import org.ehcache.config.units.MemoryUnit

/**
 * Created by yuriel on 1/18/17.
 */
val cacheConfig = CacheConfigurationBuilder.newCacheConfigurationBuilder(
        String::class.java, String::class.java,
        heap(HEAP_SIZE).offheap(OFF_HEAP_SIZE, MemoryUnit.MB)
).build()