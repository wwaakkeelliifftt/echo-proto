package com.example.echo_proto.ui.viewmodels

import android.content.SharedPreferences
import android.os.Bundle
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
import com.example.echo_proto.domain.worker.DownloadRepository
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediaServiceConnection: MediaServiceConnection,
    private val repository: FeedRepository,
    private val downloadRepository: DownloadRepository,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

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

        val position = state?.currentStatePosition ?: 0L
        if (_currentPlayerPosition.value != position && position > 0) {
            _currentPlayerPosition.postValue(position)
        }

        val metadata = currentPlayingEpisodeFromMediaServiceConnection.value
        val duration = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: 0L
        if (_currentEpisodeDuration.value != duration && duration > 0) {
            _currentEpisodeDuration.postValue(duration)
        }
    }

    init {
        _currentPlaybackSpeed.value = sharedPreferences.getFloat(
            Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY,
            Constants.DEFAULT_PLAYBACK_SPEED
        )
        _playbackSpeedPresets.value = loadPlaybackSpeedPresets()

        mediaServiceConnection.playbackState.observeForever(playbackStateObserver)
    }

    fun refreshPlayerPlaylist() {
        if (mediaServiceConnection.mediaController == null) {
            Timber.tag("PLAY").w("⚠️ Cannot refresh playlist: mediaController is null")
            return
        }
        Timber.tag("PLAY").d("🔄 refreshPlayerPlaylist() called")
        mediaServiceConnection.unsubscribe(Constants.MEDIA_QUEUE_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
        mediaServiceConnection.subscribe(Constants.MEDIA_QUEUE_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                Timber.tag("PLAY").d("✅ Queue playlist refreshed: ${children.size} items")
            }
        })
    }

    fun updateQueueInService() {
        val controls = mediaServiceConnection.transportControls
        if (controls == null) {
            Timber.tag("PLAY").w("⚠️ Cannot update queue: transportControls is null")
            return
        }
        Timber.tag("PLAY").d("📡 MainViewModel -> sending update queue command to service")
        controls.sendCustomAction(
            Constants.MEDIA_SESSION_ACTION_UPDATE_QUEUE,
            null
        )
    }

    fun moveItemInService(from: Int, to: Int) {
        val controls = mediaServiceConnection.transportControls ?: return
        Timber.tag("PLAY").d("📡 MainViewModel -> moving item in service: $from -> $to")
        val extras = Bundle().apply {
            putInt("from_index", from)
            putInt("to_index", to)
        }
        controls.sendCustomAction(Constants.MEDIA_SESSION_ACTION_MOVE_ITEM, extras)
    }

    fun removeItemFromService(index: Int) {
        val controls = mediaServiceConnection.transportControls ?: return
        Timber.tag("PLAY").d("📡 MainViewModel -> removing item in service at index: $index")
        val extras = Bundle().apply {
            putInt("index", index)
        }
        controls.sendCustomAction(Constants.MEDIA_SESSION_ACTION_REMOVE_ITEM, extras)
    }

    fun toggleEpisodeFavorite(episode: Episode) {
        viewModelScope.launch {
            repository.changeEpisodeFavoriteStatus(episode.id)
        }
    }

    fun toggleEpisodeQueue(episode: Episode) {
        viewModelScope.launch {
            repository.changeEpisodeQueueStatus(episode.id)
        }
    }

    fun downloadEpisode(episode: Episode) {
        viewModelScope.launch {
            downloadRepository.downloadEpisodeToDatabase(episode.id).collect()
        }
    }

    fun deleteEpisode(episode: Episode) {
        viewModelScope.launch {
            try {
                downloadRepository.deleteEpisodeFromDeviceAndDatabase(episode.id)
                Timber.d("Delete successful for: ${episode.title}")
            } catch (e: Exception) {
                Timber.e(e, "Error deleting episode: ${episode.title}")
            }
        }
    }

    fun playSingleEpisode(episode: Episode) {
        playOrToggleEpisode(mediaItem = episode, toggle = false)
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

    fun skipToNextEpisode(): Unit? = mediaServiceConnection.transportControls?.skipToNext()
    fun skipToPreviousEpisode(): Unit? = mediaServiceConnection.transportControls?.skipToPrevious()
    fun seekTo(position: Long): Unit? = mediaServiceConnection.transportControls?.seekTo(position)
    fun seekForward(gap: Long = 10_000L) {
        val controls = mediaServiceConnection.transportControls ?: return
        val result = (currentPlayerPosition.value ?: 0L) + gap
        currentEpisodeDuration.value?.let { totalEpisodeTime ->
            controls.seekTo(
                if (result < totalEpisodeTime) result else totalEpisodeTime
            )
        }
    }
    fun seekReplay(gap: Long = 10_000L) {
        val controls = mediaServiceConnection.transportControls ?: return
        val result = (currentPlayerPosition.value ?: 0L) - gap
        controls.seekTo(
            if (result > 0) result else 0L
        )
    }

    fun setPlaybackSpeed(requestedSpeed: Float) {
        val clamped = requestedSpeed.normalizePlaybackSpeed()
        if (!_currentPlaybackSpeed.value.isCloseTo(clamped)) {
            _currentPlaybackSpeed.postValue(clamped)
        }
        mediaServiceConnection.setPlaybackSpeed(clamped)
        sharedPreferences.edit()
            .putFloat(Constants.SHARED_PREFERENCE_PLAYBACK_SPEED_KEY, clamped)
            .apply()
    }

    fun adjustPlaybackSpeed(delta: Float) {
        val current = _currentPlaybackSpeed.value ?: Constants.DEFAULT_PLAYBACK_SPEED
        val target = current + delta
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

    fun formatSpeed(speed: Float): String = String.format(Locale.US, "%.2f", speed)
        .replace("x", "")
        .replace("X", "")

    fun playOrToggleEpisode(mediaItem: Episode, toggle: Boolean = false) {
        val controls = mediaServiceConnection.transportControls
        if (controls == null) {
            Timber.tag("PLAY").w("⚠️ Cannot play/toggle: transportControls is null")
            return
        }

        val isPrepared = playbackState.value?.isPrepared ?: false
        val currentMediaId = currentPlayingEpisodeFromMediaServiceConnection.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

        if (isPrepared && mediaItem.mediaId == currentMediaId) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {
                        if (toggle) controls.pause()
                    }
                    playbackState.isPlayEnabled -> {
                        controls.play()
                    }
                    else -> Unit
                }
            }
        } else {
            val currentPosition = playbackState.value?.currentStatePosition ?: 0L
            val currentEpisodeId = currentPlayingEpisodeFromMediaServiceConnection.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toIntOrNull()
            if (currentEpisodeId != null && currentPosition > 0 && currentEpisodeId != mediaItem.id) {
                viewModelScope.launch {
                    try {
                        repository.updateEpisodePosition(currentEpisodeId, currentPosition)
                        sharedPreferences.edit()
                            .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, currentEpisodeId.toString())
                            .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, currentPosition)
                            .apply()
                    } catch (e: Exception) {
                        Timber.e(e, "Error saving position before episode switch")
                    }
                }
            }
            controls.playFromMediaId(mediaItem.id.toString(), null)
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

    fun navigateToDetailWithSharedPref(episodeId: Int) {
        sharedPreferences.edit()
            .putInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, episodeId)
            .apply()
    }

}
