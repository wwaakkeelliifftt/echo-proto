# New Podcast Channels Analysis - Echo Proto

> **📋 Project Index**: See [PROJECT_WORK_LOG.md](./PROJECT_WORK_LOG.md) for complete overview of all project work.
> **📚 Documentation**: See [../docs/rss-parsing-architecture.md](../docs/rss-parsing-architecture.md) for RSS parsing details.

## 🎯 **Overview**

Analysis of new podcast RSS feeds that are not working properly with the current RSS parser. The new channels use different RSS structures and require parser updates to extract episode data correctly.

## 📡 **New RSS Sources Analysis**

### **🎧 1. Мысли и методы (SoundCloud)**
**URL**: `https://feeds.soundcloud.com/users/soundcloud:users:259154388/sounds.rss`

#### **🔍 RSS Structure Analysis:**
```xml
<rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
  <channel>
    <title>Мысли и методы</title>
    <description>Научно-образовательный подкаст о программировании...</description>
    <itunes:image href="https://i1.sndcdn.com/avatars-000593634591-ekvf5w-original.jpg"/>
    
    <item>
      <guid isPermaLink="false">tag:soundcloud,2010:tracks/907607083</guid>
      <title>63. Serverless: платонический код</title>
      <pubDate>Fri, 09 Oct 2020 09:02:51 +0000</pubDate>
      <link>https://soundcloud.com/mimpod/episode_63</link>
      <itunes:duration>00:45:28</itunes:duration>
      <itunes:author>Рахим Давлеткалиев</itunes:author>
      <description>Как забыть почти обо всём и писать веб-приложения...</description>
      <enclosure type="audio/mpeg" url="https://feeds.soundcloud.com/stream/907607083-mimpod-episode_63.mp3" length="32745056"/>
      <itunes:image href="https://i1.sndcdn.com/artworks-2sUaehRngk0BMQ1V-8JhN2w-t3000x3000.jpg"/>
    </item>
  </channel>
</rss>
```

#### **✅ Working Fields:**
- **title** - Episode title
- **guid** - Unique ID
- **pubDate** - Publication date
- **link** - Episode page URL
- **itunes:duration** - Duration
- **itunes:author** - Author
- **description** - Description
- **enclosure.url** - Audio URL
- **itunes:image** - Episode image

#### **🎯 Key Differences:**
- **SoundCloud-specific GUID format**: `tag:soundcloud,2010:tracks/907607083`
- **Audio URLs**: SoundCloud stream URLs
- **Images**: SoundCloud CDN URLs

---

### **🎧 2. Радио-Т (FeedBurner)**
**URL**: `https://feeds.feedburner.com/Radio-t`

#### **🔍 RSS Structure Analysis:**
```xml
<rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" version="2.0">
  <channel>
    <title>Радио-Т</title>
    <itunes:image href="https://radio-t.com/images/cover.jpg"/>
    
    <item>
      <title>Радио-Т 1006</title>
      <description><![CDATA[00:00:00 Вступление<br>...]]></description>
      <link>https://radio-t.com/p/2026/03/28/podcast-1006/</link>
      <guid>https://radio-t.com/p/2026/03/28//podcast-1006/</guid>
      <pubDate>Sat, 28 Mar 2026 18:03:36 UTC</pubDate>
      <itunes:author>Umputun, Bobuk, Gray, Ksenks, Alek.sys</itunes:author>
      <itunes:summary><![CDATA[<p><img src="https://radio-t.com/images/radio-t/rt1006.jpg" alt="" /></p>...]]></itunes:summary>
      <itunes:image href="https://radio-t.com/images/radio-t/rt1006.jpg"/>
      <enclosure length="146303092" type="audio/mp3" url="https://cdn.radio-t.com/rt_podcast1006.mp3"/>
      <itunes:explicit>no</itunes:explicit>
    </item>
  </channel>
</rss>
```

#### **✅ Working Fields:**
- **title** - Episode title
- **guid** - Unique ID
- **pubDate** - Publication date
- **link** - Episode page URL
- **itunes:author** - Authors
- **description** - CDATA description
- **itunes:summary** - CDATA summary with images
- **enclosure.url** - Audio URL
- **itunes:image** - Episode image

#### **🎯 Key Differences:**
- **CDATA sections** in description and summary
- **HTML content** in descriptions
- **Images embedded** in descriptions
- **FeedBurner-specific URLs**

---

### **🎧 3. Запуск завтра (Transistor.fm)**
**URL**: `https://feeds.transistor.fm/5f1e0bb2-458b-4ac4-8d85-f464a505f813`

