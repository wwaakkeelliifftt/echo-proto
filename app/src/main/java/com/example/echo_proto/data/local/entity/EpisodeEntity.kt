package com.example.echo_proto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.echo_proto.domain.model.Episode

@Entity(tableName = "episodes_table")
data class EpisodeEntity(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val duration: String,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val isDownloaded: Boolean = false,
    val downloadUrl: String = "",
    val isFavorite: Boolean = false,
    val isInQueue: Boolean = false,
    val indexInQueue: Int = -1,
    val hasListened: Boolean = false,
    val stopListeningAt: Long = 0L,
    // TEMP FIX: Add channelId for proper channel filtering
    val channelId: String = "",
    // TEMP FIX: Add channel image URL
    val channelImageUrl: String = "",
    // TEMP FIX: Add episode image URL
    val episodeImageUrl: String = ""
) {
    fun toEpisode(): Episode =
        Episode(
            title = title,
            rssId = rssId,
            timestamp = timestamp,
            description = description,
            audioLink = audioLink,
            videoLink = videoLink,
            mediaId = id.toString(),            //  <-- use unique youtube link as ID
            duration = parseDurationToInt(duration),  // TEMP FIX: Parse duration from different formats
            id = id ?: -1,
            isDownloaded = isDownloaded,
            downloadUrl = downloadUrl,
            isFavorite = isFavorite,
            isInQueue = isInQueue,
            indexInQueue = indexInQueue,
            hasListened = hasListened,
            stopListeningAt = stopListeningAt,
            channelId = channelId,  // TEMP FIX: Add channelId
            channelImageUrl = channelImageUrl,  // TEMP FIX: Add channel image URL
            episodeImageUrl = episodeImageUrl  // TEMP FIX: Add episode image URL
        )
    
    /**
     * 🔧 TEMP FIX: Parse duration from different database formats
     * This method should be DELETED after database migration is complete
     * - "01:39:58" -> 6298 seconds (HH:MM:SS)
     * - "39:58" -> 2398 seconds (MM:SS)  
     * - "3958" -> 3958 seconds (seconds only)
     */
    private fun parseDurationToInt(durationStr: String): Int {
        return try {
            // Try to parse as seconds first (for new episodes)
            durationStr.toInt()
        } catch (e: NumberFormatException) {
            // Try to parse as HH:MM:SS or MM:SS (for old episodes)
            val parts = durationStr.split(":")
            when (parts.size) {
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
        } catch (e: Exception) {
            0 // Fallback
        }
    }
}

