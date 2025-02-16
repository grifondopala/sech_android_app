import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import android.net.Uri
import com.example.myapplication.helpers.HttpException
import okhttp3.MultipartBody

class ProfileApi(private val client: OkHttpClient, private val serverIp: String, private val context: Context, private val credentials: String) {

    @Throws(IOException::class)
    suspend fun sendUploadAvatarResponse(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IOException("Could not open input stream")

            val bytes = inputStream.readBytes()
            inputStream.close()

            val mediaType = "image/*".toMediaType()
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "avatar.jpg", bytes.toRequestBody(mediaType))
                .build()

            val request = Request.Builder()
                .url("$serverIp/api/user/info/uploadAvatar")
                .header("Authorization", credentials)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                response.close()
                throw IOException("Unsuccessful upload profile avatar. Code: ${response.code} - ${response.message}")
            }

            response.body.string() ?: throw IOException("Response body is null")
        }
    }

    @Throws(IOException::class, HttpException::class)
    suspend fun sendLoadProfileResponse(): String {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("$serverIp/api/user/info/patient")
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