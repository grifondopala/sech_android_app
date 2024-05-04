package com.example.myapplication.interfaces

data class AnswerDto(val id: Int, val text: String){

}

data class QuestionDto(val id: Int, val text: String, val answers: Array<AnswerDto>, val type: String) {

}