//package com.example.echo_proto.download_class_mass
//
//
//import android.app.Notification
//import android.app.Service
//import android.content.Context
//import android.content.Intent
//import android.os.IBinder
//import com.example.echo_proto.util.Constants.NOTIFICATION_ID
//import dagger.hilt.android.AndroidEntryPoint
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class DownloadService : Service() {
//
//    @Inject lateinit var downloadManager: DownloadManager
//
//    companion object {
//        fun start(context: Context) {
//            val intent = Intent(context, DownloadService::class.java)
//            context.startForegroundService(intent)
//        }
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        startForeground(NOTIFICATION_ID, createServiceNotification())
//        return START_STICKY
//    }
//
//
//    private fun createServiceNotification(): Notification {
//        // Notification о работе сервиса
//    }
//
//
//    override fun onBind(p0: Intent?): IBinder? = null
//
//}
//
//
