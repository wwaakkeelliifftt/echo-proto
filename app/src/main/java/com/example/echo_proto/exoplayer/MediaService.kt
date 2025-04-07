package com.example.echo_proto.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.preference.PreferenceManager
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.callbacks.MediaPlayerNotificationListener
import com.example.echo_proto.exoplayer.callbacks.MediaPlayerEventListener
import com.example.echo_proto.util.Constants
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MediaService : MediaBrowserServiceCompat() {

    var isForegroundService = false
    private var isPlayerInitialized = false

    private var currentPlayingEpisode: Episode? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    @Inject lateinit var mediaSource: MediaSource
    @Inject lateinit var dataSourceFactory: DefaultDataSource.Factory
    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var mediaNotificationManager: MediaNotificationManager
    private lateinit var mediaPlayerEventListener: MediaPlayerEventListener

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            mediaSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { actIntent ->
            PendingIntent.getActivity(this, 0, actIntent, PendingIntent.FLAG_MUTABLE) // todo: or FLAG = 0?
        }

        mediaSession = MediaSessionCompat(this, Constants.MUSIC_SERVICE).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        // associate our created token with root @sessionToken from MediaBrowserServiceCompat()
        sessionToken = mediaSession.sessionToken

        mediaNotificationManager = MediaNotificationManager(
            this,
            mediaSession.sessionToken,
            MediaPlayerNotificationListener(this)
        )

        val mediaPlaybackPreparer = MediaPlaybackPreparer(mediaSource = mediaSource) {
            currentPlayingEpisode = it
            preparePlayer(
                episodes = mediaSource.episodes,
                episodeToPlay = currentPlayingEpisode,
                episodeTimePosition = currentPlayingEpisode?.stopListeningAt ?: 0L, //sharedPreferences.getLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, 0L),
                playNow = true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlaybackPreparer(mediaPlaybackPreparer)
            setQueueNavigator(MusicQueueNavigator())
            setPlayer(exoPlayer)
        }

        mediaPlayerEventListener = MediaPlayerEventListener(this)
        exoPlayer.addListener(mediaPlayerEventListener)
        mediaNotificationManager.showNotification(exoPlayer)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoPlayer.removeListener(mediaPlayerEventListener)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return mediaSource.episodes[windowIndex].asMediaDescriptionCompat()
        }
    }



    private fun preparePlayer(
        episodes: List<Episode>,
        episodeToPlay: Episode?,
        episodeTimePosition: Long,
        playNow: Boolean
    ) {
        val currentEpisodeIndex = episodes.indexOfFirst { it.id == episodeToPlay?.id }
        if (currentEpisodeIndex != -1) {
            exoPlayer.apply {
                setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory))
                prepare()
                seekTo(currentEpisodeIndex, episodeTimePosition)
                playWhenReady = playNow
            }
        } else {
            // Обработка случая, когда эпизод не найден
            Timber.e("Episode not found in the list")
        }

       /** old version*/
//        val currentEpisodeIndex = if (currentPlayingEpisode == null) 0 else episodes.indexOf(episodeToPlay)
//        exoPlayer.apply {
//            setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory))
//            prepare()
//            seekTo(currentEpisodeIndex, 0L)
//            playWhenReady = playNow
//        }
    }

    fun setPlayerSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    // we set root/parent to the "default" const value
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(Constants.MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            Constants.MEDIA_ROOT_ID -> {
                val resultSent = mediaSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(mediaSource.asMediaItems())
                        if (!isPlayerInitialized && mediaSource.episodes.isNotEmpty()) {
                            val lastEpisodeId = sharedPreferences.getString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, "") ?: ""
                            val pauseTimePosition = sharedPreferences.getLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, 0)
                            preparePlayer(
                                mediaSource.episodes,
                                if (lastEpisodeId.isNotEmpty()) {
                                    mediaSource.episodes.find { it.mediaId == lastEpisodeId }
                                }  else mediaSource.episodes[0],
                                pauseTimePosition,
                                false
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(Constants.NETWORK_ERROR, null) // message to MediaServiceConnection
                        result.sendResult(null)
                    }
                }
                if (!resultSent) {
                    result.detach()
                }
            }
            // hz.. maybe we need implementation for each list of episodes
            Constants.MEDIA_QUEUE_ID -> {}
            Constants.MEDIA_FEED_ID -> {}
        }
    }
}