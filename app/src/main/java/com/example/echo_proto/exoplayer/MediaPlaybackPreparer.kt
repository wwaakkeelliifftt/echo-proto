package com.example.echo_proto.exoplayer

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.echo_proto.domain.model.Episode
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector

class MediaPlaybackPreparer(
    private val mediaSource: MediaSource,
    private val playerPrepared: (Episode?) -> Unit
) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long =
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        mediaSource.whenReady {
            val episodeToPlay = mediaSource.episodes.find { mediaId == it.mediaId }
            playerPrepared.invoke(episodeToPlay)
        }
    }

    override fun onCommand(player: Player, command: String, extras: Bundle?, cb: ResultReceiver?): Boolean = false
    override fun onPrepare(playWhenReady: Boolean) = Unit
    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) = Unit
    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
}
