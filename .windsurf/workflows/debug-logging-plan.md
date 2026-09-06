# Debug Logging Plan - Echo Proto

> **📋 Project Index**: See [PROJECT_WORK_LOG.md](./PROJECT_WORK_LOG.md) for complete overview of all project work.
> **📚 Documentation**: See [../docs/rss-parsing-architecture.md](../docs/rss-parsing-architecture.md) for RSS parsing details.

## 🎯 **Overview**

Enhanced logging system to debug podcast channel display issues in ViewPager. Added comprehensive logging to track RSS parsing, database operations, and channel filtering to identify where new podcast channels fail to display episodes.

## 🔍 **Current Logging Implementation**

### **📝 Enhanced RSS Parsing Logs:**
```kotlin
🎯 RSS_PARSE: Processing ${channel.articles.size} articles from RSS feed
🎯 RSS_PARSE (EpisodeDTO): channel=${it.rssId}, title=${it.title}, video=${it.videoLink}, audio=${it.audioLink}
🎯 RSS_PARSE: Current database has ${episodesInDatabase.size} episodes
🎯 RSS_PARSE: Found ${newEpisodes.size} new episodes to insert
🎯 RSS_PARSE: Inserted ${newEpisodes.size} new episodes to database
---------->>>>>>>>>>>>>>>> 🚨 RSS_PARSE: Empty articles list from RSS feed
```

### **📱 Channel Database Logs:**
```kotlin
🎯 CHANNEL_DB: Searching for channel=${channel.name} in database
🎯 CHANNEL_DB: Found ${result.size} episodes for channel=${channel.name}
---------->>>>>>>>>>>>>>>> 🚨 CHANNEL_DB: EMPTY DATABASE FOR CHANNEL=${channel.name}
---------->>>>>>>>>>>>>>>> 🚨 CHANNEL_DB: Exception for channel=${channel.name}: ${e.message}
```

### **🔄 Channel Update Logs:**
```kotlin
🎯 CHANNEL_UPDATE: Starting update for channel=${channel.name}, url=${channel.url}
🎯 CHANNEL_UPDATE: RSS parsed for channel=${channel.name}, articles=${channelRss.articles.size}
🎯 CHANNEL_UPDATE: Found ${episodesList.size} episodes for channel=${channel.name} after update
🚨 CHANNEL_UPDATE: Empty server response for channel=${channel.name}
🚨 CHANNEL_UPDATE: Exception for channel=${channel.name}: ${e.message}
```

## 🧪 **Proposed Debug Methods**

### **🔧 New DAO Methods (for debugging):**
```kotlin
// FeedDao.kt - Add these methods for debugging
@Query("SELECT * FROM episodes_table WHERE title LIKE '%' || lower(:channelName) || '%' ORDER BY timestamp DESC")
suspend fun new_getChannelFeed(channelName: String): List<EpisodeEntity>

@Query("SELECT * FROM episodes_table ORDER BY timestamp DESC LIMIT 50")
suspend fun new_getRecentEpisodes(): List<EpisodeEntity>

@Query("SELECT COUNT(*) FROM episodes_table WHERE title LIKE '%' || lower(:channelName) || '%'")
suspend fun new_countChannelEpisodes(channelName: String): Int

@Query("SELECT DISTINCT substr(title, 1, 50) as title_sample FROM episodes_table ORDER BY timestamp DESC LIMIT 10")
suspend fun new_getTitleSamples(): List<String>
```

### **🔍 Debug Repository Methods:**
```kotlin
// FeedRepositoryImpl.kt - Add debugging methods
suspend fun debugChannelData(channelName: String) {
    try {
        val allEpisodes = db.dao.getAllFeed()
        val matchingEpisodes = db.dao.new_getChannelFeed(channelName)
        val count = db.dao.new_countChannelEpisodes(channelName)
        val recentEpisodes = db.dao.new_getRecentEpisodes()
        val titleSamples = db.dao.new_getTitleSamples()
        
        Timber.d("🔍 DEBUG: Total episodes in DB: ${allEpisodes.size}")
        Timber.d("🔍 DEBUG: Episodes matching '$channelName': $count")
        Timber.d("🔍 DEBUG: Recent episodes count: ${recentEpisodes.size}")
        
        matchingEpisodes.take(3).forEach { 
            Timber.d("🔍 DEBUG: Match - title=${it.title}, rssId=${it.rssId}")
        }
        
        titleSamples.forEach { 
            Timber.d("🔍 DEBUG: Title sample: $it")
        }
        
    } catch (e: Exception) {
        Timber.e("🔍 DEBUG: Exception in debugChannelData: ${e.message}")
    }
}

suspend fun debugRssParsing(channel: FeedChannel) {
    try {
        Timber.d("🔍 DEBUG: Testing RSS parsing for ${channel.name}")
        val channelRss = api.getChannelFeed(channel.url)
        
        Timber.d("🔍 DEBUG: RSS feed title: ${channelRss.title}")
        Timber.d("🔍 DEBUG: RSS feed description: ${channelRss.description?.take(100)}")
        Timber.d("🔍 DEBUG: RSS articles count: ${channelRss.articles.size}")
        
        channelRss.articles.take(3).forEachIndexed { index, article ->
            Timber.d("🔍 DEBUG: Article $index - title=${article.title}, guid=${article.guid}, audio=${article.audio}")
        }
        
    } catch (e: Exception) {
        Timber.e("🔍 DEBUG: Exception in debugRssParsing: ${e.message}")
    }
}
```

## 📊 **Expected Log Analysis**

