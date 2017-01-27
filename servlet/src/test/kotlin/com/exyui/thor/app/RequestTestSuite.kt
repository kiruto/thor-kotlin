package com.exyui.thor.app

import com.exyui.testkits.*
import com.exyui.thor.DEBUG
import com.exyui.thor.HOST_DEBUG
import com.exyui.thor.HTTP_PORT
import com.exyui.thor.core.ifEmpty
import com.exyui.thor.core.model.createObject
import com.exyui.thor.core.model.gson
import com.exyui.thor.core.model.toJson
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/**
 * Created by yuriel on 1/27/17.
 */
class RequestTestSuite {

    private val client by lazy {
        val i = HttpLoggingInterceptor()
        i.level = HttpLoggingInterceptor.Level.BODY
        OkHttpClient.Builder()
                .addInterceptor(i)
                .build()
    }

    private val retrofit = Retrofit.Builder()
            .baseUrl("$HOST_DEBUG:$HTTP_PORT")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    private val service = retrofit.create(ThorService::class.java)

    @Test fun encryptDebug() {
        println("Environment: debug=$DEBUG")
        /**
         * Must throw at release version and should be work at debug environment.
         * `DEBUG mustNot err` also means release must be error
         */
        DEBUG mustNot err {
            val key = "de06013f19b74206bb8ee40742be21e3d203f1fa6213facd5c55b2481b941613"
            val content = "d1b9e2308536cc58cc0d5e1ed569b6023e1759513fb5f6c6972ca52563755aa73c2a26fa4c56ef4fb1a9b7b594267f23f908"
            val encryptResult = service.encrypt(key, content).execute().body().string()
            val decryptResult = service.decrypt(key, encryptResult).execute().body().string()
            decryptResult mustEq content
        }
    }

    @Test fun insertCommentDebug() {
        println("Environment: debug=$DEBUG")
        /**
         * Must throw at release version and should be work at debug environment.
         * `DEBUG mustNot err` also means release must be error
         */
        DEBUG mustNot err {
            val p = getCommentParam()
            println(p.toJson())

            // GET
            service.newCommentDebugGet(p.toJson())
                    .execute()
                    .body()
                    .string()
                    .createObject(NewCommentResult::class)

            // POST
            service.newCommentDebugPost(p)
                    .execute()
                    .body()
                    .string()
                    .createObject(NewCommentResult::class)
        }
    }

    private interface ThorService {
        @GET(URL_TEST_ENCRYPT) fun encrypt(@Query("key")key: String, @Query("content") content: String): Call<ResponseBody>
        @GET(URL_TEST_DECRYPT) fun decrypt(@Query("key")key: String, @Query("content") content: String): Call<ResponseBody>
        @GET(URL_NEW_COMMENT_DEBUG) fun newCommentDebugGet(@Query("d") data: String): Call<ResponseBody>
        @POST(URL_NEW_COMMENT_DEBUG) fun newCommentDebugPost(@Body data: NewCommentParameter): Call<ResponseBody>
    }

    private fun getCommentParam(
            uri: String = "",
            title: String = "",
            text: String = "",
            author: String? = null,
            email: String? = null,
            website: String? = null,
            parent: Int? = null
    ): NewCommentParameter {

        return NewCommentParameter(
                uri = uri ifEmpty randomURL(),
                title = title ifEmpty randomAlphaNumOfLength(3, 10),
                text = text ifEmpty randomAlphaNumOfLength(10, 100),
                author = author?: aon(randomAlphaNumOfLength(5, 10)),
                email = email?: aon(randomEmail()),
                website = website?: aon(randomWebsite()),
                parent = parent
        )
    }

    fun randomURL() = "http://test.exyui.com/${randomAlphaNumOfLength(10)}"
}