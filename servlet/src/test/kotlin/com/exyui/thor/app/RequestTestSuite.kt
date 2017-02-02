package com.exyui.thor.app

import org.junit.Assert.*
import com.exyui.testkits.*
import com.exyui.thor.DEBUG
import com.exyui.thor.HOST_DEBUG
import com.exyui.thor.HTTP_PORT
import com.exyui.thor.core.ifEmpty
import com.exyui.thor.core.model.*
import com.exyui.thor.crypto.encryptWith
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

    private val debug = retrofit.create(DebugThorService::class.java)

    @Test fun encryptDebug() {
        println("Environment: debug=$DEBUG")
        /**
         * Must throw at release version and should be work at debug environment.
         * `DEBUG mustNot err` also means release must be error
         */
        DEBUG mustNot err {
            val key = "de06013f19b74206bb8ee40742be21e3d203f1fa6213facd5c55b2481b941613"
            val content = "d1b9e2308536cc58cc0d5e1ed569b6023e1759513fb5f6c6972ca52563755aa73c2a26fa4c56ef4fb1a9b7b594267f23f908"
            val encryptResult = debug.encrypt(key, content).execute().body().string()
            val decryptResult = debug.decrypt(key, encryptResult).execute().body().string()
            decryptResult mustEq content
        }
    }

    @Test fun curdCommentDebug() {
        println("Environment: debug=$DEBUG")
        /**
         * Must throw at release version and should be work at debug environment.
         * `DEBUG mustNot err` also means release must be error
         */
        DEBUG mustNot err {
            val p1 = getCommentParam()
            println(p1.toJson())

            // GET new comment
            val comm1 = debug.newCommentDebugGet(p1.toJson())
                    .execute()
                    .body()
                    .string()
                    .createObject(NewCommentResult::class)

            // POST new comment
            val comm2 = debug.newCommentDebugPost(p1)
                    .execute()
                    .body()
                    .string()
                    .createObject(NewCommentResult::class)

            // PUT edit comment
            val session2 = (p1.email?: comm1.ip).encryptWith(comm1.token)
            val p2 = getEditCommentParam(session2, comm1.comment.id!!)
            val edit2 = debug.editComment(p2)
                    .execute()
                    .body()
                    .string()
                    .createObject(EditCommentResult::class)

            // Edit twice with the renewed token
            val session3 = session2.encryptWith(edit2.newToken)
            val p3 = getEditCommentParam(session3, edit2.comment.id!!)
            val edit3 = debug.editComment(p3)
                    .execute()
                    .body()
                    .string()
                    .createObject(EditCommentResult::class)

            // Must error when edit with a wrong session token
            assertErr {
                val p = getEditCommentParam(randomEmail(), comm1.ip, comm1.token, comm1.comment.id!!)
                debug.editComment(p)
                        .execute()
                        .body()
                        .string()
                        .createObject(EditCommentResult::class)
            }

            // Fetch comment
            val fetch = debug.fetchComment(p1.uri)
                    .execute()
                    .body()
                    .string()
                    .createObject(FetchResult::class)

            // Must error when delete with a wrong session token
            assertErr {
                val p = DeleteCommentParameter(session3, edit3.comment.id!!)
                debug.deleteComment(p.toJson())
                        .execute()
                        .body()
                        .string()
                        .createComment()
            }

            // Delete comment
            val session4 = session3.encryptWith(edit3.newToken)
            val p4 = DeleteCommentParameter(session4, edit3.comment.id!!)
            val delete4 = debug.deleteComment(p4.toJson())
                    .execute()
                    .body()
                    .string()
                    .createComment()
            assertNull(delete4)
        }
    }

    private interface DebugThorService {
        @GET(URL_TEST_ENCRYPT) fun encrypt(@Query("key") key: String,
                                           @Query("content") content: String): Call<ResponseBody>

        @GET(URL_TEST_DECRYPT) fun decrypt(@Query("key") key: String,
                                           @Query("content") content: String): Call<ResponseBody>

        @GET(URL_NEW_COMMENT_DEBUG) fun newCommentDebugGet(@Query("d") data: String): Call<ResponseBody>

        @POST(URL_NEW_COMMENT_DEBUG) fun newCommentDebugPost(@Body data: NewCommentParameter): Call<ResponseBody>

        @PUT(URL_COMMENT_DEBUG) fun editComment(@Body data: EditCommentParameter): Call<ResponseBody>

        @DELETE(URL_COMMENT_DEBUG) fun deleteComment(@Query("d") data: String): Call<ResponseBody>

        @GET(URL_FETCH_DEBUG) fun fetchComment(@Query("uri") uri: String,
                                               @Query("after") after: Double? = null,
                                               @Query("parent") parent: Int? = null,
                                               @Query("limit") limit: Int? = null,
                                               @Query("plain") plain: Int? = null,
                                               @Query("nestedLimit") nestedLimit: Int? = null): Call<ResponseBody>
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
                uri = uri ifEmpty randomReferredURL(),
                title = title ifEmpty randomAlphaNumOfLength(3, 10),
                text = text ifEmpty randomAlphaNumOfLength(10, 100),
                author = author?: aon(randomAlphaNumOfLength(5, 10)),
                email = email?: aon(randomEmail()),
                website = website?: aon(randomWebsite()),
                parent = parent
        )
    }

    private fun getEditCommentParam(
            session: String,
            id: Int,
            text: String? = null,
            author: String? = null,
            website: String? = null
    ): EditCommentParameter {
        return EditCommentParameter(
                session, id,
                text?: aon(randomAlphaNumOfLength(10, 100)),
                author?: aon(randomAlphaNumOfLength(3, 10)),
                website?: aon(randomWebsite()))
    }

    private fun getEditCommentParam(
            email: String? = null,
            remoteAddr: String,
            token: String,
            id: Int,
            text: String? = null,
            author: String? = null,
            website: String? = null
    ) = getEditCommentParam((email?: remoteAddr).encryptWith(token), id, text, author, website)


    private fun randomReferredURL() = "http://test.exyui.com/${randomAlphaNumOfLength(10)}"
}