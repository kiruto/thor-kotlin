package com.exyui.thor.crypto

import org.junit.Assert.*

import org.junit.Test

/**
 * Created by yuriel on 1/18/17.
 */
class CryptoTestSuite {
    val key = "de06013f19b74206bb8ee40742be21e3d203f1fa6213facd5c55b2481b941613"
    val content = "d1b9e2308536cc58cc0d5e1ed569b6023e1759513fb5f6c6972ca52563755aa73c2a26fa4c56ef4fb1a9b7b594267f23f908"
    val result = listOf(
            "15ebd600a34aeb96u+a73m9bm8GrWGiux44oNGDEpGAkeEaroP+h83cs/qf9tDLKzxs2OaJoa3N+hdRQFQQYeMM4HQ2xj8zwpVigiQ8WMZsfIpN+iC1gfqX5URbHUpPdZvzQQsrWeBl3fQ8HMMt/wyzaf5hZXOcSyrArjQ==c9f679c5bb9f351e",
            "8b9f788a76f4cbb8OPvpGL1z3AjIGUwV8O+wzRvKd2yILs5f107YKiPTyiMhhYisPlXrgvYzUxCfN4dDGJYBvtAGnVpcIA+BatujQn5i2gm3OGkJvew8JAkM70TAYH2gb/ik/v7PdTMhaMyHayK/zE8gfW+Z3M0hMCfFcg==5d16c5d3ea8324f9",
            "1c15e3d3fe291ad0grsKgUcEuf3De5+AQBin0zH0kssxeYoeI5UloK9blIQ74mwtD/PUzFMjTgjtFfLq3F+rl7pk6EShsc48aqKJ6kDKC9I4xXZIRA/jr+XHRaxW6bwiLAfOpvEVxeKDjWn405tWzWww2MJpo27EB/FVqw==b71fd266005c9196"
        )

    @Test fun testEncode() {
        val r = encrypt(text = content, key = key)
        assertEquals(disEncrypt(text = r, key = key), content)
    }

    @Test fun testDecode() {
        result.forEach {
            assertEquals(disEncrypt(text = it, key = key), content)
        }
    }
}