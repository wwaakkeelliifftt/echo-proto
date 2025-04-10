package com.example.echo_proto.exoplayer

import com.example.echo_proto.domain.model.Episode
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.upstream.DefaultDataSource
import javax.inject.Inject

class AudioPlayerManager @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val mediaSource: MediaSource,
    private val dataSourceFactory: DefaultDataSource.Factory
) {
    private var currentEpisode: Episode? = null

    fun playEpisode(episode: Episode, startPosition: Long = 0) {
        currentEpisode = episode
        exoPlayer.setMediaSource(mediaSource.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(startPosition)
        exoPlayer.playWhenReady = true
    }

    // ... другие методы управления плеером
    fun play(episode: Episode, startPosition: Long = 0) {
        currentEpisode = episode
        exoPlayer.apply {
            setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory))
            prepare()
//            seekTo(currentEpisodeIndex, episodeTimePosition)
//            playWhenReady = playNow
        }
    }

    fun setPlayerSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }
}