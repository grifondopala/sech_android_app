package com.example.myapplication

import QuizApi
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.TEXT_ALIGNMENT_GRAVITY
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.helpers.AlarmReceiver
import com.example.myapplication.helpers.BaseAuth
import com.example.myapplication.interfaces.QuestionDto
import com.example.myapplication.interfaces.SaveResponseDto
import com.google.gson.Gson
import com.squareup.picasso.Picasso
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

    private lateinit var credentials: String;

    private var responseIndexes = mutableListOf<Int>()
    private var passNum: Int = 1;

    private lateinit var quizApi: QuizApi;

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

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
                val responseBody = quizApi.saveUserResponse(responseIds, passNum)

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        val response: SaveResponseDto = gson.fromJson(responseBody, SaveResponseDto::class.java)

                        if (response.is_ended) {
                            generateNotification()
                            showEnd()
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
        val next_question_id = currentQuestion.options[responseIndexes[0]].next_question_id;

        showLoader()

        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = quizApi.sendNextQuestionResponse(next_question_id)

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        currentQuestion = gson.fromJson(responseBody, QuestionDto::class.java)

                        responseIndexes = mutableListOf()

                        displayQuestion()
                        hideLoader()
                        changeErrorVisibility(false)
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
          val quizTimeAllow: Long = System.currentTimeMillis() + 10000;
          prefs.edit().putLong("quiz_time_allow", quizTimeAllow).commit()
          AlarmReceiver.scheduleNotification(this, System.currentTimeMillis() + 10000)
    }

    private fun startQuiz() {
        showLoader()

        lifecycleScope.launch(ioDispatcher) {
            try {
                val responseBody = quizApi.sendStartQuizResponse()

                withContext(mainDispatcher) {
                    val gson = Gson()
                    try {
                        currentQuestion = gson.fromJson(responseBody, QuestionDto::class.java)
                        passNum = currentQuestion.pass_num
                        displayQuestion()
                        hideLoader()
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
        val timestamp = System.currentTimeMillis()
        val imageURL = "${getString(R.string.server_ip)}/public/questions/" + currentQuestion.img_name + "?timestamp=$timestamp"
        Picasso.get()
            .load(imageURL)
            .into(mainQuizImage)

        questionText.text = currentQuestion.question_text;

        val typeface = ResourcesCompat.getFont(this, R.font.commissioner_medium)

        answersContainer.removeAllViews()

        if (currentQuestion.is_multiple_choice) {
            questionLabel.text = "Можно выбрать несколько вариантов ответа."

            val answersLinearLayout = LinearLayout(applicationContext)
            answersLinearLayout.orientation = LinearLayout.VERTICAL

            for ((index, option) in currentQuestion.options.withIndex()) {
                val newCheckbox = CheckBox(applicationContext)

                newCheckbox.text = option.response_text
                newCheckbox.textSize = 24F
                newCheckbox.typeface = typeface

                newCheckbox.gravity = Gravity.CENTER_VERTICAL
                newCheckbox.textAlignment = TEXT_ALIGNMENT_GRAVITY
                newCheckbox.setPadding(16, 0, 0, 24)

                newCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        responseIndexes.add(index)
                    } else {
                        responseIndexes.remove(index)
                    }
                }

                answersLinearLayout.addView(newCheckbox)
            }

            answersContainer.addView(answersLinearLayout)
        } else {
            questionLabel.text = "Выберите один вариант ответа."

            val answersRadioGroup = RadioGroup(applicationContext)

            for ((index, option) in currentQuestion.options.withIndex()) {
                val newRadio = RadioButton(applicationContext)

                newRadio.text = option.response_text
                newRadio.textSize = 24F
                newRadio.typeface = typeface

                newRadio.gravity = Gravity.CENTER_VERTICAL
                newRadio.setPadding(16, 0, 0, 24)

                newRadio.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        responseIndexes = mutableListOf(index);
                    }
                }

                answersRadioGroup.addView(newRadio)
            }

            answersContainer.addView(answersRadioGroup)
        }
    }
}