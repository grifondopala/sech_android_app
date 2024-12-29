package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.myapplication.interfaces.QuestionDto
import com.example.myapplication.interfaces.SignInDto
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException


interface SignInCallback {
    fun onUnsuccess()
    fun onSuccess(responseBody: String)
    fun onFailure()
}
class Auth : Activity() {

    private lateinit var prefs: SharedPreferences
    private val client = OkHttpClient()

    private lateinit var errorText: TextView;
    private lateinit var usernameInput: TextInputEditText;
    private lateinit var passwordInput: TextInputEditText;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        prefs = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        var signInButton: Button = findViewById(R.id.sign_in_button);

        usernameInput = findViewById(R.id.text_input_username);
        passwordInput = findViewById(R.id.text_input_password);

        errorText = findViewById(R.id.sign_in_error_text);

        signInButton.setOnClickListener(View.OnClickListener {
            signIn()
        })
    }

    private fun signIn(){
        var username: String = usernameInput.text.toString()
        var password: String = passwordInput.text.toString()

        if(username.isEmpty() || password.isEmpty()){
            errorText.text = "Username и пароль не должны быть пустыми"
            return;
        }

        sendSignInResponse(username, password, object : SignInCallback {
            override fun onUnsuccess(){
                runOnUiThread{
                    errorText.text = "Неверный username и пароль"
                }
            }

            override fun onSuccess(responseBody: String) {
                runOnUiThread {
                    val gson = Gson()
                    try {
                        val signInDto: SignInDto = gson.fromJson(responseBody, SignInDto::class.java)

                        prefs.edit().putString("token", "1").commit() // deprecated
                        prefs.edit().putString("token", Credentials.basic(username, password)).apply()
                        prefs.edit().putBoolean("isAuth", true).apply()
                        startActivity(Intent(this@Auth, SecondActivity::class.java))
                    } catch (e: Exception) {
                        Log.e("Quiz", "Ошибка парсинга JSON: ${e.message}")
                    }
                }
            }

            override fun onFailure() {
                runOnUiThread{
                    errorText.text = "Проблемы с интернетом, попробуйте еще раз"
                }
            }
        })
        return;
    }

    private fun sendSignInResponse(username: String, password: String, callback: SignInCallback) {

        val media = "application/json; charset=utf-8".toMediaType()
        val body = "{\"username\":\"$username\", \"password\":\"$password\"}".toRequestBody(media)

        val request = Request.Builder()
            .url("${getString(R.string.server_ip)}/api/auth/login")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("App error", e.message.toString());
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful){
                        callback.onUnsuccess()
                        return
                    }

                    callback.onSuccess(response.body.string())
                }
            }
        })
    }
}