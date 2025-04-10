package com.example.echo_proto.ui.viewmodels

import android.content.SharedPreferences
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.lifecycle.*
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.MediaServiceContentRepository
import com.example.echo_proto.exoplayer.*
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediaServiceConnection: MediaServiceConnection,
    private val repository: MediaServiceContentRepository,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    // hz..
    private val _currentFragmentViewModelState = MutableLiveData<ViewModelState>()
    val currentFragmentViewModelState: LiveData<ViewModelState> get() = _currentFragmentViewModelState
    // hz..
    private val _mediaId = MutableLiveData<String>()
    val mediaId: LiveData<String> get() = _mediaId
    // not use ??
    val episodes = liveData<List<Episode>> {
        repository.getQueueEpisodesList()
    }

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

    private val _mediaSessionEventForSnackbar = MutableLiveData<String?>()
    val mediaSessionEventForSnackbar: LiveData<String?> get() = _mediaSessionEventForSnackbar

    init {
        // ? nado li
        _mediaId.value = sharedPreferences.getString(Constants.SHARED_PREFERENCE_MEDIA_ID_KEY, Constants.MEDIA_ROOT_ID)
        _currentFragmentViewModelState.postValue(ViewModelState.Queue)

        // check when ok for launch todo: endless cycle inside. need to refactor with flow-combine
        updateCurrentPlayerPosition()

        // think, that's @_mediaItems load episodes to exoPlayer
        _mediaItems.postValue(Resource.Loading(data = null))
        mediaServiceConnection.subscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Episode(
                        title = it.description.title.toString(),
                        rssId = it.description.mediaId.toString(),
                        timestamp = it.description.extras?.getLong(MediaMetadataCompat.METADATA_KEY_YEAR) ?: 1000000L, //it.description.extras?.getString(MediaMetadataCompat.METADATA_KEY_DATE)?.toLong() ?: 0L,
                        description = it.description.description.toString(),
                        audioLink = it.description.mediaUri.toString(),
                        videoLink = it.description.mediaId.toString(),
                        duration = it.description.extras?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0,
                        id = it.description.mediaId?.toInt() ?: -1,
                        isSelected = false,
                        isFavorite = false,
                        isInQueue = false,
                        indexInQueue = -1,
                        isDownloaded = it.description.extras?.getLong(MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS) == 2L,
                        downloadUrl = "...",
                        hasListened = false,
                        mediaId = it.mediaId.toString()
                    )
                }
                // work ok without..
