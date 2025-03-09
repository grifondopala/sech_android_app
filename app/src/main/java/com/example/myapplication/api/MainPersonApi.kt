package com.example.myapplication.api

import com.example.myapplication.helpers.HttpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class MainPersonApi(private val client: OkHttpClient, private val serverIp: String, private val credentials: String) {

    @Throws(IOException::class, HttpException::class)
    suspend fun loadQuizList(): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$serverIp/api/quiz/list")
                .header("Authorization", credentials)
                .get()
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