package com.example.echo_proto.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val dataLoader: DownloadRepository
): CoroutineWorker(context, params) {

//    @Inject lateinit var dataLoader: DownloadRepository

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            Timber.d("DownloadWorker::doWork:: start")
            val episodeId = params.inputData.getInt(KEY_CONTENT_URI, -1)
            Timber.d("DownloadWorker::doWork:: episodeId=$episodeId")
            if (episodeId == -1) return@withContext Result.failure()
            dataLoader.downloadEpisodeToDatabase(episodeId)
            Timber.d("DownloadWorker::doWork:: end")
            Result.success()
        }

    }


    companion object {
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
    }
}
