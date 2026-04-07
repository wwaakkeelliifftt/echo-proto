package com.example.echo_proto.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_proto.data.local.prefs.EpisodeDisplayOptions
import com.example.echo_proto.data.local.prefs.SettingsManager
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val settingsManager: SettingsManager
): ViewModel() {

    private val _rssDownloads = MutableLiveData<List<Episode>>()
    val rssDownloads: LiveData<List<Episode>> get() = _rssDownloads

    // Display options for Downloads screen
    val displayOptions: StateFlow<EpisodeDisplayOptions> = settingsManager.getOptionsFlow("downloads")

    init {
        updateDownloadsRss()
    }

    fun updateDownloadsRss() {
        viewModelScope.launch {
            repository.getRssDownloadsFromDatabase().collect { resource ->
                when (resource) {
                    is Resource.Success -> resource.data.let { _rssDownloads.postValue(it) }
                    is Resource.Loading -> { }
                    is Resource.Error -> { }
                }
            }
        }
    }
}
