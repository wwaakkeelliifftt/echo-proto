package com.example.echo_proto.domain.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.echo_proto.MainActivity
import com.example.echo_proto.R
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted private val params: WorkerParameters,
    private val downloadRepo: DownloadRepository,
    private val db: FeedDatabase
) : CoroutineWorker(context, params) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    override suspend fun doWork(): Result {
        Timber.d("🕒 4. DOWN::DownloadWorker: doWork() STARTED")
        return withContext(Dispatchers.IO) {
            Timber.d("🕒 5. DOWN::DownloadWorker: Dispatchers.IO context")
            val episodeId = params.inputData.getInt(KEY_CONTENT_URI, -1)
            Timber.d("🕒 6. DOWN::DownloadWorker: Got episodeId: $episodeId")
            if (episodeId == -1) return@withContext Result.failure()

            try {
                // Получаем название эпизода для уведомления
                val episode = db.dao.getEpisodeById(episodeId).toEpisode()
                val episodeTitle = episode.title

                Timber.d("🕒 7. DOWN::DownloadWorker: Starting download flow...")
                
                // Показываем начальное уведомление
                setForeground(createForegroundInfo(episodeTitle, 0))
                
                downloadRepo.downloadEpisodeToDatabase(episodeId)
                    .collect { progress ->
                        // Обновляем прогресс в уведомлении
                        setForeground(createForegroundInfo(episodeTitle, progress))
                        setProgress(
                            Data.Builder()
                                .putInt(PROGRESS, progress)
                                .putInt(EPISODE_ID, episodeId)
                                .build()
                        )
                    }
                    
                // Показываем финальное уведомление о завершении
                showCompletionNotification(episodeTitle)
                
                Timber.d("🕒 9. DOWN::DownloadWorker: Download COMPLETED")
                Result.success()
            } catch (e: Exception) {
                Timber.e(e, "DownloadWorker failed")
                // Показываем уведомление об ошибке
                showErrorNotification(episodeId)
                Result.failure()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                "Загрузки эпизодов",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Прогресс загрузки эпизодов подкастов"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(episodeTitle: String, progress: Int): ForegroundInfo {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Загрузка эпизода")
            .setContentText(episodeTitle)
            .setSmallIcon(R.drawable.button_ic_download)
            .setProgress(100, progress, false)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return ForegroundInfo(Constants.DOWNLOAD_NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(episodeTitle: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Загрузка завершена")
            .setContentText(episodeTitle)
            .setSmallIcon(R.drawable.button_ic_download)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(Constants.DOWNLOAD_NOTIFICATION_ID, notification)
    }

    private suspend fun showErrorNotification(episodeId: Int) {
        val episodeTitle = try {
            db.dao.getEpisodeById(episodeId).toEpisode().title
        } catch (e: Exception) {
            "Эпизод"
        }

        val notification = NotificationCompat.Builder(applicationContext, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Ошибка загрузки")
            .setContentText("Не удалось загрузить: $episodeTitle")
            .setSmallIcon(R.drawable.button_ic_download)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(Constants.DOWNLOAD_NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val PROGRESS = "progress"
        const val EPISODE_ID = "episode_id"
    }
}

