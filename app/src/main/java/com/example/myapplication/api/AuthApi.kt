package com.example.myapplication.api

import com.example.myapplication.helpers.HttpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AuthApi(private val client: OkHttpClient, private val serverIp: String) {
    @Throws(IOException::class, HttpException::class)
    suspend fun sendSignInResponse(username: String, password: String): String {
        return withContext(Dispatchers.IO) {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = "{\"username\":\"$username\", \"password\":\"$password\"}".toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$serverIp/api/auth/login")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                response.close()
                throw HttpException(response.code, response.message)
            }

            response.body.string() ?: throw IOException("Response body is null")
        }
    }
}