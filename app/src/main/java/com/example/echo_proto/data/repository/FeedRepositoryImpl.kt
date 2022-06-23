package com.example.echo_proto.data.repository

import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.data.remote.FeedApi
import com.example.echo_proto.data.remote.dto.EpisodeDto
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.getTimeInMillisFromString
import com.prof.rssparser.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val db: FeedDatabase,
    private val api: FeedApi
): FeedRepository {


    override fun getInitialFeedRssFromDatabase(): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            val episodeList = db.dao.getAllFeed().map { it.toEpisode() }
            if (episodeList.isNullOrEmpty()) {
                emit(Resource.Error(message = Constants.DATABASE_EMPTY_MESSAGE))
                return@flow
            }
            emit(Resource.Success(data = episodeList))
        } catch (e: Exception) {
            emit(Resource.Error(message = Constants.DATABASE_ERROR_MESSAGE))
        }
    }

    override fun updateFeedRss(): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            // todo: NEED MAKE CHECK for NETWORK CONNECTION
            val channel = api.getChannelsFeed()
            val episodesList = mapChannelToEpisodesList(channel)
            emit(Resource.Success(data = episodesList))
        } catch (e: Exception) {
            emit(Resource.Error(message = Constants.NETWORK_STATE_PROBLEM))
        }
    }

    private suspend fun mapChannelToEpisodesList(channel: Channel): List<Episode> {
        if (channel.articles.isEmpty()) {
            return emptyList()
        }
        val remoteEpisodesList = channel.articles.map { item ->
            EpisodeDto(
                title = item.title ?: Constants.NO_DATA,
                channel = item.guid ?: "",
                timestamp = item.pubDate.getTimeInMillisFromString(),
                description = item.description ?: "",
                audioLink = item.audio ?: "",
                videoLink = item.link ?: "",
                duration = item.itunesArticleData?.duration ?: "0"
            )
        }
        val localEpisodesTitleSet = db.dao.getAllFeed()
            .map { it.title }
            .toHashSet()

        val newEpisodes = remoteEpisodesList
            .filterNot { localEpisodesTitleSet.contains(it.title) }
            .filterNot { it.title == Constants.NO_DATA }

        db.dao.insertEpisodesList(newEpisodes.map { it.toEpisodeEntity() })
        return db.dao.getAllFeed().map { it.toEpisode() }
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

    override fun updateQueueRss(): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        val result = db.dao.getQueueFeed().map { it.toEpisode() }
        if (result.isNotEmpty()) {
            emit(Resource.Success(data = result))
            return@flow
        } else if (result.isEmpty()) {
            emit(Resource.Success(data = emptyList()))
        }
        emit((Resource.Error(data = emptyList(), message = Constants.DATABASE_QUEUE_EMPTY)))
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

    override suspend fun changeEpisodeQueueIndex(id: Int, newPositionIndex: Int) {
        val episode = db.dao.getEpisodeById(id = id)
        val episodeWithNewIndex = episode.copy(indexInQueue = newPositionIndex)
        db.dao.insertEpisode(episodeWithNewIndex)
    }
}

