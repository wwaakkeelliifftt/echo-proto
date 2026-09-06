# ItemZoneTouchHandler - Current State & Future Plan

## 📋 Overview

This document tracks the current implementation of `ItemZoneTouchHandler` interface across all fragments and outlines the plan for future improvements.

---

## 🔍 Current Method List

| Method | Purpose | Current Implementation |
| :--- | :--- | :--- |
| `isDraggableFragment` | Does screen support Drag & Drop | `true` (Queue), `false` (others) |
| `onStartDrag` | Initiate drag operation | Implemented in QueueFragment |
| `navigateToEpisodeDetailScreen` | Navigate to episode details | Everywhere (via MainViewModel or local VM) |
| `playPauseStateChanger` | Play/Pause button | Everywhere (via MainViewModel) |
| `toggleEpisodeFavorite` | Favorite button (heart) | Everywhere (via MainViewModel) |
| `toggleEpisodeQueue` | Queue button (+) | Everywhere (via MainViewModel) |
| `toggleEpisodeQueueInQueueFragment` | Remove from queue in Queue | Only in QueueFragment (others: TODO or error) |
| `downloadEpisode` | Download button | Everywhere (stub or MainViewModel) |
| `deleteEpisode` | Delete downloaded file | Everywhere (stub or MainViewModel) |
| `onEpisodeLongClick` | Long click logic | Everywhere (show AlertDialog) |

---

## 📱 Screen-by-Screen Analysis

### QueueFragment (Queue Screen)
**Specifics:**
- Only screen with Drag & Drop and swipes
- Uses `toggleEpisodeQueueInQueueFragment` for "optimistic" deletion (card disappears instantly on click)
- Swipes: Implemented via `SwipeToDeleteCallback_Queue`

### DownloadsFragment (Downloads Screen)
**Specifics:**
- Action button configured for DELETE
- Swipes: Not implemented (though logically appropriate for deleting downloads)

### FeedFragment / FeedPersonalFragment
**Specifics:**
- Main feed screens
- Action button configured for DOWNLOAD
- Swipes: Not supported

### ChannelFragment (Channel Screen)
**Specifics:**
- Dynamic background, dimming animations on scroll
- Swipes: Not supported

---

## 🎯 Behavior Scenarios (Draft)

| Screen | Swipe Left | Drag & Drop | Click Queue (+) | Click Queue (-) |
| :--- | :--- | :--- | :--- | :--- |
| Queue | Delete (with index recalculation) | Yes (with ExoPlayer sync) | N/A | Instant disappear (Optimistic) |
| Feed | No | No | Animation +1 | Animation -1 |
| Downloads | Delete file (Planned) | No | Animation +1 | Animation -1 |
| Channel | No | No | Animation +1 | Animation -1 |

---

## 🚀 Work Plan

### Immediate Task: Fix Compilation Errors
- [ ] Add empty (default) implementation for `toggleEpisodeQueueInQueueFragment` in all fragments except QueueFragment
- [ ] This ensures project always compiles

### Phase 1: Unification & Base Class (Optimization)
- [ ] Create `BaseEpisodeFragment` that implements `ItemZoneTouchHandler` by default
- [ ] This removes code duplication (e.g., `mainViewModel.playOrToggleEpisode` is called 5 times)
- [ ] Fragments will override only specific methods (e.g., `onEpisodeLongClick` or Drag logic)

### Phase 2: Implement Swipes for Downloads
- [ ] Create `SwipeToDeleteCallback_Downloads`
- [ ] Implement file deletion and DB update on swipe left in DownloadsFragment

### Phase 3: Polish Interactions
- [ ] Queue: Move `DisplaySettingsBottomSheet` logic from `onEpisodeLongClick` to common place
- [ ] Multi-selection: Implement `startSelectionMode` (currently stub 100500 everywhere)

---

## 📝 Notes for Future Implementation

- User will add detailed descriptions of expected behavior for each screen
- Need to clarify swipe availability: currently only Queue and Downloads (planned)
- Need to define exact behavior for queue button on each screen
- Need to define long click behavior per screen