//                _mediaItems.postValue(Resource.Success(data = items))
            }
        })
    }

    fun refreshPlayerPlaylist() {
        mediaServiceConnection.unsubscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
        mediaServiceConnection.subscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                // todo.. mozhet voobshe ne nuzhny eti mediaItems
            }
        })
    }

    fun setViewModelState(state: ViewModelState): Unit = _currentFragmentViewModelState.postValue(state)

    fun mediaIdMapper(string: String, channelName: String = "") {
        when (string) {
            Constants.MEDIA_ROOT_ID -> {}
            Constants.MEDIA_QUEUE_ID -> {
                _mediaId.value = Constants.MEDIA_QUEUE_ID
                _currentFragmentViewModelState.postValue(ViewModelState.Queue)
            }
            Constants.MEDIA_FEED_ID -> {
                _mediaId.value = Constants.MEDIA_FEED_ID
                _currentFragmentViewModelState.postValue(ViewModelState.Feed)
            }
            Constants.MEDIA_FEED_PERSONAL_ID -> {
                _mediaId.value = Constants.MEDIA_FEED_PERSONAL_ID
                _currentFragmentViewModelState.postValue(ViewModelState.FeedPersonal)
            }
            Constants.MEDIA_CHANNEL_ID -> {
                _mediaId.value = Constants.MEDIA_CHANNEL_ID
                _currentFragmentViewModelState.postValue(ViewModelState.Channel(channelName))
            }
            Constants.MEDIA_DOWNLOADS_ID -> {
                _mediaId.value = Constants.MEDIA_DOWNLOADS_ID
                _currentFragmentViewModelState.postValue(ViewModelState.Downloads)
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

        Timber.d("\n\n____________MAIN___VIEW___MODEL___onCleared()_________\n" +
                "mediaId<String>=${currentPlayingEpisodeFromMediaServiceConnection.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)}\n" +
                "pausePosition<Long>=${playbackState.value?.currentStatePosition}"
        )
    }

    fun getCurrentPlayEpisode(id: Int) {
        viewModelScope.launch {
            repository.getEpisodeById(id = id).collect { result ->
                when (result) {
                    is Resource.Success -> _currentEpisodeFromDb.postValue(result.data!!)
                }
            }
        }
    }

    fun skipToNextEpisode(): Unit = mediaServiceConnection.transportControls.skipToNext()
    fun skipToPreviousEpisode(): Unit = mediaServiceConnection.transportControls.skipToPrevious()
    fun seekTo(position: Long): Unit = mediaServiceConnection.transportControls.seekTo(position)
    fun seekForward(gap: Long = 10_000L) {
        val result = (currentPlayerPosition.value ?: 0L) + gap
        Timber.d("FORWARD_RESULT=$result")
        currentEpisodeDuration.value?.let { totalEpisodeTime ->
            mediaServiceConnection.transportControls.seekTo(
                if (result < totalEpisodeTime) result else totalEpisodeTime
            )
        }
    }
    fun seekReplay(gap: Long = 10_000L) {
        val result = (currentPlayerPosition.value ?: 0L) - gap
        Timber.d("REPLAY_RESULT=$result")
        mediaServiceConnection.transportControls.seekTo(
            if (result > 0) result else 0L
        )
    }

    fun playOrToggleEpisode(mediaItem: Episode, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        // replace mediaItem.mediaId with unique id of episode. probably it can be @videoLink param
        Timber.d("::::::isPrepared=$isPrepared")
        Timber.d("\nPLAY-LOG::::::mediaItemId/uri=${mediaItem.id} \n title=${mediaItem.title}\n\n")
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
            Timber.d("TRY_TO_PLAY_FROM_____ID::${mediaItem.id}")
            Timber.d("TRY_TO_PLAY_FROM_____mediaId::${mediaItem.mediaId}")
            Timber.d("TRY_TO_PLAY_FROM_____audioLink::${mediaItem.audioLink}")
            mediaServiceConnection.transportControls.playFromMediaId(mediaItem.id.toString(), null)
        }
    }

    private fun updateCurrentPlayerPosition() {
//        viewModelScope.launch {
//            combine(
//                mediaServiceConnection.playbackState,
//                mediaServiceConnection.currentPlayingEpisode
//            ) { playbackState, metadata ->
//                Pair(
//                    playbackState?.currentStatePosition ?: 0L,
//                    metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: 0
//                )
//            }.collect { (position, duration) ->
//                _currentPlayerPosition.postValue(position)
//                _currentEpisodeDuration.postValue(duration)
//            }
//        }

        // old version
        viewModelScope.launch {
            while (true) {
                if (playbackState.value == null) {
                    Timber.d("\n\n---------->>>>>>> ERROR at PlaybackState==null in mainViewModel  <<<<<<<---------\n\n\n")
                }
                val position = playbackState.value?.currentStatePosition ?: 0L
                if (currentPlayerPosition.value != position) {
                    _currentPlayerPosition.postValue(position)
                }
                val duration = currentPlayingEpisodeFromMediaServiceConnection.value?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) ?: 0
                if (currentEpisodeDuration.value != duration) {
                    _currentEpisodeDuration.postValue(duration)
                }
                delay(Constants.UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        updateCurrentPlayEpisodeIdToSharedPref()
        mediaServiceConnection.unsubscribe(Constants.MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

}