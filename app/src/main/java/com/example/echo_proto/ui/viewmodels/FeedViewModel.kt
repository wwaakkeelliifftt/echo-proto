package com.example.echo_proto.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_proto.data.local.prefs.EpisodeDisplayOptions
import com.example.echo_proto.data.local.prefs.SettingsManager
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences,
    private val settingsManager: SettingsManager
): ViewModel() {

    private val _rssFeed = MutableLiveData(listOf<Episode>())
    val rssFeed: LiveData<List<Episode>> get() = _rssFeed

    private val _rssFeedPersonal = MutableLiveData(listOf<Episode>())
    val rssFeedPersonal: LiveData<List<Episode>> get() = _rssFeedPersonal

    // 🔧 RESTORED: Filter state for Personal Feed (used by FeedFilterListDialogFragment)
    private val _filterStringsSet = MutableLiveData(emptySet<String>())
    val filterStringsSet: LiveData<Set<String>> get() = _filterStringsSet

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _snackbarMessage = MutableLiveData("")
    val snackbarMessage: LiveData<String> get() =  _snackbarMessage

    private val _isDatabaseEmptyDialog = MutableLiveData(false)
    val isDatabaseEmptyDialog: LiveData<Boolean> get() = _isDatabaseEmptyDialog

    // Display options from SettingsManager
    val displayOptions: StateFlow<EpisodeDisplayOptions> = settingsManager.getOptionsFlow("feed")

    private var searchJob: Job? = null
    private var _searchQuery = MutableLiveData<String?>(null)
    val searchQuery: LiveData<String?> get() = _searchQuery

    init {
        getRssFeedFromDatabase()
        // Initialize filters from Prefs
        val filterSet = sharedPreferences.getStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, emptySet()) ?: emptySet()
        _filterStringsSet.value = filterSet
        refreshRssFeedPersonal()
    }

    fun updateDisplayOptions(options: EpisodeDisplayOptions) {
        settingsManager.saveDisplayOptions("feed", options)
    }

    fun getRssFeedFromDatabase() {
        if (!_searchQuery.value.isNullOrEmpty()) return
        
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
                        }
                    }
                }
            }
        }
    }

    fun searchByQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300L)
            repository.searchByQuery(query).collect { resource ->
                if (resource is Resource.Success) {
                    _rssFeed.postValue(resource.data ?: emptyList())
                }
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = null
        getRssFeedFromDatabase()
    }

    fun updateFeedRss(): Boolean {
        viewModelScope.launch {
            repository.updateFeedRss().collect { resource ->
                if (resource is Resource.Success) {
                    _rssFeed.postValue(resource.data ?: emptyList())
                }
            }
        }
        return false
    }

    // 🔧 RESTORED: Personal Feed Filtering Logic
    fun refreshRssFeedPersonal() {
        val filterSet = _filterStringsSet.value ?: emptySet()
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

    fun addNewFilterToRssFeedPersonalFilters(newFilter: String) {
        val updatedFilterSet = _filterStringsSet.value?.plus(newFilter) ?: setOf(newFilter)
        _filterStringsSet.value = updatedFilterSet
        refreshRssFeedPersonal()
    }

    fun removeFilterFromRssFeedPersonal(filter: String) {
        val updatedFilterSet = _filterStringsSet.value?.minus(filter) ?: emptySet()
        _filterStringsSet.value = updatedFilterSet
        refreshRssFeedPersonal()
    }

    fun saveRssFeedPersonalFiltersIntoSharedPref() {
        sharedPreferences.edit()
            .putStringSet(Constants.SHARED_PREFERENCES_INIT_KEY, _filterStringsSet.value)
            .apply()
    }

    // --- Action Mode Logic ---

    fun selectEpisodeField(position: Int) {
        val currentList = _rssFeed.value?.toMutableList() ?: return
        currentList.getOrNull(position)?.let { episode ->
            currentList[position] = episode.copy(isSelected = !episode.isSelected)
            _rssFeed.postValue(currentList)
        }
    }

    fun unselectAllFields() {
        val currentList = _rssFeed.value ?: return
        _rssFeed.postValue(currentList.map { it.copy(isSelected = false) })
    }

    fun addSelectedEpisodesToQueue() {
        viewModelScope.launch {
            _rssFeed.value?.filter { it.isSelected }?.forEach { 
                repository.changeEpisodeQueueStatus(it.id)
            }
            getRssFeedFromDatabase()
        }
    }

    fun navigateToDetailWithSharedPref(episodeId: Int) {
        sharedPreferences.edit()
            .putInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, episodeId)
            .apply()
    }

    fun initDatabaseMessageSuccess() = _isDatabaseEmptyDialog.postValue(false)
}
