package com.example.echo_proto.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EpisodeDetailViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _currentEpisode = MutableLiveData<Episode>()
    val currentEpisode: LiveData<Episode> get() = _currentEpisode

    private val _curStateFlowEpisode = MutableStateFlow<Resource<Episode>>(Resource.Loading())
    val curStateFlowEpisode: SharedFlow<Resource<Episode>> = _curStateFlowEpisode

    init {
        val initialEpisodeId = sharedPreferences.getInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, 0)
        Timber.d("initId=$initialEpisodeId")
        getSelectedEpisode(initialEpisodeId)
    }

    private fun getSelectedEpisode(id: Int) {
        viewModelScope.launch {
            repository.getEpisodeById(id = id).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _currentEpisode.postValue(result.data!!)
                        _curStateFlowEpisode.value = result
                    }
                    else -> Unit
                }
            }
        }
    }

    fun changeEpisodeQueueStatus() {
        val episode = currentEpisode.value
        if (episode != null) {
            viewModelScope.launch {
                repository.changeEpisodeQueueStatus(episode.id)
            }
            getSelectedEpisode(episode.id)
        }
    }


}

