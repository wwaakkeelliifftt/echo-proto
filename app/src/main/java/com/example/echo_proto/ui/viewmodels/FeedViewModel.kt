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
import kotlinx.coroutines.Job
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

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var searchJob: Job? = null

    fun updateFeedRss(): Boolean {
        viewModelScope.launch {
            repository.updateFeedRss().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _isLoading.postValue(true)
                    is Resource.Success -> {
                        _rssFeed.postValue(resource.data ?: emptyList())
                        _isLoading.postValue(false)
                    }
                    is Resource.Error -> {
                        _snackbarMessage.postValue(resource.message.toString())
                        _isLoading.postValue(false)
                    }
                }
            }
        }
        return false
    }

    fun getRssFeedFromDatabase() {
        viewModelScope.launch {
            repository.getRssFeedFromDatabase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _isLoading.postValue(true)
                    is Resource.Success -> {
                        _rssFeed.postValue(resource.data ?: emptyList())
                        _isLoading.postValue(false)
                    }
                    is Resource.Error -> {
                        _isLoading.postValue(false)
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

    fun refreshRssFeedPersonal() {
        val filterSet = sharedPreferences.getStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, emptySet()) ?: emptySet()
        _filterStringsSet.value = filterSet
        
        if (filterSet.isEmpty()) {
            _rssFeedPersonal.postValue(emptyList())
            return
        }

        viewModelScope.launch {
            val filterMass = mutableListOf<Episode>()
            for (query in filterSet) {
                repository.searchByQuery(string = query).collect { result ->
                    if (result is Resource.Success) {
                        filterMass += result.data ?: emptyList()
                    }
                }
            }
            val result = filterMass.distinctBy { it.id }.sortedByDescending { it.timestamp }
            _rssFeedPersonal.postValue(result)
        }
    }

    init {
        getRssFeedFromDatabase()
    }

    fun initDatabaseMessageSuccess() = _isDatabaseEmptyDialog.postValue(false)

    fun changeEpisodeInQueueStatus(position: Int, source: LiveData<List<Episode>>) {
        viewModelScope.launch {
            source.value?.getOrNull(position)?.let { episode ->
                repository.changeEpisodeQueueStatus(id = episode.id)
            }
        }
    }

    fun searchByQuery(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            repository.searchByQuery(query).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Timber.d("QUERY LOADING ->> $query")
                    is Resource.Success -> {
                        _rssFeed.postValue(resource.data ?: emptyList())
                    }
                    is Resource.Error -> {
                        if (resource.message == Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY) {
                             _rssFeed.postValue(emptyList())
                        }
                        _snackbarMessage.postValue(resource.message.toString())
                    }
                }
            }
        }
    }

    fun selectEpisodeField(position: Int): Int {
        val currentList = _rssFeed.value?.toMutableList() ?: return 0
        currentList.getOrNull(position)?.let { episode ->
            val newEpisodeState = episode.copy(isSelected = !episode.isSelected)
            currentList[position] = newEpisodeState
            _rssFeed.postValue(currentList)
            return currentList.count { it.isSelected }
        }
        return getSelectedEpisodesCount()
    }

    fun getSelectedEpisodesCount(): Int {
        return _rssFeed.value?.count { it.isSelected } ?: 0
    }

    fun unselectAllFields() {
        val currentList = _rssFeed.value ?: return
        val newList = currentList.map { it.copy(isSelected = false) }
        _rssFeed.postValue(newList)
    }

    fun addSelectedEpisodesToQueue() {
        viewModelScope.launch {
            val toQueueList = _rssFeed.value?.filter { it.isSelected } ?: emptyList()
            toQueueList.forEach { episode ->
                repository.changeEpisodeQueueStatus(episode.id)
            }
            getRssFeedFromDatabase()
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
