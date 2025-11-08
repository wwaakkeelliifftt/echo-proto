//package com.example.echo_proto.download_class_mass
//
//import android.app.DownloadManager
//import androidx.lifecycle.ViewModel
//import com.example.echo_proto.domain.model.Episode
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import javax.inject.Inject
//
//@HiltViewModel
//class DownloadViewModel @Inject constructor(
//    private val downloadManager: DownloadManager,
//    private val notificationHelper: DownloadNotificationHelper
//) : ViewModel() {
//
//    fun downloadEpisode(episode: Episode) {
//        val task = DownloadTask(
//            episodeId = episode.id,
//            url = episode.audioLink,
//            onProgress = { progress ->
//                updateNotification(episode, progress)
//            },
//            onSuccess = { file ->
//                markEpisodeAsDownloaded(episode, file)
//                showDownloadComplete(episode)
//            },
//            onError = { e ->
//                showError(e)
//            }
//        )
//
//        downloadManager.enqueueDownload(task)
//    }
//
//    private fun updateNotification(episode: Episode, progress: Int) {
//        val notification = notificationHelper.createProgressNotification(
//            episode.id.hashCode(),
//            episode.title,
//            progress
//        )
//        notificationManager.notify(episode.id.hashCode(), notification)
//    }
//}
//
//
