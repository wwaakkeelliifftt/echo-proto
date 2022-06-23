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
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository
): ViewModel() {

    private val _rssFeed = MutableLiveData(listOf<Episode>())
    val rssFeed: LiveData<List<Episode>> get() = _rssFeed

    private val _snackbarMessage = MutableLiveData("")
    val snackbarMessage: LiveData<String> get() =  _snackbarMessage

    private val _isDatabaseEmptyDialog = MutableLiveData(false)
    val isDatabaseEmptyDialog: LiveData<Boolean> get() = _isDatabaseEmptyDialog

    // todo: circle progress indicator
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun updateFeedRss(): Boolean {
        viewModelScope.launch {
            val result = repository.updateFeedRss()
            result.collect { resource ->
                when (resource) {
                    is Resource.Loading -> Timber.d("..RSS FEED is LOADING with UPDATE")
                    is Resource.Success -> _rssFeed.postValue(resource.data)
                    is Resource.Error -> Timber.d("ERROR OCCUR at UPDATE")
                }
            }
        }
        return false
    }

    private fun getInitialFeedRssFromDb() {
        viewModelScope.launch {
            val result = repository.getInitialFeedRssFromDatabase()
            result.collect { resource ->
                when (resource) {
                    is Resource.Loading -> Timber.d("LOADING RSS from DATABASE with init load")
                    is Resource.Success -> _rssFeed.postValue(resource.data)
                    is Resource.Error -> {
                        if (resource.message == Constants.DATABASE_EMPTY_MESSAGE && _isDatabaseEmptyDialog.value == false) {
                            _isDatabaseEmptyDialog.postValue(true)
                            return@collect
                        }
                        _snackbarMessage.postValue(resource.message.toString())
                    }
                }
            }
        }
    }

    init {
        getInitialFeedRssFromDb()
    }

    fun initDatabaseMessageSuccess() = _isDatabaseEmptyDialog.postValue(false)

    // for individual context menu
    fun changeEpisodeInQueueStatus(position: Int, source: LiveData<List<Episode>>) {
        viewModelScope.launch {
            val episode = source.value?.get(position)
            repository.changeEpisodeQueueStatus(episode!!.id)
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
                        _rssFeed.postValue(result)
                    }
                    is Resource.Error -> {
                        Timber.d("QUERY ERROR ->> $query \n\n ${Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY}")
                        _rssFeed.postValue(resource.data)
                        delay(2222L)
                        // probably doesn't need error message, because we get "error" at every new letter in query
                        _snackbarMessage.postValue(Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY)
                    }
                }
            }
        }
    }

    fun selectEpisodeField(position: Int) {
        _rssFeed.value?.get(position).let { episode ->
            val newEpisodeState = episode!!.copy(isSelected = !episode.isSelected)
            val newFeedList = _rssFeed.value as MutableList     // todo: check this approach for correct/ok ???
            Timber.d("${newFeedList.hashCode()}  ===  ${_rssFeed.value.hashCode()}")
            newFeedList[position] = newEpisodeState
            _rssFeed.postValue(newFeedList)
        }
    }

    fun unselectAllFields() {
        val newFeedList = mutableListOf<Episode>()
        _rssFeed.value?.forEach { episode ->
            episode.isSelected = false
            newFeedList.add(episode)
        }
        _rssFeed.postValue(newFeedList)
        Timber.d("-----UNSELECTED-----")
    }

    fun addSelectedEpisodesToQueue() {
        viewModelScope.launch {
            val toQueueList = _rssFeed.value?.filter { it.isSelected } ?: emptyList()
            toQueueList.forEach { episode ->
                repository.changeEpisodeQueueStatus(episode.id)
            }
        }
    }


}