### **✅ Working Channel (Эхо Москвы):**
```
🎯 CHANNEL_UPDATE: Starting update for channel=Эхо Москвы, url=https://feedmaster.umputun.com/rss/echo-msk
🎯 CHANNEL_UPDATE: RSS parsed for channel=Эхо Москвы, articles=10
🎯 RSS_PARSE: Processing 10 articles from RSS feed
🎯 RSS_PARSE: Current database has 150 episodes
🎯 RSS_PARSE: Found 5 new episodes to insert
🎯 RSS_PARSE: Inserted 5 new episodes to database
🎯 CHANNEL_DB: Searching for channel=Эхо Москвы in database
🎯 CHANNEL_DB: Found 10 episodes for channel=Эхо Москвы
🎯 CHANNEL_UPDATE: Found 10 episodes for channel=Эхо Москвы after update
```

### **🚨 Broken Channel (Мысли и методы):**
```
🎯 CHANNEL_UPDATE: Starting update for channel=Мысли и методы, url=https://feeds.soundcloud.com/users/soundcloud:users:259154388/sounds.rss
🎯 CHANNEL_UPDATE: RSS parsed for channel=Мысли и методы, articles=8
🎯 RSS_PARSE: Processing 8 articles from RSS feed
🎯 RSS_PARSE: Current database has 158 episodes
🎯 RSS_PARSE: Found 8 new episodes to insert
🎯 RSS_PARSE: Inserted 8 new episodes to database
🎯 CHANNEL_DB: Searching for channel=Мысли и методы in database
🎯 CHANNEL_DB: Found 0 episodes for channel=Мысли и методы
---------->>>>>>>>>>>>>>>> 🚨 CHANNEL_DB: EMPTY DATABASE FOR CHANNEL=Мысли и методы
```

### **🔍 Debug Method Output:**
```
🔍 DEBUG: Total episodes in DB: 158
🔍 DEBUG: Episodes matching 'Мысли и методы': 0
🔍 DEBUG: Recent episodes count: 50
🔍 DEBUG: Title sample: 63. Serverless: платонический код
🔍 DEBUG: Title sample: Радио-Т 1006
🔍 DEBUG: Title sample: Podlodka #470 – Vim
🔍 DEBUG: Title sample: Частные ракеты, выводящие в космос...
```

## 🎯 **Problem Diagnosis Flow**

### **📋 Step-by-Step Analysis:**

#### **1. RSS Parsing Check:**
- **Look for**: `🎯 CHANNEL_UPDATE: RSS parsed for channel=X, articles=Y`
- **Success**: Y > 0 (RSS feed parsed successfully)
- **Problem**: Y = 0 (RSS parsing failed)

#### **2. Database Insertion Check:**
- **Look for**: `🎯 RSS_PARSE: Inserted Z new episodes to database`
- **Success**: Z > 0 (Episodes saved to database)
- **Problem**: Z = 0 (No episodes saved)

#### **3. Channel Search Check:**
- **Look for**: `🎯 CHANNEL_DB: Found W episodes for channel=X`
- **Success**: W > 0 (Episodes found by channel name)
- **Problem**: W = 0 (No episodes found - THIS IS THE ISSUE!)

#### **4. Debug Method Analysis:**
- **Look for**: `🔍 DEBUG: Episodes matching 'X': Y`
- **Success**: Y > 0 (Channel name matching works)
- **Problem**: Y = 0 (Channel name matching fails)

## 🚨 **Expected Root Cause**

### **🎯 Most Likely Issue:**
```sql
-- Current (broken) query:
SELECT * FROM episodes_table 
WHERE title LIKE '%Мысли и методы%' 
ORDER BY timestamp DESC

-- Problem: Episode titles don't contain channel names!
-- Episode title: "63. Serverless: платонический код"
-- Channel name: "Мысли и методы"
-- Result: No match found
```

### **✅ Solution:**
```sql
-- Fixed query (after migration):
SELECT * FROM episodes_table 
WHERE channelId = '1' 
ORDER BY timestamp DESC

-- Result: Exact match by channel ID
```

## 📋 **Testing Plan**

### **🧪 Current Test:**
1. **Build app** with enhanced logging
2. **Open ViewPager** with multiple channels
3. **Swipe to refresh** on each channel
4. **Collect logs** for each channel
5. **Analyze patterns** between working/broken channels

### **🔍 Next Steps:**
1. **If RSS parsing fails** → Fix parser issues
2. **If database insertion fails** → Fix database logic
3. **If channel search fails** → Implement migration plan
4. **Add debug methods** if needed for deeper analysis

## 📈 **Success Criteria**

### **✅ Logging Success:**
- **Clear visibility** into each processing step
- **Identifiable failure points** in the pipeline
- **Comparable logs** between working/broken channels
- **Actionable insights** for problem resolution

### **🎯 Problem Resolution:**
- **Root cause identified** through log analysis
- **Solution implemented** (migration or fix)
- **All channels working** correctly in ViewPager
- **Enhanced monitoring** for future issues

---

## 🚀 **Implementation Timeline**

### **📅 Phase 1: Current Logging (Done)**
- **Enhanced RSS parsing logs** ✅
- **Channel database logs** ✅
- **Channel update logs** ✅

### **📅 Phase 2: Testing (Current)**
- **Build and test app** 🔄
- **Collect log data** ⏳
- **Analyze results** ⏳

### **📅 Phase 3: Debug Methods (If needed)**
- **Add new DAO methods** ⏳
- **Add debug repository methods** ⏳
- **Test with enhanced debugging** ⏳

### **📅 Phase 4: Solution Implementation**
- **Implement migration plan** ⏳
- **Fix identified issues** ⏳
- **Verify all channels work** ⏳

---

*Last Updated: April 2026*
*Priority: High - Critical for new podcast channels*
*Status: In Progress - Testing enhanced logging*
*Dependencies: FeedRepositoryImpl, FeedDao, RSS Parser*
