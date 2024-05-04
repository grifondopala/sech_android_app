package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class Auth : Activity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        prefs = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        var signInButton: Button = findViewById(R.id.sign_in_button);
        var loginInput: TextInputEditText = findViewById(R.id.text_input_login);
        var passwordInput: TextInputEditText = findViewById(R.id.text_input_password);
        signInButton.setOnClickListener(View.OnClickListener {
            var login: String = loginInput.text.toString()
            var password: String = passwordInput.text.toString()
            signIn(login, password)
        })
    }

    // поменять, пока что для теста
    fun signIn(login: String, password: String){
        if(login == "1" && password == "1") {
            prefs.edit().putString("token", "1").commit(); // пациент
            prefs.edit().putBoolean("isAuth", true).apply();
            startActivity(Intent(this, SecondActivity::class.java))
        }else if(login == "2" && password == "2"){
            prefs.edit().putString("token", "2").commit(); // врач
            prefs.edit().putBoolean("isAuth", true).apply();
            startActivity(Intent(this, SecondActivity::class.java))
        }
    }

}