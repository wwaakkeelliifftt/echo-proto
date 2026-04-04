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
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}

class MediaSource @Inject constructor(
    private val db: FeedDatabase
) {

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    var episodes = emptyList<Episode>()
        set(value) {
            field = value
            Timber.d("MediaSource episodes updated: ${value.size} episodes")
        }

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

    suspend fun refreshMediaData() = withContext(Dispatchers.IO) {
        val result = db.dao.getQueueFeed()
            .map { it.toEpisode() }
            .sortedBy { it.indexInQueue }

        episodes = when {
            result.isNotEmpty() -> result
            else -> db.dao.getAllFeed().map { it.toEpisode() }
        }
        Timber.tag("PLAY").d("📊 MediaSource refreshed: ${episodes.size} episodes")
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSource.Factory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        episodes.forEach { episode ->
            val mediaUri = if (episode.isDownloaded && episode.downloadUrl.isNotEmpty()) {
                val file = java.io.File(episode.downloadUrl)
                if (file.exists()) {
                    android.net.Uri.fromFile(file)
                } else {
                    episode.audioLink.toUri()
                }
            } else {
                episode.audioLink.toUri()
            }
            
            val mediaItem = MediaItem.fromUri(mediaUri)
                .buildUpon()
                .setMimeType(MimeTypes.AUDIO_MPEG)
                .setMediaId(episode.id.toString())
                .build()

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }


    fun asMediaItems(): MutableList<MediaBrowserCompat.MediaItem> = episodes.map { episode ->
        val description = episode.asMediaDescriptionCompat()
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

    suspend fun updateEpisodePosition(episodeId: Int, position: Long) {
        withContext(Dispatchers.IO) {
            db.dao.updateEpisodePosition(episodeId, position)
            episodes = episodes.map { episode ->
                if (episode.id == episodeId) episode.copy(stopListeningAt = position) else episode
            }
        }
    }

    suspend fun markEpisodeAsListened(episodeId: Int) {
        withContext(Dispatchers.IO) {
            db.dao.markEpisodeAsListened(episodeId)
            episodes = episodes.map { episode ->
                if (episode.id == episodeId) {
                    episode.copy(hasListened = true, stopListeningAt = 0, isInQueue = false, indexInQueue = -1)
                } else {
                    episode
                }
            }
        }
    }

}

fun Episode.asMediaDescriptionCompat(): MediaDescriptionCompat {
    val finalIconUri = if (this.episodeImageUrl.isNotEmpty()) {
        this.episodeImageUrl.toUri()
    } else if (this.channelImageUrl.isNotEmpty()) {
        this.channelImageUrl.toUri()
    } else {
        null
    }

    val mmc = MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_MEDIA_ID, this.id.toString())
        .putString(METADATA_KEY_TITLE, this.title)
        .putString(METADATA_KEY_DISPLAY_TITLE, this.title)
        .putString(METADATA_KEY_DISPLAY_SUBTITLE, this.channelId)
        .putString(METADATA_KEY_DISPLAY_DESCRIPTION, this.description)
        .putLong(METADATA_KEY_DURATION, this.duration.toLong())
        .putString(METADATA_KEY_MEDIA_URI, this.audioLink)
        .putString(METADATA_KEY_DISPLAY_ICON_URI, finalIconUri?.toString())
        .putString(METADATA_KEY_ALBUM_ART_URI, finalIconUri?.toString())
        .build()

    return mmc.description
}
