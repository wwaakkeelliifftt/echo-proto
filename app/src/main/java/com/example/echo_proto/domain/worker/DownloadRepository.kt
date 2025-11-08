package com.example.echo_proto.domain.worker


import android.content.Context
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.domain.model.Episode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Named

interface DownloadRepository {
    suspend fun downloadEpisodeToDatabase(episodeId: Int): Flow<Int>
    suspend fun deleteEpisodeFromDeviceAndDatabase(episodeId: Int)
}

class DownloadRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val db: FeedDatabase,
    private val context: Context
) : DownloadRepository {

    override suspend fun downloadEpisodeToDatabase(episodeId: Int): Flow<Int> = flow {
        Timber.d("🕒 10. DOWN::DownloadRepositoryImpl: START - Getting episode from DB")
        val episode = db.dao.getEpisodeById(episodeId).toEpisode()
        Timber.d("🕒 11. DOWN::DownloadRepositoryImpl: Got episode: ${episode.title}")
        val file = makeEpisodeFilepath(episode)

        Timber.d("🕒 12. DOWN::DownloadRepositoryImpl: Creating HTTP request...")
        val request = Request.Builder().url(episode.audioLink).build()
        Timber.d("🕒 13. DOWN::DownloadRepositoryImpl: Executing request...")
        Timber.d("🕒 13.1 DOWN::DownloadRepositoryImpl: Request created at ${System.currentTimeMillis()}")
        val response = okHttpClient.newCall(request).execute()
        Timber.d("🕒 13.2 DOWN::DownloadRepositoryImpl: Response received at ${System.currentTimeMillis()}")
        Timber.d("🕒 14. DOWN::DownloadRepositoryImpl: Got response, success: ${response.isSuccessful}")

        if (response.isSuccessful) {
            "response.isSuccessful".toLogcat()
            response.body?.let { body ->
                val totalSize = body.contentLength()
                var downloadedSize = 0L
                Timber.d("🕒 15. DOWN::DownloadRepositoryImpl: Total size: $totalSize bytes")

                body.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int

                        Timber.d("🕒 16. DOWN::DownloadRepositoryImpl: Starting file copy...")
                        var progress = 0
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedSize += bytesRead
                            val currentProgress = ((downloadedSize * 100) / totalSize).toInt()
                            if (currentProgress > progress) {
                                progress = currentProgress
                                "$progress".toLogcat()
                            }
                            emit(progress)
                        }
                    }
                }
            }

            Timber.d("🕒 17. DOWN::DownloadRepositoryImpl: Updating database...")
            db.dao.insertEpisode(episode.copy(
                isDownloaded = true,
                downloadUrl = file.absolutePath
            ).toEpisodeEntity())
            emit(100)
            Timber.d("🕒 18. DOWN::DownloadRepositoryImpl: DONE")
        }
    }

    private fun makeEpisodeFilepath(episode: Episode): File {
        val startTime = System.currentTimeMillis()
        val file = File(context.filesDir, "episodes/${episode.rssId}.mp3")
        "File path: ${file.absolutePath}".toLogcat()
        file.parentFile?.mkdirs()
        Timber.d("File creation took ${System.currentTimeMillis() - startTime} ms")
        return file
    }

    private suspend fun addPathToEpisodeMp3File(path: String, episode: Episode) {
        val episode = episode.copy(audioLink = path)
        val episodeEntity = episode.toEpisodeEntity()
        db.dao.insertEpisode(episodeEntity)
        Timber.d("DownloadRepositoryImpl:addPathToEpisodeMp3File:: path=$$path")
    }

    override suspend fun deleteEpisodeFromDeviceAndDatabase(episodeId: Int) {
        val episodeEntity = db.dao.getEpisodeById(episodeId)
        val episode = episodeEntity.toEpisode()

        if (!episode.isDownloaded || episode.downloadUrl.isEmpty()) {
            Timber.e("DownloadRepository::deleteEpisodeFromDeviceAndDatabase: Episode is not downloaded or file path is empty")
            return
        }

        val file = File(episode.downloadUrl!!)
        if (file.exists()) {
            try {
                val deleted = file.delete()
                Timber.d("DownloadRepository::deleteEpisodeFromDeviceAndDatabase: File deleted: $deleted, path: ${file.absolutePath}")
                if (!deleted) {
                    Timber.e("DownloadRepository::deleteEpisodeFromDeviceAndDatabase: Failed to delete file: ${file.absolutePath}")
                    return
                }
            } catch (e: Exception) {
                Timber.e(e, "DownloadRepository::deleteEpisodeFromDeviceAndDatabase: Error deleting file: ${file.absolutePath}")
                return
            }
        } else {
            Timber.e("DownloadRepository::deleteEpisodeFromDeviceAndDatabase: File does not exist: ${file.absolutePath}")
            return
        }

        db.dao.insertEpisode(
            episode.copy(
                isDownloaded = false,
                downloadUrl = ""
            ).toEpisodeEntity()
        )
    }


    private fun String.toLogcat() {
        Timber.d("DownloadRepositoryImpl: $this")
    }
}