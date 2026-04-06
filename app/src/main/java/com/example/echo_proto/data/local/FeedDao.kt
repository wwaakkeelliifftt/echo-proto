package com.example.echo_proto.data.local

import androidx.room.Dao
import androidx.room.Delete
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

    // 🔧 NEW: Reactive flow for the main feed
    @Query("SELECT * FROM episodes_table ORDER BY timestamp DESC")
    fun getAllFeedFlow(): Flow<List<EpisodeEntity>>

    // special list for exoplayer ??
    @Query("SELECT * FROM episodes_table WHERE isDownloaded = 1")
    fun getDownloadedEpisodes(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes_table WHERE isInQueue = 1 ORDER BY indexInQueue ASC")
    suspend fun getQueueFeed(): List<EpisodeEntity>

    @Query("SELECT * FROM episodes_table WHERE isInQueue = 1 ORDER BY indexInQueue ASC")
    fun getQueueFeedFlow(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes_table " +
            "WHERE channelId = :channelId " +
            "ORDER BY timestamp DESC")
    suspend fun new_getChannelFeed(channelId: String): List<EpisodeEntity>

    @Query("SELECT * FROM episodes_table " +
            "WHERE title LIKE '%' || lower(:channelName) || '%' " +
            "ORDER BY timestamp DESC")
    suspend fun getChannelFeed(channelName: String): List<EpisodeEntity>

    /**
     * Search episodes where words start with the given query.
     * Matches at the beginning of the string OR after a space.
     */
    @Query("SELECT * FROM episodes_table " +
            "WHERE (lower(title) LIKE lower(:query) || '%' OR lower(title) LIKE '% ' || lower(:query) || '%') " +
            "OR (lower(description) LIKE lower(:query) || '%' OR lower(description) LIKE '% ' || lower(:query) || '%') " +
            "ORDER BY timestamp DESC")
    suspend fun searchByQuery(query: String): List<EpisodeEntity>

    @Query("SELECT * FROM episodes_table WHERE id = :id")
    suspend fun getEpisodeById(id: Int): EpisodeEntity

    @Query("SELECT * FROM episodes_table WHERE id = :id")
    fun getFlowEpisodeById(id: Int): Flow<EpisodeEntity>

    @Query("UPDATE episodes_table SET stopListeningAt = :position WHERE id = :episodeId")
    suspend fun updateEpisodePosition(episodeId: Int, position: Long)

    @Query("UPDATE episodes_table SET hasListened = 1, stopListeningAt = 0, isInQueue = 0, indexInQueue = -1 WHERE id = :episodeId")
    suspend fun markEpisodeAsListened(episodeId: Int)

    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)
}
