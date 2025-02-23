package com.example.myapplication

import ProfileApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.helpers.BaseAuth
import com.example.myapplication.helpers.HttpException
import com.example.myapplication.interfaces.PatientDto
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException


class Profile : Fragment() {

    private val client = OkHttpClient()
    private lateinit var credentials: String;
    private lateinit var profileApi: ProfileApi

    private lateinit var userInfo: PatientDto;

    private lateinit var firstNameTextField: TextView;
    private lateinit var middleNameTextField: TextView;
    private lateinit var lastNameTextField: TextView;
    private lateinit var snilsTextField: TextView;
    private lateinit var phoneTextField: TextView;
    private lateinit var emailTextField: TextView;

    private lateinit var avatarImageView: ImageView;

    private lateinit var uploadAvatarResultLauncher : ActivityResultLauncher<Intent>

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs: SharedPreferences? = activity?.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        val context = requireContext()
        credentials = BaseAuth.getCredentials(context);

        profileApi = ProfileApi(client, getString(R.string.server_ip), context, credentials);

        val quitFromAccountButton: Button = view.findViewById(R.id.quit_from_account_button);
        quitFromAccountButton.setOnClickListener(View.OnClickListener {
            prefs?.edit()?.putBoolean("isAuth", false)?.apply();
            startActivity(Intent(activity, Auth::class.java))
            activity?.finish()
        })

        val uploadAvatarButton: Button = view.findViewById(R.id.upload_avatar_button);

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
                if (result.resultCode != Activity.RESULT_OK) {
                    return@registerForActivityResult;
                }

                val intent = result.data

                if (intent == null) {
                    return@registerForActivityResult;
                }

                val selectedImageUri = intent.data
                selectedImageUri?.let { uri ->
                    lifecycleScope.launch(ioDispatcher) {
                        try {
                            val responseBody = profileApi.sendUploadAvatarResponse(uri)

                            withContext(mainDispatcher) {
                                DisplayAvatar()
                            }
                        } catch (e: Exception) {
                            Log.e("App error", "Ошибка при загрузке аватара: ${e.message}")
                            withContext(mainDispatcher) {

                            }
                        }
                    }
                }
            }

        loadProfile()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        uploadAvatarResultLauncher.launch(intent)
    }

    private fun DispayPatientInfo(){
        firstNameTextField.text = userInfo.first_name;
        middleNameTextField.text = userInfo.middle_name;
        lastNameTextField.text = userInfo.last_name;
        phoneTextField.text = userInfo.phone;
        emailTextField.text = userInfo.email;
        snilsTextField.text = userInfo.snils;

        DisplayAvatar()
    }

    private fun DisplayAvatar() {
        val timestamp = System.currentTimeMillis()
        val imageURL = "${getString(R.string.server_ip)}/public/avatars/" + userInfo.avatar + "?timestamp=$timestamp"

        Picasso.get()
            .load(imageURL)
            .error(R.drawable.default_avatar)
            .into(avatarImageView)
    }

    private fun loadProfile() {
        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = profileApi.sendLoadProfileResponse()
                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        userInfo = gson.fromJson(responseBody, PatientDto::class.java)
                        DispayPatientInfo()
                    } catch (e: Exception) {
                        Log.e("App error", "Profile: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            } catch (e: IOException) {
                Log.e("App error", "Ошибка при загрузке профиля: ${e.message}")

                withContext(mainDispatcher) {

                }
            } catch (e: HttpException) {
                Log.e("App error", "HTTP ошибка при загрузке профиля: ${e.message}")
                withContext(mainDispatcher) {

                }
            }
        }
    }
}