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
    val rssFeed: LiveData<List<Episode>> =  _rssFeed

    private val _snackbarMessage = MutableLiveData("")
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _isDatabaseEmptyDialog = MutableLiveData(false)
    val isDatabaseEmptyDialog: LiveData<Boolean> get() = _isDatabaseEmptyDialog

    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> get() = _searchQuery

    private val _isFeedLock = MutableLiveData(false)
    val isFeedLock: LiveData<Boolean> get() = _isFeedLock

    fun getFeed(): Boolean {
        viewModelScope.launch {
            val result = repository.updateRssFeed()
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

    private fun getInitialFeedFromDatabase() {
        viewModelScope.launch {
            val result = repository.getInitialFeedFromDatabase()
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
        getInitialFeedFromDatabase()
    }

    fun initDatabaseMessageSuccess() = _isDatabaseEmptyDialog.postValue(false)

    fun feedLockerUpdate() {
        val revert = isFeedLock.value?.let { !it }
        _isFeedLock.postValue(revert)
    }

    fun updateSearchByQuery(query: String) {
        viewModelScope.launch {
            delay(666L)
            val result = repository.searchByQuery(query)
            result.collect { resource ->
                when (resource) {
                    is Resource.Loading -> Timber.d("QUERY LOADING ->> $query")
                    is Resource.Success -> {
                        Timber.d("QUERY SUCCESS ->> $query")
                        _rssFeed.postValue(resource.data)
                        _searchQuery.postValue(query)
                    }
                    is Resource.Error -> {
                        Timber.d("QUERY ERROR ->> $query")
                        _rssFeed.postValue(emptyList())
                        delay(5000L)
                        // probably doesn't need error message, because we get "error" at every new letter in query
                        _snackbarMessage.postValue(Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY)
                    }
                }
            }
        }
    }


}
