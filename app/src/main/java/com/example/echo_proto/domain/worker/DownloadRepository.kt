package com.example.echo_proto.domain.worker

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.net.toUri
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.domain.model.Episode
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

interface DownloadRepository {
    suspend fun downloadEpisodeToDatabase(episodeId: Int): Boolean
}

class DownloadRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val db: FeedDatabase,
    private val app: Context
): DownloadRepository {

    companion object {
        val filePath by lazy { Environment.DIRECTORY_DOWNLOADS + "/echo-podcasts/" }
    }

    private suspend fun getEpisodeById(id: Int): Episode = db.dao.getEpisodeById(id).toEpisode()

    private fun makeOkHttpRequest(url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

//    private fun OkHttpClient.makeEnqueueCall(request: Request, ioLambda: (Response) -> Unit) {
//        this.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  ERROR -> !response.isSuccessful")
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (!response.isSuccessful) {
//                    Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  ERROR -> !response.isSuccessful")
//                    return
//                }
//                ioLambda(response)
//            }
//        })
//    }

    override suspend fun downloadEpisodeToDatabase(episodeId: Int): Boolean {
        Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::START")

        val episode = getEpisodeById(episodeId)
        val episodeFileName = episode.videoLink
        val folder = File(filePath).also {
            if (!it.exists()) it.mkdir()
        }
        val outputFile = File(folder.absolutePath, "$episodeFileName.mp3") //File(app.filesDir, "$episodeFileName.mp3")
        val url = episode.audioLink
        Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::\n\t\t" +
                "episode.title=$episodeFileName\n\t" +
                "outputFilePath=${outputFile.path}\n\t" +
                "url=$url")
        Timber.d("EPISODE=$episode")
        val request = makeOkHttpRequest(url)
        Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::\n\t\t REQUEST is DONE, let's TRY:")

        try {
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  ERROR -> !response.isSuccessful")
                return false
            }
            val inputStream = response.body?.byteStream()
            val length = response.body?.contentLength() ?: -1
            if (!outputFile.exists()) {
                Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase:: !outputFile.exists()")
                outputFile.createNewFile()
            }
            val outputStream = FileOutputStream(outputFile)
            val data = byteArrayOf()
//            emit("0%")
            var progress = 0
            while (true) {
                val count = inputStream?.read(data) ?: -1
                if (count == -1) break
                progress += count
                val percents = (progress * 100 / length)
//                emit("$percents%")
                Timber.d("progress=$percents")
                outputFile.writeBytes(data)
            }
            Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  before (CLOSE) streams")
            outputStream.flush()
            outputStream.close()
            inputStream?.close()
            Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  copy from input to output done")




//            Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  copy from input to output done")
//            updateDatabase(episode, pathToFile = outputFile.path)
//            return true

//            okHttpClient.makeEnqueueCall(request) { res ->
//                Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::makeEnqueueCall:: go")
//                val inputStream = res.body()?.byteStream()
//                val outputStream = FileOutputStream(outputFile)
//                inputStream.use { input ->
//                    outputStream.use { output ->
//                        input?.copyTo(output)
//                    }
//                }
//                Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  before (CLOSE) streams")
//                inputStream?.close()
//                outputStream.close()
//
//                Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::  copy from input to output done")
//            }

//            saveWithDownloadManager(episode)
            val path = Environment.DIRECTORY_DOWNLOADS + "${episode.rssId}.mp3"
            updateDatabase(episode, pathToFile = path)
            return true

        } catch (e: Exception) {
            Timber.d("DownloadRepositoryImpl::downloadEpisodeToDatabase::\n ${e.printStackTrace()}")
            return false
        }
    }

    private suspend fun updateDatabase(episode: Episode, pathToFile: String) {
        val newEpisode = episode.copy(
            isDownloaded = true,
            // todo -> activate link?
//            audioLink = pathToFile
        ).toEpisodeEntity()
        Timber.d("DownloadRepositoryImpl::updateDatabase:: ->db.dad.invoke() ")
        db.dao.insertEpisode(newEpisode)
        Timber.d("DownloadRepositoryImpl::updateDatabase::  \n" +
                "episodeEntity=${newEpisode.toString()}")
    }

    private fun saveWithDownloadManager(episode: Episode) {
        Timber.d("DownloadRepositoryImpl::saveWithDownloadManager:: GO")
        val url = episode.audioLink
        val pathToFile = "/echo-podcasts/${episode.rssId}.mp3"
        val downloadManager = app.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        Timber.d("DownloadRepositoryImpl::saveWithDownloadManager:: url=$url, uri=${Uri.parse(url)}")
        val request = DownloadManager.Request(url.toUri()).apply {
            setTitle("Download podcast mp3")
            setDescription("desc: " + episode.title.take(50))
            setMimeType("audio/mpeg")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pathToFile)
        }
        val downloadId = downloadManager.enqueue(request)

        fun getReceiver(): BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, intent: Intent?) {
                if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        Toast.makeText(app, "Загрузка завершена :: $pathToFile", Toast.LENGTH_LONG).show()
                        Timber.d("Загрузка завершена :: pathToFile=$pathToFile")
                    }
                }

            }
        }
        app.registerReceiver(getReceiver(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

}

//class DownloadCompleteReceiver: BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
//            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//            if (id == downloadId) {
//                Toast.makeText(app, "Загрузка завершена :: $pathToFile", Toast.LENGTH_LONG).show()
//                Timber.d("Загрузка завершена :: pathToFile=$pathToFile")
//            }
//        }
//    }
//}