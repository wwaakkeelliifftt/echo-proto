package com.example.echo_proto.ui.common

import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.LifecycleOwner
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.ui.viewmodels.MainViewModel

interface PlaybackStateAware {
    fun updatePlaybackState(playingEpisodeId: Int?, isPlaying: Boolean)
}

fun LifecycleOwner.observePlaybackState(
    mainViewModel: MainViewModel,
    playbackStateAware: PlaybackStateAware
) {
    mainViewModel.playbackState.observe(this) { playbackState ->
        val isPlaying = playbackState?.isPlaying == true
        val currentId = mainViewModel.currentPlayingEpisodeFromMediaServiceConnection.value
            ?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            ?.toIntOrNull()

        playbackStateAware.updatePlaybackState(currentId, isPlaying)
    }

    mainViewModel.currentPlayingEpisodeFromMediaServiceConnection.observe(this) { metadata ->
        val isPlaying = mainViewModel.playbackState.value?.isPlaying == true
        val currentId = metadata
            ?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            ?.toIntOrNull()

        playbackStateAware.updatePlaybackState(currentId, isPlaying)
    }
}
