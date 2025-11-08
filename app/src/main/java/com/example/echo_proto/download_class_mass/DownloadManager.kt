//package com.example.echo_proto.download_class_mass
//
//import android.content.Context
//import androidx.core.app.NotificationManagerCompat
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import javax.inject.Inject
//import kotlin.random.Random
//
//@AndroidEntryPoint
//class DownloadManager @Inject constructor(
//    private val context: Context,
//    private val notificationManager: NotificationManagerCompat
//) : CoroutineScope by CoroutineScope(Dispatchers.IO + SupervisorJob()) {
//
//    private val downloadQueue = Channel<DownloadTask>(capacity = Channel.UNLIMITED)
//    private val activeDownloads = mutableMapOf<String, DownloadProgress>()
//    private val maxParallelDownloads = 3 // По умолчанию
//
//    init {
//        repeat(maxParallelDownloads) {
//            launchDownloadWorker()
//        }
//    }
//
//    private fun launchDownloadWorker() = launch {
//        for (task in downloadQueue) {
//            try {
//                downloadFile(task)
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    task.onError?.invoke(e)
//                }
//            }
//        }
//    }
//
//    private suspend fun downloadFile(task: DownloadTask) {
//        val notificationId = Random.nextInt()
//        activeDownloads[task.episodeId] = DownloadProgress(0, notificationId)
//
//        val result = withContext(Dispatchers.IO) {
//            // Реализация загрузки файла
//        }
//
//        activeDownloads.remove(task.episodeId)
//        withContext(Dispatchers.Main) {
//            task.onSuccess?.invoke(result)
//        }
//    }
//
//    fun setMaxParallelDownloads(count: Int) {
//        // Обновляем количество воркеров
//    }
//
//    fun enqueueDownload(task: DownloadTask) {
//        launch {
//            downloadQueue.send(task)
//        }
//    }
//}