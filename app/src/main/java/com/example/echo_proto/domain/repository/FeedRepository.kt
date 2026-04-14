package com.example.echo_proto.domain.repository

import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.util.Resource
import com.prof.rssparser.Channel
import kotlinx.coroutines.flow.Flow
import java.text.FieldPosition

interface FeedRepository {

    fun getEpisodeById(id: Int): Flow<Resource<Episode>>

    fun getRssFeedFromDatabase(): Flow<Resource<List<Episode>>>

    fun updateFeedRss(): Flow<Resource<List<Episode>>>

    fun getRssQueueFromDatabase(): Flow<Resource<List<Episode>>>

    fun getRssDownloadsFromDatabase(): Flow<Resource<List<Episode>>>

    fun searchByQuery(string: String): Flow<Resource<List<Episode>>>

    suspend fun changeEpisodeQueueStatus(id: Int)

    suspend fun changeEpisodeFavoriteStatus(id: Int)

    suspend fun changeEpisodeQueueIndex(id: Int, newPositionIndex: Int)

    suspend fun updateQueueOrder(episodeIds: List<Int>)

    suspend fun updateEpisodePosition(episodeId: Int, position: Long)

    fun getRssChannelFromDatabase(channel: FeedChannel): Flow<Resource<List<Episode>>>

    fun updateChannelRss(channel: FeedChannel): Flow<Resource<List<Episode>>>

}
