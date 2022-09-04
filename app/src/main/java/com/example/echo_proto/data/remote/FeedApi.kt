package com.example.echo_proto.data.remote

import com.example.echo_proto.util.Constants
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import javax.inject.Inject

class FeedApi @Inject constructor(private val parser: Parser) {

    suspend fun getFullChannelsFeed(): Channel = parser.getChannel(Constants.URL)

    suspend fun getChannelFeed(url: String): Channel = parser.getChannel(url)
}

