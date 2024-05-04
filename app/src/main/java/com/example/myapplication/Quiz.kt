package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.TextView
import com.example.myapplication.interfaces.AnswerDto
import com.example.myapplication.interfaces.QuestionDto

class Quiz : Activity() {

    private lateinit var questionNumber: TextView;
    private lateinit var questionText: TextView;
    private lateinit var answersContainer: LinearLayout;
    private lateinit var nextQuestionButton: Button;
    private lateinit var questionLabel: TextView;

    private var questionIndex: Int = 0;

    private lateinit var questions: Array<QuestionDto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionNumber = findViewById(R.id.question_number)
        questionText = findViewById(R.id.question_text)
        questionLabel = findViewById(R.id.question_label)
        answersContainer = findViewById(R.id.answers_container)
        nextQuestionButton = findViewById(R.id.next_question_button)

        nextQuestionButton.setOnClickListener(View.OnClickListener {
            questionIndex++;
            if(questionIndex == questions.size - 1){
                nextQuestionButton.text = "Закончить опрос"
            }
            displayQuestion();
        })

        questions = getQuestions()

        displayQuestion()
    }

    private fun getQuestions(): Array<QuestionDto>{
        return arrayOf(
            QuestionDto(1,
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam tempor accumsan magna, tristique?",
                    arrayOf(AnswerDto(1, "Да"), AnswerDto(2, "Нет")), "single"),
            QuestionDto(2,
                "Тестовый вопрос: как дела?",
                arrayOf(AnswerDto(3, "Хорошо"), AnswerDto(4, "Отлично"), AnswerDto(5, "Средне"), AnswerDto(6, "Плохо")), "multiple"),
            QuestionDto(3,
                "Какой сегодня день недели?",
                arrayOf(AnswerDto(7, "Понедельник"), AnswerDto(8, "Вторник"), AnswerDto(9, "Среда"), AnswerDto(10, "Четверг"), AnswerDto(11, "Пятница"), AnswerDto(12, "Суббота"), AnswerDto(13, "Воскресенье")), "single"),)
    }

    fun displayQuestion(){
        questionNumber.text = "Вопрос ${questionIndex + 1}/${questions.size}";
        questionText.text = questions[questionIndex].text;


        answersContainer.removeAllViews()

        if(questions[questionIndex].type == "multiple"){
            questionLabel.text = "Выберите один или несколько вариантов ответа."
            for (answer in questions[questionIndex].answers){
                val newBox = CheckBox(applicationContext);
                newBox.text = answer.text
                newBox.textSize = 24F;
                answersContainer.addView(newBox)
            }
        }else{
            val radioGroup: RadioGroup = RadioGroup(applicationContext);
            questionLabel.text = "Выберите один вариант ответа."
            for (answer in questions[questionIndex].answers){
                val newRadio = RadioButton(applicationContext)
                newRadio.text = answer.text
                newRadio.textSize = 24F;
                radioGroup.addView(newRadio)
            }
            answersContainer.addView(radioGroup)
        }
    }
}