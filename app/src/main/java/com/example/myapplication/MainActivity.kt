package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myapplication.helpers.AlarmReceiver

class MainActivity : ComponentActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        val prefs: SharedPreferences = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)
        val isAuth: Boolean = prefs.getBoolean("isAuth", false)

        startActivity(Intent(this, SecondActivity::class.java))

        if (!isAuth) {
            startActivity(Intent(this, Auth::class.java))
        }
        else {
            startActivity(Intent(this, SecondActivity::class.java))
        }

        finish()
    }
}