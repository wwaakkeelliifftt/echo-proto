package com.example.echo_proto.ui.viewmodels

import android.content.SharedPreferences
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.exoplayer.MediaServiceConnection
import com.example.echo_proto.exoplayer.currentStatePosition
import com.example.echo_proto.exoplayer.isPlayEnabled
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.exoplayer.isPrepared
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.isCloseTo
import com.example.echo_proto.util.normalizePlaybackSpeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediaServiceConnection: MediaServiceConnection,
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    private val _mediaId = MutableLiveData<String>()
    val mediaId: LiveData<String> get() = _mediaId

    private val _mediaItems = MutableLiveData<Resource<List<Episode>>>()
    val mediaItems: LiveData<Resource<List<Episode>>> get() = _mediaItems

    val isConnected = mediaServiceConnection.isConnected
    val networkError = mediaServiceConnection.networkError
    val playbackState = mediaServiceConnection.playbackState
    val currentPlayingEpisodeFromMediaServiceConnection = mediaServiceConnection.currentPlayingEpisode

    private val _currentEpisodeFromDb = MutableLiveData<Episode>()
    val currentEpisodeFromDb: LiveData<Episode> get() = _currentEpisodeFromDb

    private val _currentEpisodeDuration = MutableLiveData<Long>()
    val currentEpisodeDuration: LiveData<Long> get() = _currentEpisodeDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition: LiveData<Long> get() = _currentPlayerPosition

    private val _currentPlaybackSpeed = MutableLiveData<Float>()
    val currentPlaybackSpeed: LiveData<Float> get() = _currentPlaybackSpeed

    private val _playbackSpeedPresets = MutableLiveData<List<Float>>()
    val playbackSpeedPresets: LiveData<List<Float>> get() = _playbackSpeedPresets

    private val _mediaSessionEventForSnackbar = MutableLiveData<String?>()
    val mediaSessionEventForSnackbar: LiveData<String?> get() = _mediaSessionEventForSnackbar

    private val playbackStateObserver = Observer<PlaybackStateCompat?> { state ->
        val speed = state?.playbackSpeed ?: return@Observer
        if (speed <= 0f) return@Observer
        Timber.tag("SPEED").d("6) playbackStateObserver -> session speed=%.2f", speed)
        if (!_currentPlaybackSpeed.value.isCloseTo(speed)) {
            _currentPlaybackSpeed.postValue(speed)
            sharedPreferences.edit()
                .putFloat(Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY, speed)
                .apply()
        }
    }

    init {
        _mediaId.value = sharedPreferences.getString(Constants.SHARED_PREFERENCE_MEDIA_ID_KEY, Constants.MEDIA_ROOT_ID)

        updateCurrentPlayerPosition()

        _currentPlaybackSpeed.value = sharedPreferences.getFloat(
            Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY,
            Constants.DEFAULT_PLAYBACK_SPEED
        )
        _playbackSpeedPresets.value = loadPlaybackSpeedPresets()

        mediaServiceConnection.playbackState.observeForever(playbackStateObserver)
    }

    fun refreshPlayerPlaylist() {
        // Обновляем подписку на MEDIA_QUEUE_ID для обновления MediaSource и плейлиста
        mediaServiceConnection.unsubscribe(Constants.MEDIA_QUEUE_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
        mediaServiceConnection.subscribe(Constants.MEDIA_QUEUE_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                Timber.d("Queue playlist refreshed: ${children.size} items")
            }
        })
    }


    fun mediaIdMapper(string: String) {
        when (string) {
            Constants.MEDIA_ROOT_ID -> {}
            Constants.MEDIA_QUEUE_ID -> {
                _mediaId.value = Constants.MEDIA_QUEUE_ID
            }
            Constants.MEDIA_FEED_ID -> {
                _mediaId.value = Constants.MEDIA_FEED_ID
            }
            Constants.MEDIA_FEED_PERSONAL_ID -> {
                _mediaId.value = Constants.MEDIA_FEED_PERSONAL_ID
            }
            Constants.MEDIA_CHANNEL_ID -> {
                _mediaId.value = Constants.MEDIA_CHANNEL_ID
            }
            Constants.MEDIA_DOWNLOADS_ID -> {
                _mediaId.value = Constants.MEDIA_DOWNLOADS_ID
            }
            else -> Unit
        }
        updateMediaChannelToSharedPref()
    }

    private fun updateMediaChannelToSharedPref() {
        sharedPreferences.edit()
            .putString(Constants.SHARED_PREFERENCE_MEDIA_ID_KEY, mediaId.value)
            .apply()
    }

    private fun updateCurrentPlayEpisodeIdToSharedPref() {
        sharedPreferences.edit()
            .putString(
                Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY,
                currentPlayingEpisodeFromMediaServiceConnection.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            )
            .putLong(
                Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY,
                playbackState.value?.currentStatePosition ?: 0L
            )
            .apply()
    }

    fun getCurrentPlayEpisode(id: Int) {
        viewModelScope.launch {
            repository.getEpisodeById(id = id).collect { result ->
                when (result) {
                    is Resource.Success -> _currentEpisodeFromDb.postValue(result.data!!)
                    else -> {}
                }
            }
        }
    }

    fun skipToNextEpisode(): Unit = mediaServiceConnection.transportControls.skipToNext()
    fun skipToPreviousEpisode(): Unit = mediaServiceConnection.transportControls.skipToPrevious()
    fun seekTo(position: Long): Unit = mediaServiceConnection.transportControls.seekTo(position)
    fun seekForward(gap: Long = 10_000L) {
        val result = (currentPlayerPosition.value ?: 0L) + gap
        currentEpisodeDuration.value?.let { totalEpisodeTime ->
            mediaServiceConnection.transportControls.seekTo(
                if (result < totalEpisodeTime) result else totalEpisodeTime
            )
        }
    }
    fun seekReplay(gap: Long = 10_000L) {
        val result = (currentPlayerPosition.value ?: 0L) - gap
        mediaServiceConnection.transportControls.seekTo(
            if (result > 0) result else 0L
        )
    }

    fun setPlaybackSpeed(requestedSpeed: Float) {
        val clamped = requestedSpeed.normalizePlaybackSpeed()
        Timber.tag("SPEED").d("2) setPlaybackSpeed -> requested=%.2f clamped=%.2f", requestedSpeed, clamped)
        if (!_currentPlaybackSpeed.value.isCloseTo(clamped)) {
            _currentPlaybackSpeed.postValue(clamped)
            Timber.tag("SPEED").d("2) setPlaybackSpeed -> posting LiveData value=%.2f", clamped)
        }
        mediaServiceConnection.setPlaybackSpeed(clamped)
        sharedPreferences.edit()
            .putFloat(Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY, clamped)
            .apply()
    }

    fun adjustPlaybackSpeed(delta: Float) {
        val current = _currentPlaybackSpeed.value ?: Constants.DEFAULT_PLAYBACK_SPEED
        val target = current + delta
        Timber.tag("SPEED").d("2) adjustPlaybackSpeed -> current=%.2f delta=%.2f target=%.2f", current, delta, target)
        setPlaybackSpeed(target)
    }

    fun addPlaybackSpeedPreset(speed: Float): Boolean {
        val normalized = speed.normalizePlaybackSpeed()
        val currentPresets = (_playbackSpeedPresets.value ?: emptyList()).toMutableList()
        if (currentPresets.any { it.isCloseTo(normalized) }) {
            return false
        }
        if (currentPresets.size >= Constants.PLAYBACK_SPEED_PRESET_LIMIT) {
            currentPresets.removeFirst()
        }
        currentPresets.add(normalized)
        currentPresets.sort()
        persistPlaybackSpeedPresets(currentPresets)
        _playbackSpeedPresets.postValue(currentPresets)
        return true
    }

    fun removePlaybackSpeedPreset(speed: Float) {
        val normalized = speed.normalizePlaybackSpeed()
        val currentPresets = (_playbackSpeedPresets.value ?: emptyList()).filterNot { it.isCloseTo(normalized) }
        persistPlaybackSpeedPresets(currentPresets)
        _playbackSpeedPresets.postValue(currentPresets)
    }

    fun formatSpeed(speed: Float): String = String.format(Locale.US, "%.2fx", speed)

    fun playOrToggleEpisode(mediaItem: Episode, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false

        if (isPrepared && mediaItem.mediaId ==
            currentPlayingEpisodeFromMediaServiceConnection.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) mediaServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> mediaServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            mediaServiceConnection.transportControls.playFromMediaId(mediaItem.id.toString(), null)
        }
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            // Периодически опрашиваем позицию из MediaController напрямую
            while (true) {
                try {
                    val playbackState = mediaServiceConnection.playbackState.value
                    val position = playbackState?.currentStatePosition ?: 0L
                    val metadata = currentPlayingEpisodeFromMediaServiceConnection.value
                    val duration = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: 0L

                    if (currentPlayerPosition.value != position && position > 0) {
                        _currentPlayerPosition.postValue(position)
                    }
                    if (currentEpisodeDuration.value != duration && duration > 0) {
                        _currentEpisodeDuration.postValue(duration)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error updating player position")
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateCurrentPlayEpisodeIdToSharedPref()
        mediaServiceConnection.playbackState.removeObserver(playbackStateObserver)
        mediaServiceConnection.unsubscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    private fun loadPlaybackSpeedPresets(): List<Float> {
        val stored = sharedPreferences.getStringSet(Constants.SHARED_PREFERENCE_SPEED_PRESETS_KEY, emptySet())
            ?.mapNotNull { it.toFloatOrNull() }
            ?.map { it.normalizePlaybackSpeed() }
            ?.distinctBy { (it * 100).roundToInt() }
            ?.sorted()
        return stored ?: emptyList()
    }

    private fun persistPlaybackSpeedPresets(presets: Collection<Float>) {
        val values = presets.map { it.normalizePlaybackSpeed() }
        sharedPreferences.edit()
            .putStringSet(Constants.SHARED_PREFERENCE_SPEED_PRESETS_KEY, values.map { it.toString() }.toSet())
            .apply()
    }


}
