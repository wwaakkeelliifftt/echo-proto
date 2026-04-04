package com.example.echo_proto.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    private val _rssChannel = MutableLiveData<List<Episode>>(emptyList())
    val rssChannel: LiveData<List<Episode>> get() = _rssChannel

    fun getRssChannelFromDatabase(feedChannel: FeedChannel) {
        viewModelScope.launch {
            repository.getRssChannelFromDatabase(channel = feedChannel).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Error -> Timber.d("ERROR VIEWMODEL DATABASE WAS HAPPENED!! \n\n\n ERROR: ${resource.message}")
                    is Resource.Success -> {
                        resource.data?.let { _rssChannel.postValue(it) }
                    }
                }
            }
        }
    }

    fun updateChannelRss(feedChannel: FeedChannel): Boolean {
        viewModelScope.launch {
            repository.updateChannelRss(channel = feedChannel).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Error -> Timber.d("ERROR ON UPDATE_CHANNEL_RSS:\n\n\n ERROR: ${resource.message}")
                    is Resource.Success -> resource.data?.let { _rssChannel.postValue(it) }
                }
            }
        }
        // todo: need to replace with boolean observer
        return false
    }

    fun navigateToDetailWithSharedPref(episodeId: Int) {
        sharedPreferences.edit()
            .putInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, episodeId)
            .apply()
    }

}