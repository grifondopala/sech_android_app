import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class QuizApi(private val client: OkHttpClient, private val serverIp: String, private val credentials: String) {

    @Throws(IOException::class)
    suspend fun sendStartQuizResponse(): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$serverIp/api/questions/start")
                .header("Authorization", credentials)
                .post(RequestBody.create(null, ""))
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                response.close()
                throw IOException("Unsuccessful quiz start: ${response.code} - ${response.message}")
            }

            val responseBody = response.body?.string()
            response.close()

            responseBody ?: throw IOException("Response body is null")
        }
    }

    @Throws(IOException::class) //  Указываем возможные исключения
    suspend fun sendNextQuestionResponse(nextQuestionId: Int): String {
        return withContext(Dispatchers.IO) { // Выполняем в IO потоке
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = ("{\"question_id\": $nextQuestionId}").toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$serverIp/api/questions/get")
                .header("Authorization", credentials)
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Unsuccessful next question: ${response.code} - ${response.message}")
            }

            val responseBody = response.body?.string()
            response.close()

            responseBody ?: throw IOException("Response body is null")
        }
    }

    @Throws(IOException::class)
    suspend fun saveUserResponse(responseIds: MutableList<Int>, passNum: Int): String {
        return withContext(Dispatchers.IO) {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val responseIdsStr = responseIds.joinToString(prefix = "[", separator = ",", postfix = "]")

            Log.d("App error", responseIdsStr)
            val body = ("{\"response_ids\": $responseIdsStr," +
                    "\"pass_num\": $passNum}").toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$serverIp/api/user/response/save")
                .header("Authorization", credentials)
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                response.close()
                throw IOException("Unsuccessful save response: ${response.code} - ${response.message}")
            }

            val responseBody = response.body?.string()
            response.close()

            responseBody ?: throw IOException("Response body is null")
        }
    }
}