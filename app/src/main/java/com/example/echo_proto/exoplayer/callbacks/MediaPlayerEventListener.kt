package com.example.echo_proto.exoplayer.callbacks


import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import com.example.echo_proto.exoplayer.MediaService
import com.example.echo_proto.util.Constants
import com.google.android.exoplayer2.*

import timber.log.Timber

class MediaPlayerEventListener(
    private val mediaService: MediaService,
    private val mediaSession: MediaSessionCompat
): Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(mediaService, "Error was happened. ::eventListener", Toast.LENGTH_LONG).show()

        val bundle = Bundle().apply {
            putString("errorCode", error.errorCodeName)
            error.cause?.message?.let { putString("details", it) }
        }
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                mediaSession.sendSessionEvent(Constants.ERROR_NETWORK, bundle)
            }
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                mediaSession.sendSessionEvent(Constants.ERROR_HTTP, bundle)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        Timber.d("Player state changed: playWhenReady=$playWhenReady, playbackState=$playbackState")
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            mediaService.stopForeground(false)
            Timber.d("Player state changed: ::if (playbackState == Player.STATE_READY && !playWhenReady)::  ->  mediaService.stopForeground(false)\n")
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        // Update the UI according to the modified playlist (add, move or remove).
        if (reason == Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) {
//            updateUiForPlaylist(timeline)
            Toast.makeText(mediaService, "Playlist TIMELINE is Changed", Toast.LENGTH_LONG).show()
        }
        Toast.makeText(mediaService, "execute::onTimelineChanged()", Toast.LENGTH_LONG).show()
        Timber.d("____________>>-----TIMELINE=${timeline}")
    }
}
