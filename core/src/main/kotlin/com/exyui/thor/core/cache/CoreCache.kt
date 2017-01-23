package com.exyui.thor.core.cache

/**
 * Created by yuriel on 1/23/17.
 */
internal enum class CoreCache(val cache: ThorCache) {
    USER_IDENTIFY(ThorCache["user_identify"]),
    SESSION(ThorCache["session"])
}