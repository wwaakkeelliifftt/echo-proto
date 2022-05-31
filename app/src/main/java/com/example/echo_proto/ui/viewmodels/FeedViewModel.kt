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
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository
): ViewModel() {

    private val _rssFeed = MutableLiveData(listOf<Episode>())
    val rssFeed: LiveData<List<Episode>> = _rssFeed

    fun getFeed(): Boolean {
        viewModelScope.launch {
            val result = repository.updateRssFeed()
            result.collect { resource ->
                when (resource) {
                    is Resource.Loading -> Timber.d("..RSS FEED is Loading WITH UPDATE")
                    is Resource.Success -> _rssFeed.postValue(resource.data)
                    is Resource.Error -> Timber.d("ERROR OCCUR at UPDATE")
                }
            }
        }
        return false
    }


}
