# RSS Parsing Architecture - Echo Proto

> **📋 Project Index**: See [../workflows/PROJECT_WORK_LOG.md](../workflows/PROJECT_WORK_LOG.md) for complete overview of all project work.

## 🌐 **Overview**

Complete analysis of RSS parsing architecture in Echo Proto, covering data flow from RSS feed to database storage and domain models.

## 🔄 **Data Flow Architecture**

### **📡 1. RSS Parser Layer**
```kotlin
// FeedApi.kt
class FeedApi @Inject constructor(private val parser: Parser) {
    suspend fun getFullChannelsFeed(): Channel = parser.getChannel(Constants.URL)
    suspend fun getChannelFeed(url: String): Channel = parser.getChannel(url)
}

// Constants.kt
object Constants {
    const val URL = "https://feedmaster.umputun.com/rss/echo-msk"
}
```

**External Library**: `com.prof.rssparser.Parser`

### **📋 2. RSS Data Structure**
```kotlin
// com.prof.rssparser.Channel
class Channel {
    val articles: List<Article>  // ← List of episodes
}

// com.prof.rssparser.Article
class Article {
    val title: String?           // Episode title
    val guid: String?            // Unique identifier
    val pubDate: Date?          // Publication date
    val description: String?      // Episode description
    val audio: String?           // Audio file URL
    val link: String?            // Video/page URL
    val itunesArticleData: ItunesArticleData?  // iTunes metadata
}
```

### **🔄 3. Data Transfer Objects (DTO)**
```kotlin
// EpisodeDto.kt
data class EpisodeDto(
    val title: String,           // RSS title
    val rssId: String,           // RSS GUID
    val timestamp: Long,          // RSS pubDate (converted)
    val description: String,       // RSS description
    val audioLink: String,        // RSS audio link
    val videoLink: String,        // RSS video link
    val duration: String          // iTunes duration
) {
    fun toEpisodeEntity(): EpisodeEntity = EpisodeEntity(...)
}
```

### **🗃️ 4. Database Entity Layer**
```kotlin
// EpisodeEntity.kt
@Entity(tableName = "episodes_table")
data class EpisodeEntity(
    val title: String,           // From RSS title
    val rssId: String,           // From RSS GUID
    val timestamp: Long,          // From RSS pubDate
    val duration: String,         // From iTunes duration
    val description: String,       // From RSS description
    val audioLink: String,        // From RSS audio link
    val videoLink: String,        // From RSS video link
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,         // Room-generated ID
    // UI state fields
    val isDownloaded: Boolean = false,
    val downloadUrl: String = "",
    val isFavorite: Boolean = false,
    val isInQueue: Boolean = false,
    val indexInQueue: Int = -1,
    val hasListened: Boolean = false,
    val stopListeningAt: Long = 0L
) {
    fun toEpisode(): Episode = Episode(...)
}
```

### **🎯 5. Domain Model Layer**
```kotlin
// Episode.kt
data class Episode(
    val title: String,           // From Entity
    val rssId: String,           // From Entity
    val timestamp: Long,          // From Entity
    val duration: Int,            // Converted from String
    val description: String,       // From Entity
    val audioLink: String,        // From Entity
    val videoLink: String,        // From Entity
    val mediaId: String,          // id.toString() for player
    val id: Int,                 // From Entity
    // UI state fields
    var isSelected: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadUrl: String,
    val isFavorite: Boolean = false,
    val isInQueue: Boolean = false,
    val indexInQueue: Int,
    val hasListened: Boolean = false,
    val stopListeningAt: Long = 0L
) {
    fun toEpisodeEntity(): EpisodeEntity { ... }
}
```

## 🔧 **Parsing Implementation**

### **📡 RSS Data Extraction**
```kotlin
// FeedRepositoryImpl.kt - insertApiResponseToDatabase()
private suspend fun insertApiResponseToDatabase(channel: Channel): Boolean {
    if (channel.articles.isEmpty()) {
        Timber.d("-------------->>>>>>>>>>>>>>>PROBLEM<<<<<<<<<<<-----ParserHasBadResponse")
        return true
    }
    
    val remoteEpisodesList = channel.articles.map { item ->
        EpisodeDto(
            title = item.title ?: Constants.NO_DATA,
            rssId = item.guid ?: "",
            timestamp = item.pubDate.getTimeInMillisFromString(),
            description = item.description ?: "",
            audioLink = item.audio ?: "",
            videoLink = item.link ?: "",
            duration = item.itunesArticleData?.duration ?: "0"
        )
    }
    
    // ... database insertion logic
}
```

### **🗃️ Database Integration**
```kotlin
// FeedRepositoryImpl.kt - updateFeedRss()
override fun updateFeedRss(): Flow<Resource<List<Episode>>> = flow {
    emit(Resource.Loading())
    try {
        val channelFullFeed = api.getFullChannelsFeed()
        val emptyListFlag = insertApiResponseToDatabase(channelFullFeed)
        if (emptyListFlag) {
            emit(Resource.Error(data = emptyList(), message = Constants.ERROR_EMPTY_SERVER_RESPONSE))
        } else {
            val episodesList = db.dao.getAllFeed().map { it.toEpisode() }
            emit(Resource.Success(data = episodesList))
        }
    } catch (e: Exception) {
        emit(Resource.Error(message = Constants.ERROR_NETWORK))
    }
}
```

## 📋 **Available RSS Fields**

### **🎯 Core RSS Fields**
| RSS Field | DTO Field | Entity Field | Description |
|-----------|-----------|--------------|-------------|
| `item.title` | `title` | `title` | Episode title |
| `item.guid` | `rssId` | `rssId` | Unique identifier |
| `item.pubDate` | `timestamp` | `timestamp` | Publication date |
| `item.description` | `description` | `description` | Episode description |
| `item.audio` | `audioLink` | `audioLink` | Audio file URL |
| `item.link` | `videoLink` | `videoLink` | Video/page URL |

