package com.exyui.thor.core

/**
 * Created by yuriel on 1/15/17.
 */
open class ThorException(msg: String): Exception(msg)
class ThorBadRequest(reason: String?): ThorException("Bad request: $reason")
class ThorNotFound(things: String?): ThorException("Not found $things")
class ThorForbidden(): ThorException("Forbidden")
