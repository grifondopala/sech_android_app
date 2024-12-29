package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.helpers.BaseAuth
import com.example.myapplication.interfaces.PatientDto
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException

interface ProfileCallback {
    fun onUnsuccess()
    fun onSuccess(responseBody: String)
    fun onFailure()
}

class Profile : Fragment() {

    private val client = OkHttpClient()
    private lateinit var credentials: String;
    private lateinit var userInfo: PatientDto;

    private lateinit var firstNameTextField: TextView;
    private lateinit var middleNameTextField: TextView;
    private lateinit var lastNameTextField: TextView;
    private lateinit var snilsTextField: TextView;
    private lateinit var phoneTextField: TextView;
    private lateinit var emailTextField: TextView;

    private lateinit var avatarImageView: ImageView;

    private lateinit var uploadAvatarResultLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        val prefs: SharedPreferences? = activity?.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        val context = requireContext()
        credentials = BaseAuth.getCredentials(context);

        var quitFromAccountButton: Button = view.findViewById(R.id.quit_from_account_button);
        quitFromAccountButton.setOnClickListener(View.OnClickListener {
            prefs?.edit()?.putBoolean("isAuth", false)?.apply();
            startActivity(Intent(activity, Auth::class.java))
            activity?.finish()
        })

        var uploadAvatarButton: Button = view.findViewById(R.id.upload_avatar_button);

        uploadAvatarButton.setOnClickListener(View.OnClickListener {
            pickImageFromGallery();
        })

        firstNameTextField = view.findViewById(R.id.profile_name_text_view);
        middleNameTextField = view.findViewById(R.id.profile_middle_name_text_view);
        lastNameTextField = view.findViewById(R.id.profile_last_name_text_view);
        snilsTextField = view.findViewById(R.id.profile_snils_text_view);
        phoneTextField = view.findViewById(R.id.profile_phone_text_view);
        emailTextField = view.findViewById(R.id.profile_email_text_view);
        avatarImageView = view.findViewById(R.id.profile_avatar_image);

        uploadAvatarResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        val selectedImageUri = intent.data
                        selectedImageUri?.let { uri ->
                            sendUploadResponse(uri, object : ProfileCallback {
                                override fun onUnsuccess(){
                                    activity?.runOnUiThread{

                                    }
                                }

                                override fun onSuccess(responseBody: String) {
                                    activity?.runOnUiThread {
                                        var imageURL = "${getString(R.string.server_ip)}/public/" + userInfo.avatar;
                                        Picasso.get()
                                            .load(imageURL)
                                            .into(avatarImageView)
                                    }
                                }

                                override fun onFailure() {
                                    activity?.runOnUiThread{

                                    }
                                }
                            })
                        }
                    }
                }
            }


        loadProfile();

        return view
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        uploadAvatarResultLauncher.launch(intent)
    }

    private fun DispayPatientInfo(){
        try{

        }catch (e: Exception){

        }

        firstNameTextField.text = userInfo.first_name;
        middleNameTextField.text = userInfo.middle_name;
        lastNameTextField.text = userInfo.last_name;
        phoneTextField.text = userInfo.phone;
        emailTextField.text = userInfo.email;
        snilsTextField.text = userInfo.snils;
    }

    private fun loadProfile(){
        sendLoadProfileResponse(object : ProfileCallback {
            override fun onUnsuccess(){
                activity?.runOnUiThread{

                }
            }

            override fun onSuccess(responseBody: String) {
                activity?.runOnUiThread {
                    val gson = Gson()
                    try {
                        userInfo = gson.fromJson(responseBody, PatientDto::class.java);

                        DispayPatientInfo();
                    } catch (e: Exception) {
                        Log.e("App error", "Profile: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            }

            override fun onFailure() {
                activity?.runOnUiThread{

                }
            }
        })
    }

    private fun sendUploadResponse(imageUri: Uri, callback: ProfileCallback){
        val inputStream = context?.contentResolver?.openInputStream(imageUri)
        val file = File(context?.cacheDir, "my_image_4")

        if(inputStream != null){
            val bytes = inputStream.readBytes()
            file.writeBytes(bytes)
        }else {
            return;
        }

        val media = "multipart/form-data".toMediaType()
        val requestBody = file.asRequestBody(media)

        val request = Request.Builder()
            .url("${getString(R.string.server_ip)}/api/user/info/uploadAvatar")
            .header("Authorization", credentials)
            .post(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.name, requestBody)
                    .build()
            )
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("App error", e.message.toString());
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful){
                        Log.d("App error", "Unsuccessful upload profile avatar");
                        callback.onUnsuccess()
                        return
                    }

                    callback.onSuccess(response.body.string())
                }
            }
        })
    }

    private fun sendLoadProfileResponse(callback: ProfileCallback) {
        val request = Request.Builder()
            .url("${getString(R.string.server_ip)}/api/user/info/patient")
            .header("Authorization", credentials)
            .post(RequestBody.create(null, ""))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("App error", e.message.toString());
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful){
                        Log.d("App error", "Unsuccessful request get profile");
                        callback.onUnsuccess()
                        return
                    }

                    callback.onSuccess(response.body.string())
                }
            }
        })
    }

}