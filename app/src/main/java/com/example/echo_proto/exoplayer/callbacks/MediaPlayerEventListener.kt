package com.example.echo_proto.exoplayer.callbacks

import android.widget.Toast
import com.example.echo_proto.exoplayer.MediaService
import com.google.android.exoplayer2.*
import timber.log.Timber

class MediaPlayerEventListener(
    private val mediaService: MediaService
): Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(mediaService, "Error was happened. ::eventListener", Toast.LENGTH_LONG).show()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if (playbackState == Player.STATE_READY && !playWhenReady) {
            mediaService.stopForeground(false)
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
