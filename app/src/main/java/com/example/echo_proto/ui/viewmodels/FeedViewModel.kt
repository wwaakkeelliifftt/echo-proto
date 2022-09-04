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
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences
): ViewModel() {

    private val _rssFeed = MutableLiveData(listOf<Episode>())
    val rssFeed: LiveData<List<Episode>> get() = _rssFeed

    private val _rssFeedPersonal = MutableLiveData(listOf<Episode>())
    val rssFeedPersonal: LiveData<List<Episode>> get() = _rssFeedPersonal

    private val _filterStringsSet = MutableLiveData(emptySet<String>())
    val filterStringsSet: LiveData<Set<String>> get() = _filterStringsSet

    private val _notifyAdapterUpdateFlag = MutableLiveData(false)
    val notifyAdapterUpdateFlag: LiveData<Boolean> get() = _notifyAdapterUpdateFlag

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

    fun getRssFeedFromDatabase() {
        viewModelScope.launch {
            val result = repository.getRssFeedFromDatabase()
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

    // TODO: EXTRA CHANGE
    fun refreshRssFeedPersonal_MODIFY() {
        _filterStringsSet.value = sharedPreferences.getStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, emptySet())
        if (filterStringsSet.value.isNullOrEmpty()) {
            Timber.d("SHARED_PREFS ARE EMPTY -- RETURN affected")
            _rssFeedPersonal.postValue(emptyList())
            return
        }
        viewModelScope.launch {
            val filterListResult = mutableListOf<Episode>()
            for (episode in rssFeed.value!!) {
                var statusPass = false
                Timber.d("PRELOAD EPISODE:: ${episode.title}")
                filterStringsSet.value?.forEach { queryString ->
                    Timber.d("FOREACH : QUERY: $queryString")
                    if (episode.title.lowercase().contains(queryString.lowercase()) ||
                        episode.description.lowercase().contains(queryString.lowercase())
                    ) {
                        statusPass = true
                        filterListResult.add(episode)
                        Timber.d("statusPass=$statusPass")
                        return@forEach
                    }
                    Timber.d("statusPass=$statusPass")
                }
            }
            _rssFeedPersonal.postValue(filterListResult)
        }
    }

    fun refreshRssFeedPersonal() {
        _filterStringsSet.value = sharedPreferences.getStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, emptySet())
        if (filterStringsSet.value.isNullOrEmpty()) {
            _rssFeedPersonal.postValue(emptyList())
            return
        } else {
            viewModelScope.launch {
                val filterMass = mutableListOf<Episode>()
                for (query in filterStringsSet.value!!) {
                    repository.searchByQuery(string = query).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                filterMass += result.data
                                    ?: emptyList(); Timber.d("SEARCH_SUCCESS=$query \nsize=${result.data?.size}")
                            }
                        }
                    }
                }
                val result = filterMass.toSet().sortedByDescending { it.timestamp }
                _rssFeedPersonal.postValue(result)
            }
        }
    }



    init {
        getRssFeedFromDatabase()
    }

    fun initDatabaseMessageSuccess() = _isDatabaseEmptyDialog.postValue(false)

    // for individual context menu
    fun changeEpisodeInQueueStatus(position: Int, source: LiveData<List<Episode>>) {
        viewModelScope.launch {
            val episode = source.value?.get(position)
            repository.changeEpisodeQueueStatus(id = episode!!.id)
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
                        _rssFeedPersonal.postValue(result)
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

    // todo: make check for show that episode already in some state (queue, favorite)
    fun selectEpisodeField(position: Int) {
        rssFeed.value?.get(position).let { episode ->
            if (episode != null) {
                val newEpisodeState = episode.copy(isSelected = !episode.isSelected)
                val newFeedList = rssFeed.value as MutableList            // todo: check this approach for correct/ok ???
                Timber.d("${newFeedList.hashCode()}  ===  ${rssFeed.value.hashCode()}")
                newFeedList[position] = newEpisodeState
                _rssFeed.postValue(newFeedList)
            }
        }
    }

    fun unselectAllFields() {
        val newFeedList = mutableListOf<Episode>()
        rssFeed.value?.forEach { episode ->
            val newEpisode = episode.copy(isSelected = false)
            newFeedList.add(newEpisode)
         }
        _rssFeed.postValue(newFeedList)
    }

    fun addSelectedEpisodesToQueue() {
        viewModelScope.launch {
            val toQueueList = rssFeed.value?.filter { it.isSelected } ?: emptyList()
            toQueueList.forEachIndexed { index, episode ->
                Timber.d("CHANGE_START: select=${episode.isSelected}, title=${episode.title}")
                repository.changeEpisodeQueueStatus(episode.id)
            }
            repository.getRssFeedFromDatabase().collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _rssFeed.postValue(resource.data)
                }
            }
        }
    }

    fun navigateToDetailWithSharedPref(episodeId: Int) {
        sharedPreferences.edit()
            .putInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, episodeId)
            .apply()
    }

    fun addNewFilterToRssFeedPersonalFilters(newFilter: String) {
        val updatedFilterSet = filterStringsSet.value?.plus(newFilter) ?: setOf(newFilter)
        _filterStringsSet.postValue(updatedFilterSet)
        sharedPreferences.edit()
            .putStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, updatedFilterSet)
            .apply()
        refreshRssFeedPersonal()
    }

    fun removeFilterFromRssFeedPersonal(filter: String) {
        val updatedFilterSet = filterStringsSet.value?.minus(filter) ?: emptySet()
        _filterStringsSet.postValue(updatedFilterSet)
        sharedPreferences.edit()
            .putStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, updatedFilterSet)
            .apply()
        refreshRssFeedPersonal()
    }

    fun saveRssFeedPersonalFiltersIntoSharedPref() {
        sharedPreferences.edit()
            .putStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, filterStringsSet.value)
            .apply()
    }

    fun clearRssFeedPersonalFilters() {
        _filterStringsSet.postValue(emptySet())
        sharedPreferences.edit()
            .remove(Constants.SHARED_PREFERENCES_INIT_KEY)
            .apply()
        refreshRssFeedPersonal()
    }


}
