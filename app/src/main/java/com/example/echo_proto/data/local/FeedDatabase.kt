package com.example.echo_proto.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.echo_proto.data.local.entity.EpisodeEntity

@Database(
    entities = [EpisodeEntity::class],
    version = 1
)
abstract class FeedDatabase: RoomDatabase() {
    abstract val dao: FeedDao
}