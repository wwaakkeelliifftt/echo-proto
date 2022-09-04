package com.example.echo_proto.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.State.*
import com.example.echo_proto.ui.viewmodels.ViewModelState
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}

class MediaSource @Inject constructor(
    // maybe need inject in serviceModule?
    private val db: FeedDatabase
) {

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    var episodes = emptyList<Episode>()
        private set // ?? ne fact

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener.invoke(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        val result = db.dao.getQueueFeed()
            .map { it.toEpisode() }
            .sortedBy { it.indexInQueue }

        episodes = when {
            result.isNotEmpty() -> result
            else -> db.dao.getAllFeed().map { it.toEpisode() }
        }
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        episodes.forEach { episode ->
            val mediaItem = MediaItem.fromUri(
                if (episode.isDownloaded) episode.downloadUrl else episode.audioLink
            )
            Timber.d("\n\t\tAS_MEDIA_SOURCE::title=${episode.title},\n\t\t" +
                    "isDownloaded=${episode.isDownloaded},\n\t\t" +
                    "mp3=${episode.downloadUrl},\n\t\t" +
                    "web=${episode.audioLink}\n\t\t" +
                    "mediaItem=${mediaItem}\n\t\t" +
                    "mediaItem/title=${mediaItem.mediaMetadata.title}")
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    MediaItem.fromUri(episode.audioLink)
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }


    fun asMediaItems(): MutableList<MediaBrowserCompat.MediaItem> = episodes.map { episode ->
        val description = MediaDescriptionCompat.Builder()
            .setMediaUri(episode.audioLink.toUri())
            .setTitle(episode.title)
            .setMediaId(episode.mediaId)
            .setDescription(episode.description)
            .build()
        MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE)
    }.toMutableList()



    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action.invoke(state == STATE_INITIALIZED)
            true
        }
    }

    // need customize to update playlist inside player??
    suspend fun updateEpisodeSourcePlaylist(state: ViewModelState) {
        when (state) {
            ViewModelState.Queue -> {
                val result = db.dao.getQueueFeed().map { it.toEpisode() }
                if (result.isNotEmpty()) {
                    episodes = result
                }
            }
            else -> {}
        }
    }

}

// TODO: MediaSource changeover ---------------------------------------------------------------
fun Episode.asMediaDescriptionCompat(): MediaDescriptionCompat {
    val mmc = MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_MEDIA_ID, this.id.toString())
        .putString(METADATA_KEY_TITLE, this.title)
        .putString(METADATA_KEY_DISPLAY_TITLE, this.title)
        .putString(METADATA_KEY_DISPLAY_DESCRIPTION, this.description)

        .putLong(METADATA_KEY_YEAR, this.timestamp).also { Timber.d("-----::TIMESTAMP::---->>>${this.timestamp}") }
        .putString(METADATA_KEY_DATE, this.timestamp.toString()).also {
            Timber.d("TIMESTAMP::episode[${this.id}]=${this.timestamp} / asString=${this.timestamp}")
        }
        .putLong(METADATA_KEY_DURATION, this.duration.toLong()).also {
            Timber.d("DURATION::episode[${this.id}]=${this.duration} / asLong=${this.duration.toLong()}")
        }
//        .putLong(METADATA_KEY_DOWNLOAD_STATUS, MediaDescriptionCompat.STATUS_DOWNLOADED)
//        .putLong(METADATA_KEY_TRACK_NUMBER, this.indexInQueue.toLong()) // check with assert to -1 ??

//        .putString(METADATA_KEY_ART_URI, this.videoLink)
        .putString(METADATA_KEY_MEDIA_URI, this.audioLink) // todo: <-- check WAT we save? file on disk or url
//                .putString(METADATA_KEY_MEDIA_URI, episode.downloadUrl) // todo: <-- check WAT we save? file on disk or url
        .build()

    return mmc.description
}
