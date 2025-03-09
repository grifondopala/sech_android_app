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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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

        loadQuizList(view)

        return view
    }

    private fun loadQuizList(view: View){
        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = mainPersonApi.loadQuizList()

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        quizList = gson.fromJson(responseBody, QuizListDto::class.java)
                        displayQuizList(view)
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

    private fun displayQuizList(view: View){
        val quizListLayout: LinearLayout = view.findViewById(R.id.quizList)

        val sortedQuizList = quizList.list.sortedByDescending { it.is_available }

        for (quizItem in sortedQuizList) {
            val cardView: CardView = LayoutInflater.from(view.context)
                .inflate(R.layout.quiz_card, quizListLayout, false) as CardView

            val titleTextView = cardView.findViewById<TextView>(R.id.quizTitle)
            val descriptionTextView = cardView.findViewById<TextView>(R.id.quizDescription)
            val statusTextView = cardView.findViewById<TextView>(R.id.quizStatus)

            titleTextView.text = quizItem.name
            descriptionTextView.text = quizItem.description

            if (quizItem.is_available){
                statusTextView.text = "Можно пройти прямо сейчас!"
                statusTextView.setTextColor(resources.getColor(R.color.primary_black, null));
                cardView.setOnClickListener(View.OnClickListener {
                    val intent = Intent(activity, Quiz::class.java)
                    intent.putExtra("quizId", quizItem.quiz_id);
                    intent.putExtra("timeToPassAgain", quizItem.time_to_pass_again);
                    intent.putExtra("name", quizItem.name);
                    startActivity(intent)
                })
            } else {
                val time = transformUtcTimeToLocalFormattedString(quizItem.next_time_can);
                if (time != null){
                    statusTextView.text = "Нельзя пройти до ${time}!"
                    statusTextView.setTextColor(resources.getColor(R.color.default_red, null));
                }
            }

            quizListLayout.addView(cardView)
        }
    }

    private fun transformUtcTimeToLocalFormattedString(utcTimeString: String): String? {
        try {
            val partTime = utcTimeString.substring(0, 19)
            val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            utcFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = utcFormat.parse(partTime) ?: return null

            val localFormat = SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault())
            localFormat.timeZone = TimeZone.getDefault()
            return localFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

}