#### **🔍 RSS Structure Analysis:**
```xml
<rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" xmlns:podcast="https://podcastindex.org/namespace/1.0">
  <channel>
    <title>Запуск завтра</title>
    <itunes:image href="https://img.transistorcdn.com/NYHSTYm4WW6ZIe3l8J-IK9NBuRnRpLfwDl9tRJ8a5hM/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/..."/>
    
    <item>
      <title>Частные ракеты, выводящие в космос тысячи спутников...</title>
      <description><![CDATA[<p>Частные ракеты, выводящие в космос тысячи спутников...</p>]]></description>
      <content:encoded><![CDATA[<p>Частные ракеты, выводящие в космос тысячи спутников...</p>]]></content:encoded>
      <pubDate>Fri, 23 May 2025 18:12:06 +0300</pubDate>
      <author>libo/libo</author>
      <enclosure url="https://media.transistor.fm/d2c409a4/e69b6231.mp3" length="76259322" type="audio/mpeg"/>
      <itunes:author>libo/libo</itunes:author>
      <itunes:image href="https://img.transistorcdn.com/regYv79m7O77rMSODftFYesVqkrFvxWVBwvzrf24BCI/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/..."/>
      <itunes:duration>3176</itunes:duration>
    </item>
  </channel>
</rss>
```

#### **✅ Working Fields:**
- **title** - Episode title
- **description** - CDATA description
- **content:encoded** - Additional CDATA content
- **pubDate** - Publication date
- **author** - Author
- **enclosure.url** - Audio URL
- **itunes:author** - iTunes author
- **itunes:image** - Episode image
- **itunes:duration** - Duration (seconds)

#### **🎯 Key Differences:**
- **content:encoded** field for additional content
- **Transistor.fm CDN URLs**
- **Duration in seconds** (not HH:MM:SS format)
- **Additional namespaces**: `podcast`, `content`

---

### **🎧 4. Podlodka (SoundCloud)**
**URL**: `https://feeds.soundcloud.com/users/soundcloud:users:291337106/sounds.rss`

#### **🔍 RSS Structure Analysis:**
```xml
<rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
  <channel>
    <title>Podlodka Podcast</title>
    <itunes:image href="https://i1.sndcdn.com/avatars-zahUKr5M4VhexYPo-72DBYA-original.jpg"/>
    
    <item>
      <guid isPermaLink="false">tag:soundcloud,2010:tracks/2294681444</guid>
      <title>Podlodka #470 – Vim</title>
      <pubDate>Wed, 01 Apr 2026 16:23:17 +0000</pubDate>
      <link>https://soundcloud.com/podlodka/podlodka-470</link>
      <itunes:duration>01:20:55</itunes:duration>
      <itunes:author>Егор Толстой, Стас Цыганов, Екатерина Петрова и Евгений Кателла</itunes:author>
      <description>Программисты — одна из самых уязвимых групп по боли в спине и шее...</description>
      <enclosure type="audio/mpeg" url="http://dts.podtrac.com/redirect.mp3/feeds.soundcloud.com/stream/2263132268-podlodka-podlodka-463.mp3" length="184641827"/>
      <itunes:image href="https://i1.sndcdn.com/artworks-RHd2xMDZmLzPAKd5-HtOJ4A-t3000x3000.png"/>
    </item>
  </channel>
</rss>
```

#### **✅ Working Fields:**
- **title** - Episode title
- **guid** - Unique ID
- **pubDate** - Publication date
- **link** - Episode page URL
- **itunes:duration** - Duration
- **itunes:author** - Authors
- **description** - Description
- **enclosure.url** - Audio URL
- **itunes:image** - Episode image

#### **🎯 Key Differences:**
- **Podtrac redirect URLs** for audio
- **SoundCloud CDN URLs** for images
- **Multiple authors** in iTunes field

---

## 🔧 **Parser Issues & Solutions**

### **🚨 Current Parser Problems:**

#### **1. Missing Image Support**
**Issue**: Current `EpisodeDto` doesn't include image URL
```kotlin
// Current EpisodeDto
data class EpisodeDto(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val duration: String
)
```

**Solution**: Add image field
```kotlin
// Updated EpisodeDto
data class EpisodeDto(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val duration: String,
    val imageUrl: String = ""  // ← NEW FIELD
)
```

#### **2. CDATA Content Handling**
**Issue**: Some feeds use CDATA sections with HTML content
```xml
<description><![CDATA[<p>Content with <strong>HTML</strong></p>]]></description>
```

**Solution**: Strip HTML tags from CDATA content
```kotlin
private fun stripHtml(html: String): String {
    return html.replace("<[^>]*>".toRegex(), "").trim()
}
```

#### **3. Duration Format Variations**
**Issue**: Different duration formats
- **HH:MM:SS**: `01:20:55`
- **Seconds**: `3176`

**Solution**: Handle both formats
```kotlin
private fun parseDuration(duration: String): String {
    return if (duration.contains(":")) {
        duration  // Already in HH:MM:SS format
    } else {
        val seconds = duration.toIntOrNull() ?: 0
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}
```

#### **4. Audio URL Redirects**
**Issue**: Some URLs use redirect services
```xml
<enclosure url="http://dts.podtrac.com/redirect.mp3/feeds.soundcloud.com/stream/..." />
```

**Solution**: Handle redirects or use direct URLs
```kotlin
private fun getDirectAudioUrl(url: String): String {
    return if (url.contains("podtrac.com")) {
        // Extract direct URL from redirect
        url.substringAfter("redirect.mp3/")
    } else {
        url
    }
}
```

