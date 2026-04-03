package com.example.echo_proto.data.remote.dto

import com.example.echo_proto.data.local.entity.EpisodeEntity

data class EpisodeDto(
   val title: String,
   val rssId: String,
   val timestamp: Long,
   val description: String,
   val audioLink: String,
   val videoLink: String,
   val duration: String,
   // 🔧 TEMP FIX: Add channelId for proper channel filtering
   val channelId: String = "",
   // 🔧 TEMP FIX: Add channel image URL
   val channelImageUrl: String = "",
   // 🔧 TEMP FIX: Add episode image URL
   val episodeImageUrl: String = ""
) {
   fun toEpisodeEntity(): EpisodeEntity =
      EpisodeEntity(
         title = title,
         rssId = rssId,
         timestamp = timestamp,
         description = description,
         audioLink = audioLink,
         videoLink = videoLink,
         duration = parseDurationToSeconds(duration),  // TEMP FIX: Parse duration to seconds here
         channelId = channelId,  // TEMP FIX: Pass channelId
         channelImageUrl = channelImageUrl,  // TEMP FIX: Pass channel image URL
         episodeImageUrl = episodeImageUrl  // TEMP FIX: Pass episode image URL
      )
      
   /**
    * 🔧 Parse duration from different RSS formats to unified seconds
    * - "01:39:58" -> 6298 seconds (HH:MM:SS)
    * - "39:58" -> 2398 seconds (MM:SS)  
    * - "3958" -> 3958 seconds (seconds only)
    */
   private fun parseDurationToSeconds(durationStr: String): String {
       return try {
           // Try to parse as seconds first
           val seconds = durationStr.toInt()
           seconds.toString()
       } catch (e: NumberFormatException) {
           // Try to parse as HH:MM:SS or MM:SS
           val parts = durationStr.split(":")
           val totalSeconds = when (parts.size) {
               3 -> { // HH:MM:SS
                   val hours = parts[0].toIntOrNull() ?: 0
                   val minutes = parts[1].toIntOrNull() ?: 0
                   val seconds = parts[2].toIntOrNull() ?: 0
                   hours * 3600 + minutes * 60 + seconds
               }
               2 -> { // MM:SS
                   val minutes = parts[0].toIntOrNull() ?: 0
                   val seconds = parts[1].toIntOrNull() ?: 0
                   minutes * 60 + seconds
               }
               else -> 0 // Invalid format
           }
           totalSeconds.toString()
       } catch (e: Exception) {
           "0" // Fallback
       }
   }
}
