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

class MainPerson : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_main_person, container, false)

        var moveToQuestionsButton: Button = view.findViewById(R.id.move_to_questions_button);
        moveToQuestionsButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity, Quiz::class.java))
        })

        return view
    }

}