### **🎵 iTunes Extended Fields**
| RSS Field | DTO Field | Entity Field | Description |
|-----------|-----------|--------------|-------------|
| `item.itunesArticleData.duration` | `duration` | `duration` | Episode duration |
| `item.itunesArticleData.author` | - | - | Author (not used) |
| `item.itunesArticleData.summary` | - | - | Summary (not used) |

### **🔄 Generated Fields**
| Field | Type | Description |
|-------|------|-------------|
| `id` | Int | Room auto-generated primary key |
| `mediaId` | String | `id.toString()` for player |
| `isDownloaded` | Boolean | Download state |
| `isFavorite` | Boolean | Favorite state |
| `isInQueue` | Boolean | Queue state |
| `indexInQueue` | Int | Queue position |
| `hasListened` | Boolean | Listen state |
| `stopListeningAt` | Long | Playback position |

## 🎯 **Channel Support**

### **📡 Multiple RSS Channels**
```kotlin
// FeedChannels.kt
data class FeedChannel(val id: Int, val name: String, val url: String, val tabBadgeName: String) {
    companion object {
        val listOfChannels = listOf(
            FeedChannel(0, "Живой Гвоздь", url = "https://worker.feed-master.com/yt/media/UCWAIvx2yYLK_xTYD4F2mUNw.xml", "Эхо"),
            FeedChannel(1, "Дилетант", url = "https://worker.feed-master.com/yt/media/UCuIE7-5QzeAR6EdZXwDRwuQ.xml", "Дилетант"),
            // ... 18 more channels
        )
    }
}
```

### **🔄 Channel-Specific Parsing**
```kotlin
// FeedRepositoryImpl.kt - updateChannelRss()
override fun updateChannelRss(channel: FeedChannel): Flow<Resource<List<Episode>>> = flow {
    try {
        val channelRss = api.getChannelFeed(channel.url)
        val emptyListFlag = insertApiResponseToDatabase(channelRss)
        // ... same parsing logic
    }
}
```

## 🗃️ **Database Operations**

### **📋 DAO Interface**
```kotlin
// FeedDao.kt
interface FeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodesList(episodes: List<EpisodeEntity>)
    
    @Query("SELECT * FROM episodes_table ORDER BY timestamp DESC")
    suspend fun getAllFeed(): List<EpisodeEntity>
    
    @Query("SELECT * FROM episodes_table WHERE id = :id")
    suspend fun getEpisodeById(id: Int): EpisodeEntity
    
    @Query("SELECT * FROM episodes_table WHERE title LIKE '%' || lower(:query) || '%' OR description LIKE '%' || lower(:query) || '%' ORDER BY timestamp DESC")
    suspend fun searchByQuery(query: String): List<EpisodeEntity>
    
    // ... more queries
}
```

## 🔧 **Error Handling**

### **🚨 Network Errors**
```kotlin
// FeedRepositoryImpl.kt
catch (e: Exception) {
    emit(Resource.Error(message = Constants.ERROR_NETWORK))
}
```

### **📡 Empty Response Handling**
```kotlin
if (channel.articles.isEmpty()) {
    Timber.d("-------------->>>>>>>>>>>>>>>PROBLEM<<<<<<<<<<<-----ParserHasBadResponse")
    return true
}
```

### **🔍 Data Validation**
```kotlin
val newEpisodes = remoteEpisodesList
    .filterNot { localEpisodesTitleSet.contains(it.title) }
    .filterNot { localEpisodeCrossLinkSet.contains(it.rssId) }
    .filterNot { it.title == Constants.NO_DATA }
```

## 📊 **Performance Considerations**

### **⚡ Optimization Strategies**
- **Batch Insert**: `insertEpisodesList()` for multiple episodes
- **Deduplication**: Filter by title and RSS ID
- **Flow-based**: Reactive updates for UI
- **DiffUtil**: Efficient RecyclerView updates

### **🔄 Data Consistency**
- **OnConflictStrategy.REPLACE**: Update existing episodes
- **Timestamp-based ordering**: Consistent episode order
- **Unique constraints**: Prevent duplicates

## 🎯 **Integration Points**

### **📱 UI Layer Integration**
```kotlin
// FeedViewModel.kt
fun updateFeedRss(): Boolean {
    viewModelScope.launch {
        val result = repository.updateFeedRss()
        result.collect { resource ->
            when (resource) {
                is Resource.Loading -> Timber.d("RSS FEED is LOADING")
                is Resource.Success -> _rssFeed.postValue(resource.data)
                is Resource.Error -> Timber.d("ERROR OCCUR at UPDATE")
            }
        }
    }
}
```

### **🎵 Player Integration**
```kotlin
// Episode.mediaId = id.toString()  // Used by ExoPlayer
// Episode.stopListeningAt = 0L  // Playback position
// Episode.duration = duration.toInt()  // Player duration
```

## 📝 **Future Enhancements**

### **🔧 Planned Improvements**
- **Network Connection Check**: Before RSS fetch
- **Incremental Updates**: Only new episodes
- **Image Caching**: Episode thumbnails
- **Background Sync**: Periodic RSS updates
- **Error Recovery**: Retry mechanisms

### **📊 Monitoring**
- **Parsing Metrics**: Success/failure rates
- **Performance Tracking**: Parse times
- **Data Quality**: Missing fields detection

---

*Last Updated: April 2026*
*Architecture: RSS Parser → DTO → Entity → Domain Model*
*External Dependencies: com.prof.rssparser.Parser*
