package com.example.echo_proto.data.repository

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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val db: FeedDatabase,
    private val api: FeedApi
): FeedRepository {

    override fun getEpisodeById(id: Int) = flow<Resource<Episode>> {
        emit(Resource.Loading())
        try {
//            val episode = db.dao.getEpisodeById(id = id).toEpisode()
            db.dao.getFlowEpisodeById(id = id).collect { episodeEntity ->
                emit(Resource.Success(data = episodeEntity.toEpisode()))
            }
//            emit(Resource.Success(data = episode))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message))
        }
    }

    // todo: make check with dialog message for bad xml code period on server
    private suspend fun insertApiResponseToDatabase(channel: Channel): Boolean {
        if (channel.articles.isEmpty()) {
            Timber.d("-------------->>>>>>>>>>>>>>>PROBLEM<<<<<<<<<<<-----ParserHasBadResponse")
            return true
        }
        val remoteEpisodesList = channel.articles.map { item ->
            EpisodeDto(
                title = item.title ?: Constants.NO_DATA,
                rssId = item.guid ?: "",
                timestamp = item.pubDate.getTimeInMillisFromString(),
                description = item.description ?: "",
                audioLink = item.audio ?: "",
                videoLink = item.link ?: "",
                duration = item.itunesArticleData?.duration ?: "0"
            )
        }
        remoteEpisodesList.forEach {
            Timber.d("EpisodeDTO: channel=${it.rssId}, title=${it.title}, video=${it.videoLink}")
        }

        val episodesInDatabase = db.dao.getAllFeed()
        val localEpisodesTitleSet = episodesInDatabase.map { it.title }.toHashSet()
        val localEpisodeCrossLinkSet = episodesInDatabase.map { it.rssId }.toHashSet()

        val newEpisodes = remoteEpisodesList
            .filterNot { localEpisodesTitleSet.contains(it.title) }
            .filterNot { localEpisodeCrossLinkSet.contains(it.rssId) }
            .filterNot { it.title == Constants.NO_DATA }

        db.dao.insertEpisodesList(newEpisodes.map { it.toEpisodeEntity() })
        return false
    }


    override fun getRssFeedFromDatabase(): Flow<Resource<List<Episode>>> = flow {
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
            val channelFullFeed = api.getFullChannelsFeed()
            val emptyListFlag = insertApiResponseToDatabase(channelFullFeed)
            if (emptyListFlag) {
                emit(Resource.Error(data = emptyList(), message = Constants.EMPTY_SERVER_RESPONSE))
            } else {
                val episodesList = db.dao.getAllFeed().map { it.toEpisode() }
                emit(Resource.Success(data = episodesList))
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = Constants.NETWORK_ERROR))
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

    override fun getRssQueueFromDatabase(): Flow<Resource<List<Episode>>> = flow {
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

    // todo: need fix with for-loop validate queue - because true order go shuffle ---- USE POSITION
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
        Timber.d("DB_DAO::EE.copy(newIndex=$newPositionIndex, title=${episode.title}")
        db.dao.insertEpisode(episodeWithNewIndex)
    }

    override fun getRssChannelFromDatabase(channel: FeedChannel): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            val result = db.dao.getChannelFeed(channelName = channel.name).map { it.toEpisode() }
            if (result.isNotEmpty()) {
                emit(Resource.Success(data = result))
            } else if (result.isNullOrEmpty()) {
                Timber.d("---------->>>>>>>>>>>>>>>> EMPTY DATABASE FOR CHANNEL=${channel.name}")
                emit(Resource.Error(message = Constants.DATABASE_EMPTY_MESSAGE, data = emptyList()))
                return@flow
            }
        } catch (e: Exception) {
            emit(Resource.Error(message = Constants.DATABASE_ERROR_MESSAGE))
        }
    }

    override fun updateChannelRss(channel: FeedChannel): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        try {
            val channelRss = api.getChannelFeed(channel.url)
            val emptyListFlag = insertApiResponseToDatabase(channelRss)
            if (emptyListFlag) {
                emit(Resource.Error(data = emptyList(), message = Constants.EMPTY_SERVER_RESPONSE))
            } else {
                val episodesList = db.dao.getChannelFeed(channelName = channel.name).map { it.toEpisode() }
                emit(Resource.Success(data = episodesList))
            }
        } catch (e: Exception) {
            emit(Resource.Error(data = emptyList(), message = Constants.NETWORK_ERROR))
        }
    }



}

