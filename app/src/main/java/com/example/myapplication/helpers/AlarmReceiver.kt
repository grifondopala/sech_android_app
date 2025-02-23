package com.example.myapplication.helpers

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    companion object{
        @SuppressLint("ScheduleExactAlarm")
        fun scheduleNotification(context: Context, timeInMillis: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

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
        if (context != null) {
            Notification.sendNotification(context)
        }
    }
}