# hasListened Visual Implementation Plan

## 📋 Current State Analysis

### ✅ Logic Status: WORKING CORRECTLY

**When `hasListened = true` is assigned:**
- Trigger: Episode playback ends in Queue (ExoPlayer STATE_ENDED or MEDIA_ITEM_TRANSITION_REASON_AUTO)
- Location: `MediaPlayerEventListener.kt` → `MediaService.onEpisodePlaybackEnded()` → `MediaSource.markEpisodeAsListened()`
- Database update: `hasListened = 1, stopListeningAt = 0, isInQueue = 0, indexInQueue = -1`
- Reactive flows: Automatically update UI on all screens

### 🎯 Current Behavior by Screen

| Screen | Behavior for `hasListened=true` | Reactive Flow |
|--------|--------------------------------|---------------|
| **QueueFragment** | Episode removed from queue (isInQueue=false, indexInQueue=-1) | `getQueueFeedFlow()` |
| **FeedFragment** | Episode remains in feed with updated params | `getAllFeedFlow()` |
| **FeedPersonalFragment** | Episode remains, auto-updated from main feed | `getAllFeedFlow()` |
| **DownloadsFragment** | Episode remains (isDownloaded unchanged) | `getDownloadedEpisodes()` |
| **ChannelFragment** | Episode remains with updated params | `getChannelFeed()` |

### ✅ Issues Resolved

1. **Double Playback Bug**: Fixed in `MediaService.onEpisodePlaybackEnded()`. Now using `isAutoTransition` flag to correctly handle ExoPlayer's internal state transitions and avoid "restarting" the next episode after a few seconds.
2. **DownloadsFragment UI Delay**: Fixed in `FeedRepositoryImpl.getRssDownloadsFromDatabase()`. Removed restrictive `distinctUntilChangedBy` that was blocking updates when only `hasListened` changed.
3. **RecyclerView Partial Updates**: Fixed in `EpisodeFeedAdapterV2.submitList()`. Improved `areContentsTheSame` to explicitly check `hasListened` and other critical fields, ensuring UI reflects state changes immediately.

---

## 🎨 Implementation Plan

### Phase 1: Visual Effect for Listened Episodes

**Goal:** Apply light dimming to episodes with `hasListened=true` in all recyclers except QueueFragment

**Visual Effect Choice:**
- **Dimming entire card** (simpler implementation, easier maintenance)
- Medium dimming: alpha = 0.5 (50% transparency)
- Preserves button interactivity (Android allows clicks through alpha views)

**Implementation Location:**
- `EpisodeFeedAdapterV2.kt` - `EpisodeViewHolder.bind()` method
- Apply dimming based on `episode.hasListened`
- Skip dimming for QueueFragment (episodes already removed)

**Technical Approach:**
```kotlin
// In EpisodeViewHolder.bind()
if (episode.hasListened && adapter.playbackButtonMode != PlaybackButtonMode.PLAY_STREAMING) {
    binding.cardEpisode.alpha = 0.5f
} else {
    binding.cardEpisode.alpha = 1.0f
}
```

### ✅ Implementation Details (Phase 1) - COMPLETED

**Changes made to `EpisodeFeedAdapterV2.kt`:**

1. **Added PAYLOAD_LISTENED constant**: Efficiently updates only the alpha state.
2. **Added updateListenedState() method**: Handles the alpha transition (0.5f for listened, 1.0f for active).
3. **Improved submitList()**: Now uses comprehensive content comparison to trigger proper DiffUtil updates.
4. **Optimistic UI Updates**: `updateInternalItemState` correctly triggers `PAYLOAD_LISTENED`.

**Result:**
- Listened episodes are dimmed (alpha = 0.5) in all fragments except QueueFragment.
- UI updates are instantaneous and flicker-free.
- Playback flow is smooth without duplicate starts.

---

## 🔮 Future Implementation Plans

### Phase 2: Manual hasListened Assignment in QueueFragment

**Scenario:** User wants to manually mark episodes as listened in Queue without removing them from queue.

**Implementation Requirements:**
- Add dialog menu item for long-click: "Mark as listened"
- Add action mode menu item for multi-select.
- Modify `markEpisodeAsListened()` to accept optional `keepInQueue` parameter.
- Update `QueueFragment` to apply dimming for listened episodes.

---

## 📝 Implementation Checklist

### Phase 1 (Current) - COMPLETED ✅
- [x] Add dimming logic to `EpisodeFeedAdapterV2.kt`
- [x] Fix `submitList()` content comparison logic
- [x] Fix `MediaService` auto-transition logic (avoid double starts)
- [x] Fix `FeedRepository` download flow (reactive updates)
- [x] Test on DownloadsFragment (instant dimming verified)
- [x] Verify button interactivity preserved

### Phase 2 (Future)
- [ ] Add `markEpisodeAsListenedKeepInQueue()` to FeedDao
- [ ] Add long-click dialog to QueueFragment
- [ ] Add action mode menu to QueueFragment
