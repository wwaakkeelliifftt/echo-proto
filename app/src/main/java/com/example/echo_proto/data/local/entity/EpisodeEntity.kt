package com.example.echo_proto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.echo_proto.domain.model.Episode

@Entity(tableName = "episodes_table")
data class EpisodeEntity(
    val title: String,
    val channel: String,
    val timestamp: Long,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val duration: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var isFavorite: Boolean = false,
    var isInQueue: Boolean = false,
    var indexInQueue: Int = -1,
    var hasListened: Boolean = false
) {
    fun toEpisode(): Episode =
        Episode(
            title = title,
            channel = channel,
            timestamp = timestamp,
            description = description,
            audioLink = audioLink,
            videoLink = videoLink,
            duration = duration.toInt(),
            id = id ?: -1,
            isFavorite = isFavorite,
            isInQueue = isInQueue,
            indexInQueue = indexInQueue,
            hasListened = hasListened
        )
}

