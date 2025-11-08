package com.example.echo_proto.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val downloadRepo: DownloadRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.d("🕒 4. DOWN::DownloadWorker: doWork() STARTED")
        return withContext(Dispatchers.IO) {
            Timber.d("🕒 5. DOWN::DownloadWorker: Dispatchers.IO context")
            val episodeId = params.inputData.getInt(KEY_CONTENT_URI, -1)
            Timber.d("🕒 6. DOWN::DownloadWorker: Got episodeId: $episodeId")
            if (episodeId == -1) return@withContext Result.failure()

            try {
                Timber.d("🕒 7. DOWN::DownloadWorker: Starting download flow...")
                downloadRepo.downloadEpisodeToDatabase(episodeId)
                    .collect { progress ->
                        setProgress(
                            Data.Builder()
                                .putInt(PROGRESS, progress)
                                .putInt(EPISODE_ID, episodeId)
                                .build()
                        )
                    }
                Timber.d("🕒 9. DOWN::DownloadWorker: Download COMPLETED")
                Result.success()
            } catch (e: Exception) {
                Timber.e(e, "DownloadWorker failed")
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val PROGRESS = "progress"
        const val EPISODE_ID = "episode_id"
    }
}

