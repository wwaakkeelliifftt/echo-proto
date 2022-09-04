package com.example.echo_proto.data.repository

import com.example.echo_proto.data.local.FeedDao
import com.example.echo_proto.data.remote.FeedApi
import com.example.echo_proto.domain.repository.ChannelsRepository
import javax.inject.Inject

class ChannelsRepositoryImpl @Inject constructor(
    private val api: FeedApi,
    private val dao: FeedDao
)