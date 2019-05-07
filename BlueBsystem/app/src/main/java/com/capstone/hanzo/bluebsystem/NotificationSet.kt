package com.capstone.hanzo.bluebsystem

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class NotificationSet(context: Context) {
    val manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    val pendingIntent:PendingIntent = PendingIntent.getActivities(context,0,Intent(context,MenuActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT)

}

