package com.example.echo_proto.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    suspend fun isUrlAvailable(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection)
                    .apply {
                        requestMethod = "HEAD"
                        connectTimeout = 3000
                        readTimeout = 3000
                    }
                connection.responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                Timber.e("NetworkUtils:: URL check failed with $e \ne.message=${e.message}")
                false
            }
        }
    }
}