//package com.example.echo_proto.download_class_mass
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.example.echo_proto.R
//import javax.inject.Inject
//
//class DownloadNotificationHelper @Inject constructor(
//    private val context: Context
//) {
//    private val channelId = "downloads_channel"
//
//    init {
//        createNotificationChannel()
//    }
//
//    fun createProgressNotification(
//        id: Int,
//        episodeTitle: String,
//        progress: Int
//    ): Notification {
//        return NotificationCompat.Builder(context, channelId)
//            .setContentTitle("Загрузка: $episodeTitle")
//            .setSmallIcon(R.drawable.button_ic_download) // todo: make relevant ic_download
//            .setProgress(100, progress, false)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .build()
//    }
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Загрузки подкастов",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Прогресс загрузки эпизодов"
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//}