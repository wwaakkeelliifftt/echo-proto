package com.example.echo_proto

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.echo_proto.domain.worker.DownloadRepository
import com.example.echo_proto.domain.worker.DownloadWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class EchoApp: Application(), Configuration.Provider {

//    @Inject lateinit var workerFactory: WorkerFactory
    @Inject lateinit var workerFactory: DownloadWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}

class DownloadWorkerFactory @Inject constructor(
    private val repository: DownloadRepository
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return DownloadWorker(appContext, workerParameters, repository)
    }
}