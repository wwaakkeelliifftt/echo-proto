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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QueueViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences
) :ViewModel() {

    private val _rssQueue = MutableLiveData(listOf<Episode>())
    val rssQueue: LiveData<List<Episode>> get() = _rssQueue

    private val _queueDurationSeconds = MutableLiveData(0)
    val queueDurationSeconds: LiveData<Int> get() = _queueDurationSeconds

    private val _queueCount = MutableLiveData(0)
    val queueCount: LiveData<Int> get() = _queueCount

    private val _isLockedQueue = MutableLiveData(true)
    val isLockedQueue: LiveData<Boolean> get() = _isLockedQueue

    fun updateQueueRss(): Boolean {
        viewModelScope.launch {
            repository.getRssQueueFromDatabase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        val sortedResult = resource.data
                            ?.filter { it.isInQueue }
                            ?.sortedBy { it.indexInQueue } ?: emptyList()
                        _rssQueue.postValue(sortedResult)
                        updateQueueStats(sortedResult)
                        Timber.d("Episodes QUEUE list.size = ${sortedResult.size}")
                        // Когда очередь обновляется, MediaSource автоматически обновится через Flow
                    }
                    is Resource.Error -> {
                        Timber.e("Error loading queue: ${resource.message}")
                        val fallback = resource.data?.filter { it.isInQueue } ?: emptyList()
                        _rssQueue.postValue(fallback)
                        updateQueueStats(fallback)
                    }
                }
            }
        }
        return false
    }

    init {
        updateQueueRss()
    }

    fun updateQueueLocker() {
        val revert = isLockedQueue.value?.let { !it }
        _isLockedQueue.postValue(revert)
    }

    fun updateEpisodeIndex(episodeId: Int, newIndex: Int) {
        viewModelScope.launch {
            repository.changeEpisodeQueueIndex(id = episodeId, newPositionIndex = newIndex)
        }
    }

    fun changeEpisodeInQueueStatus(position: Int, source: LiveData<List<Episode>>) {
        viewModelScope.launch {
            val episode = source.value?.get(position)
            if (episode != null) {
                repository.changeEpisodeQueueStatus(id = episode.id)
                // Очередь обновится автоматически через Flow в updateQueueRss()
            }
        }
    }

    fun searchByQuery(query: String) {
        viewModelScope.launch {
            delay(666L)
            repository.searchByQuery(query).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Timber.d("QUERY LOADING ->> $query")
                    is Resource.Success -> {
                        val result = resource.data!!.filter { it.isInQueue }
                        _rssQueue.postValue(result)
                        updateQueueStats(result)
                    }
                    is Resource.Error -> {
                        Timber.d("QUERY ERROR ->> $query \n\n ${Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY}")
                        val fallback = resource.data?.filter { it.isInQueue } ?: emptyList()
                        _rssQueue.postValue(fallback)
                        updateQueueStats(fallback)
                    }
                }
            }
        }
    }

    fun navigateToDetailWithSharedPref(episodeId: Int) {
        sharedPreferences.edit()
            .putInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, episodeId)
            .apply()
    }

    private fun updateQueueStats(episodes: List<Episode>) {
        _queueCount.postValue(episodes.size)
        _queueDurationSeconds.postValue(episodes.sumOf { it.duration })
    }

}