package com.example.myapplication

import QuizApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide;
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myapplication.helpers.AlarmReceiver
import com.example.myapplication.helpers.AnswersAdapter
import com.example.myapplication.helpers.BaseAuth
import com.example.myapplication.helpers.GlideImageListener
import com.example.myapplication.interfaces.QuestionDto
import com.example.myapplication.interfaces.SaveResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.IOException

class Quiz : AppCompatActivity() {

    private val client = OkHttpClient()

    private lateinit var prefs: SharedPreferences;

    private lateinit var questionText: TextView;
    private lateinit var answersContainer: LinearLayout;
    private lateinit var nextQuestionButton: Button;
    private lateinit var questionLabel: TextView;
    private lateinit var errorQuizLabel: TextView;
    private lateinit var endQuizButton: Button;
    private lateinit var mainQuizImage: ImageView;

    private lateinit var quizLoaderLayout: ConstraintLayout;
    private lateinit var quizLayout: ScrollView;
    private lateinit var quizEndLayout: LinearLayout;

    private lateinit var currentQuestion: QuestionDto;
    private var quizId = 1;
    private var timeToPassAgain: Long = 50000;
    private var name = "";

    private lateinit var credentials: String;

    private var responseIndexes = mutableListOf<Int>()
    private var passNum: Int = 1;

    private lateinit var quizApi: QuizApi;

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    private lateinit var answersRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        quizId = intent.getIntExtra("quizId", 1)
        timeToPassAgain = intent.getLongExtra("timeToPassAgain", 50000)
        name = intent.getStringExtra("name").toString()

        prefs = this.getSharedPreferences("com.example.myapplication", Context.MODE_PRIVATE)

        quizLoaderLayout = findViewById(R.id.quiz_loader_layout)
        quizLayout = findViewById(R.id.quiz_layout)
        quizEndLayout = findViewById(R.id.quiz_end_layout);

        questionText = findViewById(R.id.question_text)
        questionLabel = findViewById(R.id.question_label)
        answersContainer = findViewById(R.id.answers_container)
        nextQuestionButton = findViewById(R.id.next_question_button)
        errorQuizLabel = findViewById(R.id.error_quiz_label)
        endQuizButton = findViewById(R.id.end_quiz_to_main_button)
        mainQuizImage = findViewById(R.id.quiz_main_image)

        answersRecyclerView = findViewById(R.id.answersRecyclerView)
        answersRecyclerView.setItemViewCacheSize(20)
        answersRecyclerView.setHasFixedSize(true)
        answersRecyclerView.layoutManager = LinearLayoutManager(this)

        credentials = BaseAuth.getCredentials(this);
        quizApi = QuizApi(client, getString(R.string.server_ip), credentials);

        nextQuestionButton.setOnClickListener(View.OnClickListener {
            saveResponse()
        })

        endQuizButton.setOnClickListener(View.OnClickListener {
            endQuiz()
        })

        startQuiz()
    }

    private fun endQuiz(){
        startActivity(Intent(this, SecondActivity::class.java))
        finish()
    }

    private fun showLoader(){
        quizLoaderLayout.visibility = View.VISIBLE
        quizLayout.visibility = View.INVISIBLE
    }

    private fun hideLoader(){
        quizLoaderLayout.visibility = View.INVISIBLE
        quizLayout.visibility = View.VISIBLE
    }

    private fun showEnd(){
        quizLayout.visibility = View.INVISIBLE
        quizEndLayout.visibility = View.VISIBLE
    }

    private fun changeErrorVisibility(isVisible: Boolean){
        if(isVisible){
            errorQuizLabel.visibility = View.VISIBLE
            return;
        }

        errorQuizLabel.visibility = View.INVISIBLE
    }


    private fun saveResponse() {
        if (responseIndexes.size == 0) {
            changeErrorVisibility(true)
            return
        }

        val responseIds: MutableList<Int> = responseIndexes.map { index ->
            currentQuestion.options[index].response_id
        }.toMutableList()

        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = quizApi.saveUserResponse(responseIds, passNum, quizId)

                withContext(mainDispatcher) {
                    val gson = Gson()

                    try {
                        val response = gson.fromJson(responseBody, SaveResponseDto::class.java)

                        if (response.is_ended) {
                            showEnd()

                            try {
                                generateNotification()
                            } catch (e: Error){
                                Log.d("App error", "Error while schedule notification")
                            }

                        }else {
                            nextQuestion()
                        }
                    } catch (e: Exception) {
                        Log.e("App error", "Save response: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            } catch (e: IOException) {
                withContext(mainDispatcher) {
                    Log.e("App error", "Ошибка сети при сохранении ответа: ${e.message}")
                }
            }
        }
    }

    private fun nextQuestion() {
        val nextQuestionId = currentQuestion.options[responseIndexes[0]].next_question_id;

        showLoader()

        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = quizApi.sendNextQuestionResponse(nextQuestionId, quizId)

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        currentQuestion = gson.fromJson(responseBody, QuestionDto::class.java)

                        responseIndexes = mutableListOf()

                        displayQuestion()
                    } catch (e: Exception) {
                        Log.e("App error", "Next question: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            } catch (e: IOException) {
                withContext(mainDispatcher) {
                    hideLoader()
                    Log.e("App error", "Ошибка сети при получении следующего вопроса: ${e.message}")
                }
            }
        }
    }

    private fun generateNotification(){
         if (AlarmReceiver.canScheduleExactAlarms(this)){
             AlarmReceiver.scheduleNotification(this, timeToPassAgain, quizId, name)
         }
    }

    private fun startQuiz() {
        showLoader()

        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = quizApi.sendStartQuizResponse(quizId)

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        currentQuestion = gson.fromJson(responseBody, QuestionDto::class.java)
                        passNum = currentQuestion.pass_num
                        displayQuestion()
                    } catch (e: Exception) {
                        Log.e("App error", "Start quiz: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            } catch (e: IOException) {
                withContext(mainDispatcher) {
                    hideLoader()
                    Log.e("App error", "Ошибка сети при запуске quiz: ${e.message}")
                }
            }
        }
    }

    private fun displayQuestion() {
        questionText.text = currentQuestion.question_text
        questionLabel.text = if (currentQuestion.is_multiple_choice)
            "Можно выбрать несколько вариантов." else "Выберите один вариант."

        answersRecyclerView.adapter = AnswersAdapter(
            options = currentQuestion.options,
            isMultipleChoice = currentQuestion.is_multiple_choice,
            onSelected = { index ->
                if (currentQuestion.is_multiple_choice) {
                    responseIndexes.add(index)
                } else {
                    responseIndexes = mutableListOf(index)
                }
            }
        )

        val imageURL = "${getString(R.string.server_ip)}/api/static/public/questions/" + currentQuestion.img_name

        Glide.with(applicationContext)
            .load(imageURL)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(GlideImageListener(
                onSuccess = {
                    hideLoader()
                    changeErrorVisibility(false)
                },
                onError = {
                    hideLoader()
                    changeErrorVisibility(false)
                }
            ))
            .into(mainQuizImage)
    }
}