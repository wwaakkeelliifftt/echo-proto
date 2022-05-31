package com.example.echo_proto.domain.model

data class Episode(
    val title: String,
    val channel: String,
    val timestamp: String,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val duration: Int,
    val id: Int,
    val isFavorite: Boolean,
    val isInQueue: Boolean,
    val hasListened: Boolean
)