---

## 🔄 **Required Code Changes**

### **📝 1. Update EpisodeDto**
```kotlin
// EpisodeDto.kt
data class EpisodeDto(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val duration: String,
    val imageUrl: String = ""  // ← NEW
) {
    fun toEpisodeEntity(): EpisodeEntity =
        EpisodeEntity(
            title = title,
            rssId = rssId,
            timestamp = timestamp,
            description = description,
            audioLink = audioLink,
            videoLink = videoLink,
            duration = duration,
            imageUrl = imageUrl,  // ← NEW
            // ... existing fields
        )
}
```

### **🗃️ 2. Update EpisodeEntity**
```kotlin
// EpisodeEntity.kt
@Entity(tableName = "episodes_table")
data class EpisodeEntity(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val duration: String,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val imageUrl: String = "",  // ← NEW
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    // ... existing fields
)
```

### **🎯 3. Update Episode Domain Model**
```kotlin
// Episode.kt
data class Episode(
    val title: String,
    val rssId: String,
    val timestamp: Long,
    val duration: Int,
    val description: String,
    val audioLink: String,
    val videoLink: String,
    val imageUrl: String = "",  // ← NEW
    val mediaId: String,
    val id: Int,
    // ... existing fields
)
```

### **🔧 4. Update RSS Parser Logic**
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
            description = stripHtml(item.description ?: ""),
            audioLink = getDirectAudioUrl(item.audio ?: ""),
            videoLink = item.link ?: "",
            duration = parseDuration(item.itunesArticleData?.duration ?: "0"),
            imageUrl = item.itunesArticleData?.image?.href ?: ""  // ← NEW
        )
    }
    
    // ... rest of the logic
}

private fun stripHtml(html: String): String {
    return html.replace("<[^>]*>".toRegex(), "").trim()
}

private fun parseDuration(duration: String): String {
    return if (duration.contains(":")) {
        duration
    } else {
        val seconds = duration.toIntOrNull() ?: 0
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}

private fun getDirectAudioUrl(url: String): String {
    return if (url.contains("podtrac.com")) {
        url.substringAfter("redirect.mp3/")
    } else {
        url
    }
}
```

---

## 🗄️ **Database Migration**

### **📝 Migration Script**
```kotlin
// Database Migration
@Database(
    entities = [EpisodeEntity::class],
    version = 2  // ← Increment version
)
abstract class FeedDatabase : RoomDatabase() {
    // ...
}

// Migration from v1 to v2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE episodes_table ADD COLUMN imageUrl TEXT NOT NULL DEFAULT ''")
    }
}
```

---

## 🎯 **Implementation Plan**

### **📋 Phase 1: Data Model Updates**
1. **Update EpisodeDto** with imageUrl field
2. **Update EpisodeEntity** with imageUrl field  
3. **Update Episode** domain model with imageUrl
4. **Create database migration** script

### **📋 Phase 2: Parser Enhancement**
1. **Add HTML stripping** utility function
2. **Add duration parsing** utility function
3. **Add audio URL redirect handling**
4. **Update insertApiResponseToDatabase** method

### **📋 Phase 3: UI Integration**
1. **Update EpisodeFeedAdapterV2** to display images
2. **Update episode detail screen** with image support
3. **Update player UI** with episode images
4. **Add image caching** with Glide/Coil

### **📋 Phase 4: Testing**
1. **Test all new RSS feeds** work correctly
2. **Verify image loading** in all screens
3. **Test backward compatibility** with old feeds
4. **Performance testing** with image loading

---

## 🚀 **Benefits of Implementation**

### **✅ Immediate Benefits:**
- **All new podcast feeds** will work correctly
- **Episode images** will be displayed in UI
- **Better content parsing** from various RSS formats
- **Improved user experience** with visual content

### **🔮 Future Benefits:**
- **Support for more RSS feeds** with different structures
- **Foundation for video podcasts** (videoLink field)
- **Better SEO** with image metadata
- **Social sharing** with episode images

---

## 📊 **Testing Checklist**

### **🧪 RSS Feed Testing:**
- [ ] **Мысли и методы** - SoundCloud feed
- [ ] **Радио-Т** - FeedBurner feed  
- [ ] **Запуск завтра** - Transistor.fm feed
- [ ] **Podlodka** - SoundCloud feed
- [ ] **DevOps Kitchen** - PodBean feed
- [ ] **DevZen** - Custom RSS feed
- [ ] **Кверти** - RedBarn RSS feed
- [ ] **Мы Обречены** - MaveCloud RSS feed

### **🧪 Functionality Testing:**
- [ ] **Episode titles** display correctly
- [ ] **Descriptions** parse without HTML tags
- [ ] **Duration** formats work correctly
- [ ] **Audio URLs** handle redirects
- [ ] **Image URLs** extract and display
- [ ] **Database migration** works smoothly

---

*Last Updated: April 2026*
*Priority: High - Required for new podcast channels to work*
*Dependencies: RSS Parser Library, Room Database Migration*
