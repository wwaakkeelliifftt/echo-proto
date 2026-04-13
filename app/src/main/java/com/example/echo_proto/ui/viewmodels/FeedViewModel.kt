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
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val sharedPreferences: SharedPreferences,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _rssFeed = MutableLiveData<List<Episode>>()
    val rssFeed: LiveData<List<Episode>> get() = _rssFeed

    private val _rssFeedPersonal = MutableLiveData<List<Episode>>()
    val rssFeedPersonal: LiveData<List<Episode>> get() = _rssFeedPersonal

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _filterStringsSet = MutableLiveData(emptySet<String>())
    val filterStringsSet: LiveData<Set<String>> get() = _filterStringsSet

    private val _isDatabaseEmptyDialog = MutableLiveData<Boolean>()
    val isDatabaseEmptyDialog: LiveData<Boolean> get() = _isDatabaseEmptyDialog

    // Display options flows
    val displayOptions: StateFlow<EpisodeDisplayOptions> = settingsManager.getOptionsFlow("feed")
    val displayOptionsPersonal: StateFlow<EpisodeDisplayOptions> = settingsManager.getOptionsFlow("personal")

    private var searchJob: Job? = null
    private var _searchQuery = MutableLiveData<String?>(null)
    val searchQuery: LiveData<String?> get() = _searchQuery

    init {
        getRssFeedFromDatabase()
        // Восстанавливаем фильтры из SharedPrefs
        val filterSet = sharedPreferences.getStringSet(Constants.FEED_FILTER_DIALOG_TAG, emptySet()) ?: emptySet()
        _filterStringsSet.value = filterSet
        refreshRssFeedPersonal()
    }

    fun getRssFeedFromDatabase() {
        // Если идет поиск, не перекрываем его результатами из общего потока
        if (!_searchQuery.value.isNullOrEmpty()) return

        viewModelScope.launch {
            repository.getRssFeedFromDatabase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _isLoading.postValue(true)
                    is Resource.Success -> {
                        _isLoading.postValue(false)
                        val data = resource.data ?: emptyList()
                        _rssFeed.postValue(data)
                        // 🚀 REACTION: Автоматически обновляем персональную ленту при изменениях в БД
                        refreshRssFeedPersonal(data)
                    }
                    is Resource.Error -> {
                        _isLoading.postValue(false)
                        if (resource.message == Constants.DATABASE_EMPTY_MESSAGE) {
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
            delay(300L) // Задержка для поиска
            repository.searchByQuery(query).collect { resource ->
                if (resource is Resource.Success) {
                    val data = resource.data ?: emptyList()
                    _rssFeed.postValue(data)
                    refreshRssFeedPersonal(data)
                }
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = null
        getRssFeedFromDatabase()
    }

    fun updateFeedRss() : Boolean {
        viewModelScope.launch {
            repository.updateFeedRss().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _isLoading.postValue(true)
                    is Resource.Success -> _isLoading.postValue(false)
                    is Resource.Error -> _isLoading.postValue(false)
                }
            }
        }
        return false
    }

    fun refreshRssFeedPersonal(sourceList: List<Episode>? = _rssFeed.value) {
        val filters = _filterStringsSet.value ?: emptySet()
        if (filters.isEmpty() || sourceList == null) {
            _rssFeedPersonal.postValue(sourceList ?: emptyList())
            return
        }

        val filteredList = sourceList.filter { episode ->
            filters.any { filter -> 
                episode.title.contains(filter, ignoreCase = true) || 
                episode.description.contains(filter, ignoreCase = true)
            }
        }
        _rssFeedPersonal.postValue(filteredList)
    }

    fun initDatabaseMessageSuccess() {
        _isDatabaseEmptyDialog.postValue(false)
    }

    // --- Filter Methods ---

    fun addNewFilterToRssFeedPersonalFilters(newFilter: String) {
        val currentSet = _filterStringsSet.value?.toMutableSet() ?: mutableSetOf()
        if (currentSet.add(newFilter)) {
            _filterStringsSet.value = currentSet
            saveRssFeedPersonalFiltersIntoSharedPref(currentSet)
            refreshRssFeedPersonal()
        }
    }

    fun removeFilterFromRssFeedPersonal(filter: String) {
        val currentSet = _filterStringsSet.value?.toMutableSet() ?: mutableSetOf()
        if (currentSet.remove(filter)) {
            _filterStringsSet.value = currentSet
            saveRssFeedPersonalFiltersIntoSharedPref(currentSet)
            refreshRssFeedPersonal()
        }
    }

    fun saveRssFeedPersonalFiltersIntoSharedPref(filters: Set<String>? = _filterStringsSet.value) {
        sharedPreferences.edit()
            .putStringSet(Constants.FEED_FILTER_DIALOG_TAG, filters)
            .apply()
    }

    // --- Action Mode Methods ---

    fun selectEpisodeField(position: Int) {
        val currentList = _rssFeed.value?.toMutableList() ?: return
        if (position in currentList.indices) {
            val episode = currentList[position]
            currentList[position] = episode.copy(isSelected = !episode.isSelected)
            _rssFeed.postValue(currentList)
        }
    }

    fun unselectAllFields() {
        val currentList = _rssFeed.value?.map { it.copy(isSelected = false) } ?: return
        _rssFeed.postValue(currentList)
    }

    fun addSelectedEpisodesToQueue() {
        viewModelScope.launch {
            _rssFeed.value?.filter { it.isSelected }?.forEach { episode ->
                repository.changeEpisodeQueueStatus(episode.id)
            }
            unselectAllFields()
        }
    }

    fun navigateToDetailWithSharedPref(episodeId: Int) {
        sharedPreferences.edit()
            .putInt(Constants.SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY, episodeId)
            .apply()
    }
}
