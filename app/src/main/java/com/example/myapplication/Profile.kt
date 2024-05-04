package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class Profile : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        val prefs: SharedPreferences? = activity?.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        var quitFromAccountButton: Button = view.findViewById(R.id.quit_from_account_button);
        quitFromAccountButton.setOnClickListener(View.OnClickListener {
            prefs?.edit()?.putBoolean("isAuth", false)?.apply();
            startActivity(Intent(activity, Auth::class.java))
            activity?.finish()
        })

        return view
    }

}