package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.api.MainPersonApi
import com.example.myapplication.helpers.BaseAuth
import com.example.myapplication.interfaces.QuizListDto
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException

class MainPerson : Fragment() {

    private val client = OkHttpClient()
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var mainPersonApi: MainPersonApi;
    private lateinit var credentials: String;

    private lateinit var quizList: QuizListDto;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_main_person, container, false);

        credentials = BaseAuth.getCredentials(view.context);
        mainPersonApi = MainPersonApi(client, getString(R.string.server_ip), credentials);

        LoadQuizList(view)

        return view
    }

    private fun LoadQuizList(view: View){
        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = mainPersonApi.loadQuizList()

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        quizList = gson.fromJson(responseBody, QuizListDto::class.java)
                        DisplayQuizList(view)
                    } catch (e: Exception) {
                        Log.e("App error", "Quiz list: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            } catch (e: IOException) {
                withContext(mainDispatcher) {
                    Log.e("App error", "Ошибка сети при получении списка опросов: ${e.message}")
                }
            }
        }
    }

    private fun DisplayQuizList(view: View){
        val quizListLayout: LinearLayout = view.findViewById(R.id.quizList)

        for (quizItem in quizList.list) {
            val cardView: CardView = LayoutInflater.from(view.context)
                .inflate(R.layout.quiz_card, quizListLayout, false) as CardView

            val titleTextView = cardView.findViewById<TextView>(R.id.cardTitle)
            val descriptionTextView = cardView.findViewById<TextView>(R.id.cardText)

            titleTextView.text = quizItem.name
            descriptionTextView.text = quizItem.description

            cardView.setOnClickListener(View.OnClickListener {
                var intent = Intent(activity, Quiz::class.java)
                intent.putExtra("quizId", quizItem.quiz_id);
                startActivity(intent)
            })

            quizListLayout.addView(cardView)
        }
    }

}