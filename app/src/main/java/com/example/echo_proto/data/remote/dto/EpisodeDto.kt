package com.example.echo_proto.data.remote.dto

import com.example.echo_proto.data.local.entity.EpisodeEntity
import java.time.Duration

data class EpisodeDto(
   val title: String,
   val channel: String,
   val timestamp: String,
   val description: String,
   val audioLink: String,
   val videoLink: String,
   val duration: String
) {
   fun toEpisodeEntity(): EpisodeEntity =
      EpisodeEntity(
         title = title,
         channel = channel,
         timestamp = timestamp,
         description = description,
         audioLink = audioLink,
         videoLink = videoLink,
         duration = duration
      )
}
