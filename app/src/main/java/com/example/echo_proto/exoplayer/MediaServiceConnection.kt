package com.example.echo_proto.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Event
import com.example.echo_proto.util.Resource

class MediaServiceConnection(context: Context) {

    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> get() = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> get() = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> get() = _playbackState

    private val _currentPlayingEpisode = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingEpisode: LiveData<MediaMetadataCompat?> get() = _currentPlayingEpisode

    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(context, MediaService::class.java),
        mediaBrowserConnectionCallback,
        null
    ).apply {
        connect()
    }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) = _playbackState.postValue(state)
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) = _currentPlayingEpisode.postValue(metadata)
        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                Constants.ERROR_NETWORK -> _networkError.postValue(Event(Resource.Error(message = Constants.SNACKBAR_EVENT_NETWORK_ERROR)))
                Constants.ERROR_HTTP -> { } // todo: nado pridumat' obrabotku eventov s oshibkami
                Constants.EVENT_AUDIO_UNAVAILABLE -> {
                    val title = extras?.get("title") ?: "no-title"
                    _networkError.postValue(Event(Resource.Error(message = Constants.SNACKBAR_EVENT_NO_FILE_ON_SERVER + "\n$title")))
                }
            }
        }
        override fun onSessionDestroyed() = mediaBrowserConnectionCallback.onConnectionSuspended()
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ): MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())     // todo: try with init as object : MediaControllerCallback
            }
            _isConnected.postValue(Event(Resource.Success(data = true)))
        }
        override fun onConnectionSuspended() {
            _isConnected.postValue(
                Event(Resource.Error(message = "Connection to media was suspended", data = false))
            )
        }
        override fun onConnectionFailed() {
            _isConnected.postValue(
                Event(Resource.Error(message = "Couldn't connect to media browser", data = false))
            )
        }
    }

    fun doSomethingWithMediaService() {
        // todo: connection with service by manage playlist or something??
    }

}