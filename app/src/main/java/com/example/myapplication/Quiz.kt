package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.helpers.AlarmReceiver
import com.example.myapplication.helpers.BaseAuth
import com.example.myapplication.interfaces.QuestionDto
import com.example.myapplication.interfaces.QuestionOptionDto
import com.example.myapplication.interfaces.SaveResponseDto
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

interface QuizCallback {
    fun onUnsuccess()
    fun onSuccess(responseBody: String)
    fun onFailure()
}

class Quiz : Activity() {

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

    private lateinit var credentials: String;

    private var selectedAnswerIndex: Int = -1;

    private lateinit var mainPersonIntent: Intent;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        mainPersonIntent = Intent(this, MainPerson::class.java)

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

        credentials = BaseAuth.getCredentials(this);

        nextQuestionButton.setOnClickListener(View.OnClickListener {
            saveResponse()
        })

        endQuizButton.setOnClickListener(View.OnClickListener {
            endQuiz()
        })

        startQuiz()
    }

    private fun endQuiz(){
        startActivity(mainPersonIntent)
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

    private fun saveResponse(){
        if(selectedAnswerIndex === -1){
            changeErrorVisibility(true)
            return
        }

        var answer: QuestionOptionDto = currentQuestion.options[selectedAnswerIndex];

        saveUserResponse(answer.response_id, currentQuestion.pass_num,object : QuizCallback {
            override fun onUnsuccess(){
                runOnUiThread{

                }
            }

            override fun onSuccess(responseBody: String) {
                runOnUiThread {
                    val gson = Gson()
                    try {
                        val response: SaveResponseDto = gson.fromJson(responseBody, SaveResponseDto::class.java)

                        if (response.is_ended){
                            generateNotification()
                            showEnd()
                        }else{
                            nextQuestion()
                        }
                    } catch(e: Exception){
                        Log.e("App error", "Save response: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            }

            override fun onFailure() {
                runOnUiThread{

                }
            }
        })
    }

    private fun nextQuestion(){
        var answer: QuestionOptionDto = currentQuestion.options[selectedAnswerIndex];

        showLoader();

        sendNextQuestionResponse(answer.next_question_id, object : QuizCallback {
            override fun onUnsuccess(){
                runOnUiThread{

                }
            }

            override fun onSuccess(responseBody: String) {
                runOnUiThread {
                    val gson = Gson()
                    try {
                        currentQuestion = gson.fromJson(responseBody, QuestionDto::class.java)

                        selectedAnswerIndex = -1

                        displayQuestion()

                        hideLoader()
                        changeErrorVisibility(false)
                    } catch (e: Exception) {
                        Log.e("App error", "Next question: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            }

            override fun onFailure() {
                runOnUiThread{

                }
            }
        })
    }

    private fun generateNotification(){
          var quizTimeAllow: Long = System.currentTimeMillis() + 10000;
          prefs.edit().putLong("quiz_time_allow", quizTimeAllow).commit()
          AlarmReceiver.scheduleNotification(this, System.currentTimeMillis() + 10000)
    }

    private fun startQuiz(){
        showLoader()

        sendStartQuizResponse(object : QuizCallback {
            override fun onUnsuccess(){
                runOnUiThread{

                }
            }

            override fun onSuccess(responseBody: String) {
                runOnUiThread {
                    val gson = Gson()
                    try {
                        currentQuestion = gson.fromJson(responseBody, QuestionDto::class.java)

                        displayQuestion()

                        hideLoader()
                    } catch (e: Exception) {
                        Log.e("App error", "Start quiz: Ошибка парсинга JSON: ${e.message}")
                    }
                }
            }

            override fun onFailure() {
                runOnUiThread{

                }
            }
        })
    }

    fun displayQuestion() {

        var imageURL = "${getString(R.string.server_ip)}/public/" + currentQuestion.img_name;
        Picasso.get()
            .load(imageURL)
            .into(mainQuizImage)

        questionText.text = currentQuestion.question_text;

        val typeface = ResourcesCompat.getFont(this, R.font.commissioner_medium)

        answersContainer.removeAllViews()

        var answersRadioGroup = RadioGroup(applicationContext)

        questionLabel.text = "Выберите один вариант ответа."
        for ((index, option) in currentQuestion.options.withIndex()) {
            val newRadio = RadioButton(applicationContext)

            newRadio.text = option.response_text
            newRadio.textSize = 24F
            newRadio.typeface = typeface

            newRadio.gravity = Gravity.CENTER_VERTICAL
            newRadio.setPadding(16, 0, 0, 8)

            newRadio.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedAnswerIndex = index;
                }
            }

            answersRadioGroup.addView(newRadio)
        }
        answersContainer.addView(answersRadioGroup)
    }

    private fun saveUserResponse(responseId: Int, passNum: Int, callback: QuizCallback){
        val media = "application/json; charset=utf-8".toMediaType()
        val body = ("{\"response_id\": $responseId," +
                     "\"pass_num\": $passNum}").toRequestBody(media)

        val request = Request.Builder()
            .url("${getString(R.string.server_ip)}/api/user/response/save")
            .header("Authorization", credentials)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful){
                        callback.onUnsuccess()
                        return
                    }

                    callback.onSuccess(response.body.string())
                }
            }
        })
    }

    private fun sendNextQuestionResponse(nextQuestionId: Int, callback: QuizCallback) {
        val media = "application/json; charset=utf-8".toMediaType()
        val body = ("{\"question_id\": $nextQuestionId}").toRequestBody(media)

        val request = Request.Builder()
            .url("${getString(R.string.server_ip)}/api/questions/get")
            .header("Authorization", credentials)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful){
                        Log.d("App error", response.message)
                        callback.onUnsuccess()
                        return
                    }

                    callback.onSuccess(response.body.string())
                }
            }
        })
    }

    private fun sendStartQuizResponse(callback: QuizCallback) {

        val request = Request.Builder()
            .url("${getString(R.string.server_ip)}/api/questions/start")
            .header("Authorization", credentials)
            .post(RequestBody.create(null, "")) // Send an empty body
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful){
                        callback.onUnsuccess()
                        return
                    }

                    callback.onSuccess(response.body.string())
                }
            }
        })
    }
}