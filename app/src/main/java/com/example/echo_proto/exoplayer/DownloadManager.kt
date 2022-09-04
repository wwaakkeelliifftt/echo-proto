package com.example.echo_proto.exoplayer

import android.app.Notification
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadHelper
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import java.io.File

//class DownloadManager(
//    private val constructorHelper: DownloadHelper,
//    private val actionFile: File
//): DownloadService(FOREGROUND_NOTIFICATION_ID_NONE) {
//
//    override fun getDownloadManager(): DownloadManager {
//        TODO("Not yet implemented")
//    }
//
//    override fun getScheduler(): Scheduler? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getForegroundNotification(
//        downloads: MutableList<Download>,
//        notMetRequirements: Int
//    ): Notification {
//        TODO("Not yet implemented")
//    }
//    init {
//
//    }
//
//}
