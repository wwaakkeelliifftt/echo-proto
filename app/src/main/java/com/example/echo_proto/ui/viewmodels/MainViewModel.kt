package com.example.echo_proto.ui.viewmodels

import androidx.lifecycle.*
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Resource
import com.prof.rssparser.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: FeedRepository
): ViewModel() {

    private val _rssFeed = MutableLiveData(listOf<Episode>())
    val rssFeed: LiveData<List<Episode>>
        get() = _rssFeed

    fun loadFeed() = viewModelScope.launch {
        val result = repository.getRssFeed()
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> Timber.d("..RSS FEED is Loading")
                is Resource.Success -> _rssFeed.postValue(resource.data)
                is Resource.Error -> Timber.d("ERROR OCCUR")
            }
        }
    }

    fun updateRssFeed() = viewModelScope.launch {
        val result = repository.updateRssFeed()
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> Timber.d("..RSS FEED is Loading WITH UPDATE")
                is Resource.Success -> _rssFeed.postValue(resource.data)
                is Resource.Error -> Timber.d("ERROR OCCUR at UPDATE")
            }
        }
    }

    init {
        loadFeed()
    }

}