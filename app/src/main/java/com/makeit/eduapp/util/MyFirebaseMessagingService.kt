package com.makeit.eduapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.makeit.eduapp.MainActivity
import com.makeit.eduapp.R
import java.util.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"

    companion object{
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        // Check if the message contains data payload
//        remoteMessage.data.isNotEmpty().let {
//            // Handle data payload
//            handleDataPayload(remoteMessage.data)
//            val title = remoteMessage.notification?.title ?: "Title"
//            val body = remoteMessage.notification?.body ?: "Body"
//
//            showNotification(title, body)
//
//        }
//
//        // Check if the message contains a notification payload (optional)
//        remoteMessage.notification?.let {
//            // Handle notification payload
//            handleNotificationPayload(it)
//        }

        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them.
      */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_notifications_black_24dp
        )

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setLargeIcon(largeIcon)
            .setContentTitle(remoteMessage?.data?.get("title"))
            .setContentText(remoteMessage?.data?.get("message"))
            .setAutoCancel(true)
            .setSound(notificationSoundUri)
            .setContentIntent(pendingIntent)

        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.black)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to devie notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: Refreshed Token: $token")
    }






}