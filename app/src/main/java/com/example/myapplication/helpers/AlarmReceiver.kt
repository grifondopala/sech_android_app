package com.example.myapplication.helpers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    companion object{
        @SuppressLint("ScheduleExactAlarm")
        fun scheduleNotification(context: Context, timeToPassAgain: Long, quizId: Int, name: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("quizId", quizId)
                putExtra("name", name)
                putExtra("timeToPassAgain", timeToPassAgain)
            }

            val timeInMillis = Calendar.getInstance().timeInMillis + timeToPassAgain
            val pendingIntent = PendingIntent.getBroadcast(context, quizId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }

        fun canScheduleExactAlarms(context: Context): Boolean {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val quizId = intent.getIntExtra("quizId", 1);
            val name = intent.getStringExtra("name").toString();
            val timeToPassAgain = intent.getLongExtra("timeToPassAgain", 5000)
            Notification.sendNotification(context, quizId, name, timeToPassAgain)
        }
    }
}