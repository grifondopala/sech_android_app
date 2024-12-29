package com.example.myapplication.interfaces

data class QuestionOptionDto(
    val response_id: Int,
    val response_text: String,
    val points: Int,
    val next_question_id: Int,
)

data class QuestionDto(val img_name: String, val question_text: String, val pass_num: Int, val options: Array<QuestionOptionDto>) {

}

