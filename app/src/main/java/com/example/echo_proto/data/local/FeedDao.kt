package com.example.echo_proto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.echo_proto.data.local.entity.EpisodeEntity

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM episodes_table")
    suspend fun getAllFeed(): List<EpisodeEntity>

}