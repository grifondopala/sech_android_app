package com.example.myapplication

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
import android.widget.TextView

class MainPerson : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_main_person, container, false);

        val prefs: SharedPreferences? = activity?.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE);

        val quizTimeAllow: Long? = prefs?.getLong("quiz_time_allow", 0);

        val errorQuizTextView: TextView = view.findViewById(R.id.main_person_error_quiz_text_view);

        var moveToQuestionsButton: Button = view.findViewById(R.id.move_to_questions_button);

        if (quizTimeAllow !== null && System.currentTimeMillis() < quizTimeAllow){
            Log.d("App error", "Can not use quiz");
            errorQuizTextView.visibility = View.VISIBLE;
        }else{
            errorQuizTextView.visibility = View.INVISIBLE;
            moveToQuestionsButton.setOnClickListener(View.OnClickListener {
                startActivity(Intent(activity, Quiz::class.java))
            })
        }

        return view
    }

}