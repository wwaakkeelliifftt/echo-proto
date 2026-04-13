package com.example.echo_proto.data.repository

import androidx.room.withTransaction
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.data.remote.FeedApi
import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.data.remote.dto.EpisodeDto
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.getTimeInMillisFromString
import com.prof.rssparser.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val db: FeedDatabase,
    private val api: FeedApi
): FeedRepository {

    override fun getEpisodeById(id: Int): Flow<Resource<Episode>> = flow {
        emit(Resource.Loading())
        try {
            val episode = db.dao.getEpisodeById(id = id).toEpisode()
            emit(Resource.Success(data = episode))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message))
        }
    }

    private suspend fun insertApiResponseToDatabase(channel: Channel, forcedChannelId: String? = null): Boolean {
        if (channel.articles.isEmpty()) {
            Timber.d("---------->>>>>>>>>>>>>>>> 🚨 RSS_PARSE: Empty articles list from RSS feed")
            return true
        }
        
        // 1. Используем channel.title (для RSSParser 4.0.2)
        val finalChannelId = forcedChannelId ?: channel.title ?: "Unknown Channel"
        
        Timber.d("🎯 RSS_PARSE: Processing ${channel.articles.size} articles for channelId='$finalChannelId'")
        
        val remoteEpisodesList = channel.articles.map { item ->
            EpisodeDto(
                title = item.title ?: Constants.NO_DATA,
                rssId = item.guid ?: "",
                timestamp = item.pubDate.getTimeInMillisFromString(),
                description = item.description ?: "",
                audioLink = item.audio ?: "",
                videoLink = item.link ?: "",
                duration = item.itunesArticleData?.duration ?: "0",
                channelId = finalChannelId,
                channelImageUrl = channel.image?.url ?: "",
                episodeImageUrl = item.image ?: ""
            )
        }

        val episodesInDatabase = db.dao.getAllFeed()
        val localEpisodesGuidSet = episodesInDatabase.map { it.rssId }.toHashSet()

        val newEpisodes = remoteEpisodesList
            .filterNot { localEpisodesGuidSet.contains(it.rssId) }
            .filterNot { it.title == Constants.NO_DATA }

        if (forcedChannelId != null) {
            val existingEpisodesToUpdate = episodesInDatabase.filter { localEpisode ->
                remoteEpisodesList.any { remote -> remote.rssId == localEpisode.rssId } && 
                localEpisode.channelId != forcedChannelId
            }
            
            if (existingEpisodesToUpdate.isNotEmpty()) {
                Timber.d("🔄 RSS_PARSE: Updating channelId for ${existingEpisodesToUpdate.size} existing episodes to '$forcedChannelId'")
                val updatedEntities = existingEpisodesToUpdate.map { it.copy(channelId = forcedChannelId) }
                db.dao.insertEpisodesList(updatedEntities)
            }
        }

        if (newEpisodes.isNotEmpty()) {
            Timber.d("🎯 RSS_PARSE: Found ${newEpisodes.size} new episodes to insert for '$finalChannelId'")
            db.dao.insertEpisodesList(newEpisodes.map { it.toEpisodeEntity() })
        }
        
        return false
    }

    // 🔧 FIXED: Now returns reactive Flow from database
    override fun getRssFeedFromDatabase(): Flow<Resource<List<Episode>>> = 
        db.dao.getAllFeedFlow().map { entities ->
            if (entities.isNullOrEmpty()) {
                Resource.Error(message = Constants.DATABASE_EMPTY_MESSAGE)
            } else {
                Resource.Success(data = entities.map { it.toEpisode() })
            }
        }

    override fun updateFeedRss(): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            val channelFullFeed = api.getFullChannelsFeed()
            val emptyListFlag = insertApiResponseToDatabase(channelFullFeed)
            if (emptyListFlag) {
                emit(Resource.Error(data = emptyList(), message = Constants.ERROR_EMPTY_SERVER_RESPONSE))
            } else {
                // При реактивном Flow нам не нужно делать повторный запрос здесь, 
                // база сама "пушнет" изменения. Но для обратной совместимости метода:
                val episodesList = db.dao.getAllFeed().map { it.toEpisode() }
                emit(Resource.Success(data = episodesList))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = Constants.ERROR_NETWORK))
        }
    }

    override fun searchByQuery(string: String): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        val result = db.dao.searchByQuery(string).map { it.toEpisode() }
        if (result.isNotEmpty()) {
            emit(Resource.Success(data = result))
            return@flow
        }
        emit(Resource.Error(data = emptyList(), message = Constants.DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY))
    }

    override fun getRssQueueFromDatabase(): Flow<Resource<List<Episode>>> = db.dao.getQueueFeedFlow()
        .distinctUntilChangedBy { entities ->
            entities.map { Triple(it.id, it.isInQueue, it.indexInQueue) }
        }
        .map { episodeEntities ->
            val result = episodeEntities.map { it.toEpisode() }
            if (result.isNotEmpty()) {
                Resource.Success(data = result)
            } else {
                Resource.Success(data = emptyList())
            }
        }

    override suspend fun changeEpisodeQueueStatus(id: Int) {
        val queueSize = db.dao.getQueueFeed().size
        val episode = db.dao.getEpisodeById(id = id)
        val episodeNewState = if (episode.isInQueue) {
            episode.copy(isInQueue = false, indexInQueue = -1)
        } else {
            episode.copy(isInQueue = true, indexInQueue = queueSize)
        }
        db.dao.insertEpisode(episodeNewState)
    }

    override suspend fun changeEpisodeFavoriteStatus(id: Int) {
        val episode = db.dao.getEpisodeById(id = id)
        val episodeNewState = episode.copy(isFavorite = !episode.isFavorite)
        db.dao.insertEpisode(episodeNewState)
    }

    override suspend fun changeEpisodeQueueIndex(id: Int, newPositionIndex: Int) {
        val episode = db.dao.getEpisodeById(id = id)
        val episodeWithNewIndex = episode.copy(indexInQueue = newPositionIndex)
        db.dao.insertEpisode(episodeWithNewIndex)
    }

    override suspend fun updateQueueOrder(episodeIds: List<Int>) {
        Timber.d("📦 DRAG: updateQueueOrder starting for ${episodeIds.size} items")
        db.withTransaction {
            episodeIds.forEachIndexed { index, id ->
                val entity = db.dao.getEpisodeById(id)
                if (entity.indexInQueue != index) {
                    db.dao.insertEpisode(entity.copy(indexInQueue = index))
                    Timber.d("📦 DRAG: Updated index for episode $id: $index")
                }
            }
        }
        Timber.d("📦 DRAG: updateQueueOrder finished")
    }

    override fun getRssChannelFromDatabase(channel: FeedChannel): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            val searchChannelId = channel.name 
            val result = db.dao.new_getChannelFeed(channelId = searchChannelId).map { it.toEpisode() }
            
            if (result.isNotEmpty()) {
                emit(Resource.Success(data = result))
            } else {
                val allEpisodes = db.dao.getAllFeed()
                val backupResult = allEpisodes.filter { 
                    it.title.contains(channel.name, ignoreCase = true)
                }.map { it.toEpisode() }
                
                if (backupResult.isNotEmpty()) {
                    emit(Resource.Success(data = backupResult))
                } else {
                    emit(Resource.Error(message = Constants.DATABASE_EMPTY_MESSAGE, data = emptyList()))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = Constants.DATABASE_ERROR_MESSAGE))
        }
    }

    override fun updateChannelRss(channel: FeedChannel): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            Timber.d("🎯 CHANNEL_UPDATE: Starting update for channel=${channel.name}")
            val channelRss = api.getChannelFeed(channel.url)
            val emptyListFlag = insertApiResponseToDatabase(channelRss, forcedChannelId = channel.name)
            
            if (emptyListFlag) {
                emit(Resource.Error(data = emptyList(), message = Constants.ERROR_EMPTY_SERVER_RESPONSE))
            } else {
                val episodesList = db.dao.new_getChannelFeed(channelId = channel.name).map { it.toEpisode() }
                emit(Resource.Success(data = episodesList))
            }
        } catch (e: Exception) {
            Timber.e("🚨 CHANNEL_UPDATE: Error for ${channel.name}: ${e.message}")
            emit(Resource.Error(data = emptyList(), message = Constants.ERROR_NETWORK))
        }
    }

    override fun getRssDownloadsFromDatabase(): Flow<Resource<List<Episode>>> = db.dao.getDownloadedEpisodes()
        .distinctUntilChangedBy { entities ->
            entities.map { it.id to it.isDownloaded }
        }
        .map { episodeEntities ->
            val result = episodeEntities.map { it.toEpisode() }
            if (result.isNotEmpty()) {
                Resource.Success(result)
            } else {
                Resource.Success(emptyList())
            }
        }
}
