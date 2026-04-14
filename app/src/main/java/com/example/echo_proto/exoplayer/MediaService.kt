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

    private var currentPlayingEpisode: Episode? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    // Переменные для отслеживания последнего состояния playbackState
    // чтобы не обновлять его без необходимости
    private var lastPlaybackState: Int = PlaybackStateCompat.STATE_NONE
    private var lastPlaybackPosition: Long = 0L
    private var lastPlaybackSpeed: Float = 1f
    private var lastDbBackupTime: Long = 0L // Track last DB backup save time

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

        val savedSpeed = sharedPreferences.getFloat(
            Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY,
            Constants.DEFAULT_PLAYBACK_SPEED
        ).coerceAtLeast(0.1f)
        Timber.tag("SPEED").d("0) MediaService.onCreate -> restoring saved speed=%.2f", savedSpeed)
        setPlayerSpeed(savedSpeed)

        val mediaPlaybackPreparer = MediaPlaybackPreparer(mediaSource = mediaSource) {
            currentPlayingEpisode = it
            serviceScope.launch {
                preparePlayer(
                    episodes = mediaSource.episodes,
                    episodeToPlay = currentPlayingEpisode,
                    episodeTimePosition = currentPlayingEpisode?.stopListeningAt ?: 0L, //sharedPreferences.getLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, 0L),
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
        
        // Запускаем периодическое обновление позиции воспроизведения
        startPeriodicPositionUpdate()
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

                    // Обновляем playbackState только если состояние или позиция действительно изменились
                    // Используем порог для позиции (1 секунда), чтобы не обновлять слишком часто из-за небольших изменений
                    val positionChanged = kotlin.math.abs(position - lastPlaybackPosition) > 1000
                    val stateChanged = playbackState != lastPlaybackState
                    val speedChanged = abs(currentSpeed - lastPlaybackSpeed) > 0.001f

                    // Отслеживаем изменение playWhenReady (playing -> paused)
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

                        // Advertise custom action for playback speed so controllers know about it
                        val speedAction = PlaybackStateCompat.CustomAction.Builder(
                            Constants.MEDIA_SESSION_ACTION_SET_SPEED,
                            getString(R.string.speed_control_title),
                            R.drawable.ic_menu_play
                        ).build()
                        playbackStateBuilder.addCustomAction(speedAction)

                        mediaSession.setPlaybackState(playbackStateBuilder.build())

                        // Event-driven DB save strategy:
                        // 1. Save to DB on pause/stop (when playback state changes from playing to paused)
                        // 2. Save to DB periodically as backup (every 30 seconds)
                        // 3. Always save to SharedPreferences (doesn't trigger Flow emissions)
                        val currentEpisodeId = currentPlayingEpisode?.id
                        if (currentEpisodeId != null && position > 0) {
                            val shouldSaveToDb = playbackStateChangedToPaused ||
                                    (System.currentTimeMillis() - lastDbBackupTime > Constants.DB_BACKUP_SAVE_INTERVAL)

                            if (shouldSaveToDb) {
                                // Сохраняем позицию в БД
                                mediaSource.updateEpisodePosition(currentEpisodeId, position)
                                lastDbBackupTime = System.currentTimeMillis()
                                Timber.tag("PLAYBACK").d("DB save: episodeId=$currentEpisodeId, position=$position, reason=${if (playbackStateChangedToPaused) "pause" else "backup"}")
                            }

                            // Всегда сохраняем в SharedPreferences для быстрого доступа (не триггерит Flow emissions)
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
        // Сохраняем последнюю позицию перед уничтожением сервиса
        try {
            val position = exoPlayer.currentPosition
            val currentEpisodeId = currentPlayingEpisode?.id
            if (currentEpisodeId != null && position > 0) {
                // Используем runBlocking для синхронного сохранения
                runBlocking(Dispatchers.IO) {
                    mediaSource.updateEpisodePosition(currentEpisodeId, position)
                }
                sharedPreferences.edit()
                    .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, currentEpisodeId.toString())
                    .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, position)
                    .apply()
                Timber.d("MediaService: Saved playback position onDestroy: episodeId=$currentEpisodeId, position=$position")
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
        // Сохраняем позицию перед остановкой
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
                    Timber.tag("PLAY_FLOW").d("Episode finished: id=$finishedEpisodeId, currentExoIndex=$currentExoIndex, isAutoTransition=$isAutoTransition")

                    // Save final position to DB before marking as listened
                    if (currentExoPosition > 0) {
                        mediaSource.updateEpisodePosition(finishedEpisodeId, currentExoPosition)
                        sharedPreferences.edit()
                            .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, finishedEpisodeId.toString())
                            .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, currentExoPosition)
                            .apply()
                        Timber.tag("PLAY_FLOW").d("Saved final position: episodeId=$finishedEpisodeId, position=$currentExoPosition")
                    }

                    // 1. Помечаем эпизод как прослушанный (это уберет его из очереди в БД)
                    mediaSource.markEpisodeAsListened(finishedEpisodeId)
                    
                    // 2. Обновляем локальный список в mediaSource (он теперь без прослушанного эпизода)
                    mediaSource.refreshMediaData()
                    
                    val newPlaylist = mediaSource.episodes
                    val wasPlaying = exoPlayer.playWhenReady || exoPlayer.isPlaying
                    
                    if (newPlaylist.isNotEmpty()) {
                        // Определяем, какой индекс должен быть следующим в НОВОМ плейлисте.
                        val nextIndexInNewList = if (isAutoTransition) {
                            (currentExoIndex - 1).coerceAtLeast(0)
                        } else {
                            currentExoIndex.coerceAtMost(newPlaylist.size - 1)
                        }
                        
                        Timber.tag("PLAY_FLOW").d("Updating ExoPlayer with new playlist. nextIndexInNewList=$nextIndexInNewList, size=${newPlaylist.size}")
                        
                        // 🔧 FIXED: Use resetPosition = false to avoid unnecessary gaps if possible
                        // But since we are changing the underlying media source, we still need to seek.
                        exoPlayer.setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory), false)
                        exoPlayer.prepare()
                        
                        if (nextIndexInNewList in newPlaylist.indices) {
                            val nextEpisode = newPlaylist[nextIndexInNewList]
                            currentPlayingEpisode = nextEpisode
                            
                            // 🔧 FIXED: Preserve position if auto-transitioned, to avoid "restart from 0" feel
                            val seekPosition = if (isAutoTransition) {
                                // If it's auto-transition, the player already moved forward.
                                // We take the CURRENT position it managed to reach during DB update.
                                exoPlayer.currentPosition.coerceAtLeast(currentExoPosition)
                            } else {
                                0L
                            }
                            
                            exoPlayer.seekTo(nextIndexInNewList, seekPosition)
                            exoPlayer.playWhenReady = wasPlaying
                            Timber.tag("PLAY_FLOW").d("Playing next: ${nextEpisode.title} at $seekPosition ms")
                        } else {
                            Timber.tag("PLAY_FLOW").d("No more episodes to play, stopping.")
                            exoPlayer.stop()
                            currentPlayingEpisode = null
                        }
                    } else {
                        Timber.tag("PLAY_FLOW").d("Queue is empty, stopping playback")
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

        // Проверяем доступность URL только для веб-ссылок, не для локальных файлов
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
            // Для локальных файлов проверяем существование файла
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
        Timber.tag("SPEED").d("5) MediaService.setPlayerSpeed -> applying speed=%.2f", speed)
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    private fun createSetSpeedActionProvider(): CustomActionProvider {
        return object : CustomActionProvider {
            override fun onCustomAction(
                player: Player,
                action: String,
                extras: Bundle?
            ) {
                if (action == Constants.MEDIA_SESSION_ACTION_SET_SPEED) {
                    val speed = extras?.getFloat(Constants.EXTRA_PLAYBACK_SPEED)
                    Timber.tag("SPEED").d("4) MediaService -> custom action received speed=%s", speed?.let { String.format("%.2f", it) } ?: "null")
                    if (speed != null && speed > 0f) {
                        setPlayerSpeed(speed)
                    }
                }
            }

            override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
                Timber.tag("SPEED").d("4a) MediaService -> providing custom action")
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
                    Timber.tag("PLAY").d("🔄 MediaService -> received update queue action")
                    updatePlaylist()
                }
            }

            override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
                return PlaybackStateCompat.CustomAction.Builder(
                    Constants.MEDIA_SESSION_ACTION_UPDATE_QUEUE,
                    "Update Queue",
                    R.drawable.ic_menu_play
                ).build()
            }
        }
    }

    /** mozhet v prepare i dobavit' obertku scope ? */
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
        serviceScope.launch {
            mediaSource.refreshMediaData()
            // Обновляем плейлист ExoPlayer если он уже инициализирован
            if (mediaSource.episodes.isNotEmpty()) {
                val currentMediaItemIndex = exoPlayer.currentMediaItemIndex
                val currentPosition = exoPlayer.currentPosition
                val wasPlaying = exoPlayer.isPlaying
                val currentEpisodes = mediaSource.episodes
                val newSnapshot = currentEpisodes.map { it.id }
                
                // Проверяем, изменился ли плейлист перед переинициализацией
                // Сравниваем размер и текущий элемент (если возможно)
                val playlistSizeChanged = currentEpisodes.size != exoPlayer.mediaItemCount
                val playlistOrderChanged = newSnapshot != lastPlaylistSnapshot
                
                // Если размер изменился, точно нужно обновить
                // Если размер не изменился, но текущий индекс невалидный, тоже нужно обновить
                val needsUpdate = playlistSizeChanged || 
                        playlistOrderChanged ||
                        currentMediaItemIndex < 0 || 
                        currentMediaItemIndex >= currentEpisodes.size
                
                if (needsUpdate) {
                    Timber.tag("PLAY").d("Updating ExoPlayer playlist: ${currentEpisodes.size} episodes (was ${exoPlayer.mediaItemCount}), currentIndex=$currentMediaItemIndex, wasPlaying=$wasPlaying, orderChanged=$playlistOrderChanged")
                    exoPlayer.setMediaSource(mediaSource.asMediaSource(dataSourceFactory = dataSourceFactory), false)
                    exoPlayer.prepare()
                    
                    // Восстанавливаем позицию если возможно
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
                        // Восстанавливаем состояние воспроизведения после обновления
                        exoPlayer.playWhenReady = wasPlaying
                        currentEpisodes.getOrNull(targetIndex)?.let { currentPlayingEpisode = it }
                    }
                    lastPlaylistSnapshot = newSnapshot
                } else {
                    Timber.tag("PLAY").d("ExoPlayer Playlist unchanged (${currentEpisodes.size} episodes), skipping update")
                    lastPlaylistSnapshot = newSnapshot
                }
            }
        }
    }
    /** mozhet i ne nado stop otsyuda ? */
    fun stopPlayback() {
        exoPlayer.stop()
        stopForeground(true)
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
            Constants.MEDIA_ROOT_ID, Constants.MEDIA_QUEUE_ID -> {
                // Загружаем эпизоды из очереди
                result.detach()
                serviceScope.launch {
                    Timber.tag("PLAY").d("📂 onLoadChildren called for parentId=$parentId")
                    mediaSource.refreshMediaData()
                    Timber.tag("PLAY").d("📂 MediaSource refreshed, episodes count=${mediaSource.episodes.size}")
                    mediaSource.whenReady { isInitialized ->
                        if (!isInitialized || mediaSource.episodes.isEmpty()) {
                            Timber.tag("PLAY").w("⚠️ MediaSource not initialized or empty, returning empty list")
                            // Возвращаем пустой список вместо ошибки для безопасности
                            result.sendResult(mutableListOf())
                            return@whenReady
                        }
                        val items = mediaSource.asMediaItems()
                        Timber.tag("PLAY").d("📂 Sending ${items.size} items to client")
                        items.forEachIndexed { index, item ->
                            Timber.tag("PLAY").d("  [$index] id=${item.mediaId}, title=${item.description.title}")
                        }
                        result.sendResult(items)
                        
                        // Только для MEDIA_ROOT_ID (первое подключение) запускаем с последней позиции
                        if (parentId == Constants.MEDIA_ROOT_ID) {
                            startPlaybackFromLastPosition()
                        } else if (isPlayerInitialized) {
                            // Для MEDIA_QUEUE_ID обновляем плейлист, если плеер уже работает
                            updatePlaylist()
                        }
                    }
                }
            }
        }
    }

    private fun startPlaybackFromLastPosition() {
        if (isPlayerInitialized) return

        serviceScope.launch {
            // Сначала пытаемся найти эпизод по ID из SharedPreferences
            val lastEpisodeId = sharedPreferences.getString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, "")
            val lastPositionFromPrefs = sharedPreferences.getLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, 0L)
            
            val episode = lastEpisodeId?.takeIf { it.isNotBlank() }
                ?.toIntOrNull()
                ?.let { id -> mediaSource.episodes.find { it.id == id } }
                ?: mediaSource.episodes.firstOrNull()

            episode?.let {
                // Используем позицию из БД (stopListeningAt), если она есть, иначе из SharedPreferences
                val positionToUse = if (it.stopListeningAt > 0) it.stopListeningAt else lastPositionFromPrefs
                
                Timber.d("Starting playback from last position: episodeId=${it.id}, position=$positionToUse")
                preparePlayer(
                    episodes = mediaSource.episodes,
                    episodeToPlay = it,
                    episodeTimePosition = positionToUse,
                    playNow = false
                )
                currentPlayingEpisode = it
                isPlayerInitialized = true
            }
        }
    }

    private fun sendError(result: Result<*>, message: String) {
        mediaSession.sendSessionEvent(Constants.ERROR_EVENT, Bundle().apply {
            putString("message", message)
        })
        result.sendResult(null)
    }
}