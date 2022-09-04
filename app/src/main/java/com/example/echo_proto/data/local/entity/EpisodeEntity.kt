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
    val stopListeningAt: Long = 0L
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
            duration = duration.toInt(),
            id = id ?: -1,
            isDownloaded = isDownloaded,
            downloadUrl = downloadUrl,
            isFavorite = isFavorite,
            isInQueue = isInQueue,
            indexInQueue = indexInQueue,
            hasListened = hasListened,
            stopListeningAt = stopListeningAt
        )
}

