package com.example.echo_proto.ui.viewmodels

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
    private val repository: FeedRepository
) :ViewModel() {

    private val _rssQueue = MutableLiveData(listOf<Episode>())
    val rssQueue: LiveData<List<Episode>> get() = _rssQueue

    private val _isLockedQueue = MutableLiveData(true)
    val isLockedQueue: LiveData<Boolean> get() = _isLockedQueue

    fun updateQueueRss(): Boolean {
        viewModelScope.launch {
            repository.updateQueueRss().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val filterResult = resource.data
                            ?.filter { it.isInQueue }
                            ?.sortedBy { it.indexInQueue }
                        _rssQueue.postValue(filterResult)
                        Timber.d("Episodes QUEUE list.size = ${filterResult?.size}")
                    }
                    is Resource.Error -> {}
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

    fun updateEpisodeIndex(itemId: Int, newIndex: Int) {
        viewModelScope.launch {
            repository.changeEpisodeQueueIndex(id = itemId, newPositionIndex = newIndex)
        }
    }
    fun changeEpisodeInQueueStatus(position: Int, source: LiveData<List<Episode>>) {
        viewModelScope.launch {
            val episode = source.value?.get(position)
            repository.changeEpisodeQueueStatus(episode!!.id)
            repository.updateQueueRss().collect { resource ->
                if (resource is Resource.Success) {
                    _rssQueue.postValue(resource.data)
                }
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
                        val result = resource.data!!
                        _rssQueue.postValue(result.filter { it.isInQueue })
                    }
                    is Resource.Error -> {
                        Timber.d("QUERY ERROR ->> $query \n\n ${Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY}")
                        _rssQueue.postValue(resource.data)
                    }
                }
            }
        }
    }

}