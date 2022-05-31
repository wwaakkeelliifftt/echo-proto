package com.example.echo_proto.data.repository

import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.data.remote.dto.EpisodeDto
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val db: FeedDatabase,
    private val parser: Parser
): FeedRepository {

    private val dao = db.dao

    override fun getRssFeed(): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())

        val episodesList = dao.getAllFeed().map { it.toEpisode() }
        Timber.d("timber after DAO -> $episodesList")

        if (episodesList.isNotEmpty()) {
            Timber.d("IS NOT EMPTY ${episodesList.first()}")
            emit(Resource.Success(data = episodesList))
        } else {
            Timber.d("IS EMPTY")
            updateRssFeed()
        }
    }

    override fun updateRssFeed(): Flow<Resource<List<Episode>>> = flow {
        emit(Resource.Loading())
        Timber.d("GO updateRssFeed")
        try {
            val channel = parser.getChannel(Constants.URL)
            val episodesList = mapChannelToEpisodesList(channel)

            Timber.d("Feed UPDATE SUCCESS \n ${channel.articles.first().title}")
            emit(Resource.Success(data = episodesList))

        } catch (e: Exception) {
            Timber.d("Feed result ERROR \n ${e.message}")
            emit(Resource.Error())
        }
    }

    private suspend fun mapChannelToEpisodesList(channel: Channel): List<Episode> {
        if (channel.articles.isEmpty()) {
            Timber.d("EMPTY_LIST")
            return emptyList()
        }
        Timber.d("NO_EMPTY_LIST")
        val remoteEpisodesList = channel.articles.map { item ->
            EpisodeDto(
                title = item.title ?: "no_data",
                channel = item.guid ?: "",
                timestamp = item.pubDate ?: "",
                description = item.description ?: "",
                audioLink = item.audio ?: "",
                videoLink = item.link ?: "",
                duration = item.itunesArticleData?.duration ?: "0"
            )
        }
        dao.insertEpisode(remoteEpisodesList.map { it.toEpisodeEntity() })
        return dao.getAllFeed().map { it.toEpisode() }
    }

}

