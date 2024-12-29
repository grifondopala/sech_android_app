package com.example.myapplication.helpers

import android.content.Context
import android.content.SharedPreferences

class BaseAuth {
    companion object{
         fun getCredentials(context: Context): String {
            val prefs: SharedPreferences = context.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE);

            val token: String = prefs.getString("token", "").toString();

            return token;
        }
    }
}