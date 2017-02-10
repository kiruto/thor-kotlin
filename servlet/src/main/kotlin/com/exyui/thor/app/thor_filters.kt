package com.exyui.thor.app

import com.exyui.thor.*
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 2/10/17.
 */
@WebFilter("/thor/*") class OriginFilter: Filter {
    override fun destroy() {

    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (response is HttpServletResponse) {
            if (DEBUG) {
                response.addHeader("Access-Control-Allow-Origin", ORIGIN_DEBUG)
            } else {
                HOST_RELEASE.forEach {
                    response.addHeader("Access-Control-Allow-Origin", it)
                }
            }
        }
        chain.doFilter(request, response)
    }

    override fun init(filterConfig: FilterConfig) {

    }
}