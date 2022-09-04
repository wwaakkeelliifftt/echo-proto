package com.example.echo_proto.domain.repository

import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChannelsRepository {

    fun getRssChannelFromDatabase(channel: FeedChannel): Flow<Resource<List<Episode>>>

    fun updateChannelRss(channel: FeedChannel): Flow<Resource<List<Episode>>>

    fun searchByQuery(query: String): Flow<Resource<List<Episode>>>

    suspend fun changeEpisodeQueueStatus(id: Int, position: Int) // ??

    suspend fun changeEpisodeQueueIndex(id: Int, newPositionIndex: Int) // ??

}