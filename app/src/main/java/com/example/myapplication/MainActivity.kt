package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        val prefs: SharedPreferences = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)
        val isAuth: Boolean = prefs.getBoolean("isAuth", false)

        if (!isAuth) {
            startActivity(Intent(this, Auth::class.java))
        } else {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        finish()
    }
}