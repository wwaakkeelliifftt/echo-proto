package com.example.echo_proto.exoplayer

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    context: Context,
    params: WorkerParameters,
    private val dataLoader: DataLoader
): CoroutineWorker(context, params) {

    @AssistedFactory
    interface Factory {
        fun 
    }

    override fun doWork(): Result {

        TODO("Not yet implemented")
    }




}
