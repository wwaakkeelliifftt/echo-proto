package com.example.echo_proto.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.echo_proto.R
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.callbacks.MediaPlayerEventListener
import com.example.echo_proto.exoplayer.callbacks.MediaPlayerNotificationListener
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.NetworkUtils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import timber.log.Timber
import javax.inject.Inject
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.CustomActionProvider

@AndroidEntryPoint
class MediaService : MediaBrowserServiceCompat() {

    var isForegroundService = false
    private var isPlayerInitialized = false
    private var isInitializing = false

    private var currentPlayingEpisode: Episode? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private var lastPlaybackState: Int = PlaybackStateCompat.STATE_NONE
    private var lastPlaybackPosition: Long = 0L
    private var lastPlaybackSpeed: Float = 1f
    private var lastDbBackupTime: Long = 0L

    @Inject lateinit var mediaSource: MediaSource
    @Inject lateinit var dataSourceFactory: DefaultDataSource.Factory
    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var sharedPreferences: SharedPreferences
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var mediaNotificationManager: MediaNotificationManager
    private lateinit var mediaPlayerEventListener: MediaPlayerEventListener
    private var lastPlaylistSnapshot: List<Int> = emptyList()

    override fun onCreate() {
        super.onCreate()
        
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let { actIntent ->
            PendingIntent.getActivity(this, 0, actIntent, PendingIntent.FLAG_MUTABLE)
        }

        mediaSession = MediaSessionCompat(this, Constants.MUSIC_SERVICE).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        mediaNotificationManager = MediaNotificationManager(
            this,
            mediaSession.sessionToken,
            MediaPlayerNotificationListener(this)
        )

        val savedSpeed = sharedPreferences.getFloat(
            Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY,
            Constants.DEFAULT_PLAYBACK_SPEED
        ).coerceAtLeast(0.1f)
        setPlayerSpeed(savedSpeed)

        val mediaPlaybackPreparer = MediaPlaybackPreparer(mediaSource = mediaSource) {
            currentPlayingEpisode = it
            serviceScope.launch {
                preparePlayer(
                    episodes = mediaSource.episodes,
                    episodeToPlay = currentPlayingEpisode,
                    episodeTimePosition = currentPlayingEpisode?.stopListeningAt ?: 0L,
                    playNow = true
                )
            }
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession).apply {
            setPlaybackPreparer(mediaPlaybackPreparer)
            setQueueNavigator(MusicQueueNavigator())
            setPlayer(exoPlayer)
            setCustomActionProviders(
                createSetSpeedActionProvider(),
                createUpdateQueueActionProvider()
            )
        }

        mediaPlayerEventListener = MediaPlayerEventListener(this, mediaSession)
        exoPlayer.addListener(mediaPlayerEventListener)
        mediaNotificationManager.showNotification(exoPlayer)
        
        startPeriodicPositionUpdate()

        // 🚀 CRITICAL: We must initialize BEFORE anything else can touch the player
        isInitializing = true
        serviceScope.launch {
            mediaSource.fetchMediaData()
            startPlaybackFromLastPosition()
            isInitializing = false
        }
    }

