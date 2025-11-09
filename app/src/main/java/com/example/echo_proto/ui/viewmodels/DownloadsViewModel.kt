package com.example.echo_proto.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: FeedRepository
): ViewModel() {

    private val _rssDownloads = MutableLiveData<List<Episode>>()
    val rssDownloads: LiveData<List<Episode>> get() = _rssDownloads

    init {
        updateDownloadsRss()
    }

    fun updateDownloadsRss() {
        viewModelScope.launch {
            repository.getRssDownloadsFromDatabase().collect { resource ->
                Timber.d("DownloadsViewModel::updateDownloadsRss::collect -> ${resource.data}")
                when (resource) {
                    is Resource.Success -> resource.data.let { _rssDownloads.postValue(it)
                        Timber.d("DownloadsViewModel::Resource.Success count ${resource.data?.size}")
                    }
                    is Resource.Loading -> { Timber.d("DownloadsViewModel::Resource.Loading") }
                    is Resource.Error -> { Timber.d("DownloadsViewModel::Resource.Error") }
                }
            }
        }
    }
}