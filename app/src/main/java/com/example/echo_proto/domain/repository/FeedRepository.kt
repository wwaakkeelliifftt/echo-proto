package com.example.echo_proto.domain.repository

import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.util.Resource
import com.prof.rssparser.Channel
import kotlinx.coroutines.flow.Flow
import java.text.FieldPosition

interface FeedRepository {

    fun getInitialFeedRssFromDatabase(): Flow<Resource<List<Episode>>>

    fun updateFeedRss(): Flow<Resource<List<Episode>>>

    fun updateQueueRss(): Flow<Resource<List<Episode>>>

    fun searchByQuery(string: String): Flow<Resource<List<Episode>>>

    suspend fun changeEpisodeQueueStatus(id: Int)

    suspend fun changeEpisodeQueueIndex(id: Int, newPositionIndex: Int)
}
