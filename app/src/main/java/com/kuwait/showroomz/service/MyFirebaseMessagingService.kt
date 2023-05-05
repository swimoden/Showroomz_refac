package com.kuwait.showroomz.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import com.kuwait.showroomz.R
import com.kuwait.showroomz.extras.Utils
import com.kuwait.showroomz.model.repository.LogProgressRepository
import com.kuwait.showroomz.view.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val CHANNEL_ID = "SHOWROOMZ"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(TAG, "onMessageReceived: ")
       sendNotification(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.e(TAG, "onMessageReceived: ${remoteMessage.messageId}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }


    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        LogProgressRepository.registerDeviceId(p0)
        Log.e(TAG, "onNewToken: $p0")
    }
    private fun sendNotification(messageBody: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        Log.e(TAG, "sendNotification: ${messageBody.data} " )
        messageBody.notification.toString()
        val bundle=Bundle()
        Utils.instance.mapToBundle(messageBody.data)?.let { intent.putExtras(it) }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(messageBody.notification?.title)
            .setContentText(messageBody.notification?.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}