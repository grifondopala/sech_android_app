package com.example.myapplication.helpers

import java.io.IOException

class HttpException(val code: Int, message: String) : IOException("HTTP Error: $code - $message")