package com.example.echo_proto.domain.repository

import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.util.Resource
import com.prof.rssparser.Channel
import kotlinx.coroutines.flow.Flow

interface FeedRepository {

    fun getRssFeed(): Flow<Resource<List<Episode>>>

    fun updateRssFeed(): Flow<Resource<List<Episode>>>
}