    private fun startPeriodicPositionUpdate() {
        serviceScope.launch {
            while (true) {
                try {
                    val position = exoPlayer.currentPosition
                    val state = exoPlayer.playbackState
                    val playWhenReady = exoPlayer.playWhenReady
                    val currentSpeed = exoPlayer.playbackParameters.speed
                    
                    val playbackState = when {
                        state == Player.STATE_READY && playWhenReady -> PlaybackStateCompat.STATE_PLAYING
                        state == Player.STATE_READY && !playWhenReady -> PlaybackStateCompat.STATE_PAUSED
                        state == Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                        else -> PlaybackStateCompat.STATE_NONE
                    }

                    val positionChanged = abs(position - lastPlaybackPosition) > 1000
                    val stateChanged = playbackState != lastPlaybackState
                    val speedChanged = abs(currentSpeed - lastPlaybackSpeed) > 0.001f

                    val wasPlaying = lastPlaybackState == PlaybackStateCompat.STATE_PLAYING
                    val isPlaying = playbackState == PlaybackStateCompat.STATE_PLAYING
                    val playbackStateChangedToPaused = wasPlaying && !isPlaying

                    if (stateChanged || positionChanged || speedChanged) {
                        lastPlaybackState = playbackState
                        lastPlaybackPosition = position
                        lastPlaybackSpeed = currentSpeed

                        val playbackStateBuilder = PlaybackStateCompat.Builder()
                            .setState(playbackState, position, currentSpeed)
                            .setActions(
                                PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                                PlaybackStateCompat.ACTION_SEEK_TO
                            )

                        val speedAction = PlaybackStateCompat.CustomAction.Builder(
                            Constants.MEDIA_SESSION_ACTION_SET_SPEED,
                            getString(R.string.speed_control_title),
                            R.drawable.ic_menu_play
                        ).build()
                        playbackStateBuilder.addCustomAction(speedAction)

                        mediaSession.setPlaybackState(playbackStateBuilder.build())

                        val currentEpisodeId = currentPlayingEpisode?.id
                        if (currentEpisodeId != null && position > 0 && !isInitializing) {
                            val shouldSaveToDb = playbackStateChangedToPaused ||
                                    (System.currentTimeMillis() - lastDbBackupTime > Constants.DB_BACKUP_SAVE_INTERVAL)

                            if (shouldSaveToDb) {
                                mediaSource.updateEpisodePosition(currentEpisodeId, position)
                                lastDbBackupTime = System.currentTimeMillis()
                            }

                            sharedPreferences.edit()
                                .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, currentEpisodeId.toString())
                                .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, position)
                                .apply()
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error updating playback state position")
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val position = exoPlayer.currentPosition
            val currentEpisodeId = currentPlayingEpisode?.id
            if (currentEpisodeId != null && position > 0) {
                runBlocking(Dispatchers.IO) {
                    mediaSource.updateEpisodePosition(currentEpisodeId, position)
                }
                sharedPreferences.edit()
                    .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, currentEpisodeId.toString())
                    .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, position)
                    .apply()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving playback position onDestroy")
        }
        serviceScope.cancel()
        exoPlayer.removeListener(mediaPlayerEventListener)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        try {
            val position = exoPlayer.currentPosition
            val currentEpisodeId = currentPlayingEpisode?.id
            if (currentEpisodeId != null && position > 0) {
                runBlocking(Dispatchers.IO) {
                    mediaSource.updateEpisodePosition(currentEpisodeId, position)
                }
                sharedPreferences.edit()
                    .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, currentEpisodeId.toString())
                    .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, position)
                    .apply()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving playback position onTaskRemoved")
        }
        exoPlayer.stop()
    }

