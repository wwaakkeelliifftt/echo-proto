package com.example.echo_proto.domain.model

import com.example.echo_proto.data.local.entity.EpisodeEntity

data class Episode(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val duration: Int,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val mediaId: String,    // same link to youtube?
    val id: Int,
    val isDownloaded: Boolean = false,
    val downloadUrl: String ,
    val isFavorite: Boolean = false,
    val isInQueue: Boolean = false,
    val indexInQueue: Int,
    val hasListened: Boolean = false,
    var isSelected: Boolean = false,
    val stopListeningAt: Long = 0L
) {
    fun toEpisodeEntity(): EpisodeEntity {
        return EpisodeEntity(
            title = title,
            rssId = rssId,
            timestamp = timestamp,
            duration = duration.toString(),
            description = description,
            audioLink = audioLink,
            videoLink = videoLink,
            id = id,
            isDownloaded = isDownloaded,
            downloadUrl = downloadUrl,
            isFavorite = isFavorite,
            isInQueue = isInQueue,
            indexInQueue = indexInQueue,
            hasListened = hasListened,
            stopListeningAt = stopListeningAt
        )
    }
}