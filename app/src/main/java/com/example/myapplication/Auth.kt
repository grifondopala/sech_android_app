package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.interfaces.SignInDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import java.io.IOException
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.api.AuthApi
import com.example.myapplication.helpers.HttpException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class Auth : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private val client = OkHttpClient()
    private lateinit var authApi: AuthApi

    private lateinit var errorText: TextView;
    private lateinit var usernameInput: EditText;
    private lateinit var passwordInput: EditText;

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authApi = AuthApi(client, getString(R.string.server_ip))

        prefs = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        var signInButton: Button = findViewById(R.id.sign_in_button);

        usernameInput = findViewById(R.id.text_input_username);
        passwordInput = findViewById(R.id.text_input_password);

        errorText = findViewById(R.id.sign_in_error_text);

        signInButton.setOnClickListener(View.OnClickListener {
            signIn()
        })
    }

    private fun signIn() {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            errorText.text = "Username и пароль не должны быть пустыми"
            return
        }

        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = authApi.sendSignInResponse(username, password)

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        val signInDto = gson.fromJson(responseBody, SignInDto::class.java)

                        prefs.edit().putString("token", Credentials.basic(username, password)).apply()
                        prefs.edit().putBoolean("isAuth", true).apply()

                        startActivity(Intent(this@Auth, SecondActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Log.e("Auth", "Ошибка парсинга JSON: ${e.message}")
                        errorText.text = "Ошибка при обработке ответа"
                    }
                }
            } catch (e: HttpException) {
                withContext(mainDispatcher) {
                    errorText.text = "Неверный username или пароль" // Или другое сообщение об ошибке
                    Log.e("Auth", "Ошибка HTTP: ${e.code} - ${e.message}")
                }

            } catch (e: IOException) {
                withContext(mainDispatcher) {
                    errorText.text = "Проблемы с интернетом, попробуйте еще раз"
                    Log.e("Auth", "Сетевая ошибка: ${e.message}")
                }
            } catch (e: Exception) {
                withContext(mainDispatcher) {
                    errorText.text = "Неизвестная ошибка"
                    Log.e("Auth", "Неизвестная ошибка: ${e.message}")
                }
            }
        }
    }
}