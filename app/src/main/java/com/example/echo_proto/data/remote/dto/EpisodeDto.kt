package com.example.echo_proto.data.remote.dto

import com.example.echo_proto.data.local.entity.EpisodeEntity

data class EpisodeDto(
   val title: String,
   val rssId: String,
   val timestamp: Long,
   val description: String,
   val audioLink: String,
   val videoLink: String,
   val duration: String
) {
   fun toEpisodeEntity(): EpisodeEntity =
      EpisodeEntity(
         title = title,
         rssId = rssId,
         timestamp = timestamp,
         description = description,
         audioLink = audioLink,
         videoLink = videoLink,
         duration = duration
      )
}