    fun onEpisodePlaybackEnded(isAutoTransition: Boolean = false) {
        serviceScope.launch {
            try {
                val finishedEpisodeId = currentPlayingEpisode?.id
                val currentExoIndex = exoPlayer.currentMediaItemIndex
                val currentExoPosition = exoPlayer.currentPosition

                if (finishedEpisodeId != null) {
                    if (currentExoPosition > 0) {
                        mediaSource.updateEpisodePosition(finishedEpisodeId, currentExoPosition)
                        sharedPreferences.edit()
                            .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, finishedEpisodeId.toString())
                            .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, currentExoPosition)
                            .apply()
                    }

                    mediaSource.markEpisodeAsListened(finishedEpisodeId)
                    mediaSource.refreshMediaData()
                    
                    val newPlaylist = mediaSource.episodes
                    val wasPlaying = exoPlayer.playWhenReady || exoPlayer.isPlaying
                    
                    if (newPlaylist.isNotEmpty()) {
                        val nextIndexInNewList = if (isAutoTransition) {
                            (currentExoIndex - 1).coerceAtLeast(0)
                        } else {
                            currentExoIndex.coerceAtMost(newPlaylist.size - 1)
                        }
                        
                        exoPlayer.setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory), false)
                        exoPlayer.prepare()
                        
                        if (nextIndexInNewList in newPlaylist.indices) {
                            val nextEpisode = newPlaylist[nextIndexInNewList]
                            currentPlayingEpisode = nextEpisode
                            
                            val seekPosition = if (isAutoTransition) {
                                exoPlayer.currentPosition.coerceAtLeast(currentExoPosition)
                            } else {
                                0L
                            }
                            
                            exoPlayer.seekTo(nextIndexInNewList, seekPosition)
                            exoPlayer.playWhenReady = wasPlaying
                        } else {
                            exoPlayer.stop()
                            currentPlayingEpisode = null
                        }
                    } else {
                        exoPlayer.stop()
                        currentPlayingEpisode = null
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling episode playback ended")
            }
        }
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return mediaSource.episodes[windowIndex].asMediaDescriptionCompat()
        }
    }

    private suspend fun preparePlayer(
        episodes: List<Episode>,
        episodeToPlay: Episode?,
        episodeTimePosition: Long,
        playNow: Boolean
    ) {
        val currentEpisodeIndex = episodes.indexOfFirst { it.id == episodeToPlay?.id }
        if (currentEpisodeIndex == -1) {
            Timber.e("Episode not found in the list")
            return
        }

        val currentEpisode = episodes[currentEpisodeIndex]
        if (!currentEpisode.isDownloaded) {
            val episodeUrl = currentEpisode.audioLink
            if (!NetworkUtils.isUrlAvailable(episodeUrl)) {
                Timber.e("Media URL is not available: $episodeUrl")
                mediaSession.sendSessionEvent(Constants.EVENT_AUDIO_UNAVAILABLE, Bundle().apply {
                    putString("title", currentEpisode.title)
                })
                return
            }
        } else {
            val file = java.io.File(currentEpisode.downloadUrl)
            if (!file.exists()) {
                Timber.e("Downloaded file not found: ${currentEpisode.downloadUrl}")
                mediaSession.sendSessionEvent(Constants.EVENT_AUDIO_UNAVAILABLE, Bundle().apply {
                    putString("title", currentEpisode.title)
                })
                return
            }
        }

        exoPlayer.apply {
            setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory))
            prepare()
            seekTo(currentEpisodeIndex, episodeTimePosition)
            playWhenReady = playNow
        }
    }

    fun setPlayerSpeed(speed: Float) {
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    private fun createSetSpeedActionProvider(): CustomActionProvider {
        return object : CustomActionProvider {
            override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
                if (action == Constants.MEDIA_SESSION_ACTION_SET_SPEED) {
                    val speed = extras?.getFloat(Constants.EXTRA_PLAYBACK_SPEED)
                    if (speed != null && speed > 0f) {
                        setPlayerSpeed(speed)
                    }
                }
            }
            override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
                return PlaybackStateCompat.CustomAction.Builder(
                    Constants.MEDIA_SESSION_ACTION_SET_SPEED,
                    getString(R.string.speed_control_title),
                    R.drawable.ic_menu_play
                ).build()
            }
        }
    }

    private fun createUpdateQueueActionProvider(): CustomActionProvider {
        return object : CustomActionProvider {
            override fun onCustomAction(player: Player, action: String, extras: Bundle?) {
                if (action == Constants.MEDIA_SESSION_ACTION_UPDATE_QUEUE) {
                    updatePlaylist()
                }
            }
            override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
                return PlaybackStateCompat.CustomAction.Builder(Constants.MEDIA_SESSION_ACTION_UPDATE_QUEUE, "Update Queue", R.drawable.ic_menu_play).build()
            }
        }
    }

    fun startPlayback(episode: Episode) {
        serviceScope.launch {
            preparePlayer(
                episodes = mediaSource.episodes,
                episodeToPlay = episode,
                episodeTimePosition = 0L,
                playNow = true
            )
        }
    }

    fun updatePlaylist() {
        if (isInitializing) return
        
        serviceScope.launch {
            mediaSource.refreshMediaData()
            if (mediaSource.episodes.isNotEmpty()) {
                val currentMediaItemIndex = exoPlayer.currentMediaItemIndex
                val currentPosition = exoPlayer.currentPosition
                val wasPlaying = exoPlayer.isPlaying
                val currentEpisodes = mediaSource.episodes
                val newSnapshot = currentEpisodes.map { it.id }
                
                val playlistSizeChanged = currentEpisodes.size != exoPlayer.mediaItemCount
                val playlistOrderChanged = newSnapshot != lastPlaylistSnapshot
                
                val needsUpdate = playlistSizeChanged || 
                        playlistOrderChanged ||
                        currentMediaItemIndex < 0 || 
                        currentMediaItemIndex >= currentEpisodes.size
                
                if (needsUpdate) {
                    Timber.tag("PLAY").d("Updating ExoPlayer playlist: ${currentEpisodes.size} episodes")
                    exoPlayer.setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory), false)
                    exoPlayer.prepare()
                    
                    val currentEpisodeId = currentPlayingEpisode?.id
                        ?: exoPlayer.currentMediaItem?.mediaId?.toIntOrNull()
                    val targetIndex = currentEpisodeId?.let { id -> newSnapshot.indexOf(id) }
                        ?: currentMediaItemIndex.takeIf { it in currentEpisodes.indices }
                        ?: if (currentEpisodes.isNotEmpty()) 0 else -1
                    
                    if (targetIndex >= 0) {
                        val resumePosition = if (currentEpisodeId != null && newSnapshot.contains(currentEpisodeId)) {
                            currentPosition
                        } else {
                            0L
                        }
                        exoPlayer.seekTo(targetIndex, resumePosition)
                        exoPlayer.playWhenReady = wasPlaying
                        currentEpisodes.getOrNull(targetIndex)?.let { currentPlayingEpisode = it }
                    }
                    lastPlaylistSnapshot = newSnapshot
                }
            }
        }
    }

    fun stopPlayback() {
        exoPlayer.stop()
        stopForeground(true)
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(Constants.MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            Constants.MEDIA_ROOT_ID, Constants.MEDIA_QUEUE_ID -> {
                result.detach()
                serviceScope.launch {
                    if (mediaSource.episodes.isEmpty()) {
                        mediaSource.refreshMediaData()
                    }
                    mediaSource.whenReady { isInitialized ->
                        if (!isInitialized || mediaSource.episodes.isEmpty()) {
                            result.sendResult(mutableListOf())
                            return@whenReady
                        }
                        val items = mediaSource.asMediaItems()
                        result.sendResult(items)
                    }
                }
            }
        }
    }

    private fun startPlaybackFromLastPosition() {
        if (isPlayerInitialized) return
        
        val lastEpisodeId = sharedPreferences.getString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, "")
        val lastPositionFromPrefs = sharedPreferences.getLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, 0L)
        
        val episode = lastEpisodeId?.takeIf { it.isNotBlank() }
            ?.toIntOrNull()
            ?.let { id -> mediaSource.episodes.find { it.id == id } }
            ?: mediaSource.episodes.firstOrNull()

        episode?.let {
            val positionToUse = if (it.stopListeningAt > 0) it.stopListeningAt else lastPositionFromPrefs
            
            Timber.tag("PLAY").d("✅ RESTORING: episodeId=${it.id}, pos=$positionToUse")
            
            currentPlayingEpisode = it
            serviceScope.launch {
                preparePlayer(
                    episodes = mediaSource.episodes,
                    episodeToPlay = it,
                    episodeTimePosition = positionToUse,
                    playNow = false
                )
            }
            isPlayerInitialized = true
        }
    }

    private fun sendError(result: Result<*>, message: String) {
        mediaSession.sendSessionEvent(Constants.ERROR_EVENT, Bundle().apply {
            putString("message", message)
        })
        result.sendResult(null)
    }
}