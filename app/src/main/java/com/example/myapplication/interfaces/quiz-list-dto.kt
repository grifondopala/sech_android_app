package com.example.myapplication.interfaces

data class QuizDto(
    val quiz_id: Int,
    val name: String,
    val description: String,
    val is_available: Boolean,
    val next_time_can: String,
    val time_to_pass_again: Long,
)

data class QuizListDto(
    val list: Array<QuizDto>
)