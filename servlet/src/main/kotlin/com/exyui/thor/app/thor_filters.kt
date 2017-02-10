package com.exyui.thor.app

import com.exyui.thor.*
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by yuriel on 2/10/17.
 */
@WebFilter("/thor/*") class OriginFilter: Filter {
    override fun destroy() {

    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (response is HttpServletResponse) {
            response.addHeader("Access-Control-Allow-Credentials", "true")
            response.addHeader("Access-Control-Allow-Headers", "Content-Type")
            response.addHeader("Access-Control-Expose-Headers", "Date")

            if (DEBUG) {
                if (request is HttpServletRequest) {
                    val origin = request.getHeader("Origin")
                    if (origin != null) {
                        response.addHeader("Access-Control-Allow-Origin", origin)
                    } else {
                        LOCALHOST_PORTS.forEach {
                            response.addHeader("Access-Control-Allow-Origin", "http://localhost:$it")
                            response.addHeader("Access-Control-Allow-Origin", "https://localhost:$it")
                            response.addHeader("Access-Control-Allow-Origin", "http://127.0.0.1:$it")
                            response.addHeader("Access-Control-Allow-Origin", "https://127.0.0.1:$it")
                        }
                    }
                }
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