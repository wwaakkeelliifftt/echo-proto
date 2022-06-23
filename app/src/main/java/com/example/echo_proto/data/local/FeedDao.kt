package com.example.echo_proto.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.echo_proto.data.local.entity.EpisodeEntity
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.util.Resource
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodesList(episodes: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(item: EpisodeEntity)

    @Query("SELECT * FROM episodes_table ORDER BY timestamp DESC")
    suspend fun getAllFeed(): List<EpisodeEntity>

    @Query("SELECT * FROM episodes_table " +
            "WHERE title LIKE '%' || lower(:query) || '%' " +
            "OR description LIKE '%' || lower(:query) || '%' " +
            "ORDER BY timestamp DESC")
    suspend fun searchByQuery(query: String): List<EpisodeEntity>

    @Query("SELECT * FROM episodes_table WHERE isInQueue = 1")
    suspend fun getQueueFeed(): List<EpisodeEntity>

    @Query("SELECT * FROM episodes_table WHERE id = :id")
    suspend fun getEpisodeById(id: Int): EpisodeEntity

}