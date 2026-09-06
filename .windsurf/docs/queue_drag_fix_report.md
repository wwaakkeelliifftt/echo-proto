# Report: Queue Drag-and-Drop Order Fix

## 1. Problem Description
The user observed a discrepancy between the visual order of episodes on the Queue screen and the actual playback order in ExoPlayer.
Key symptoms:
- After dragging an item, the new order in the logs (player) didn't always match the screen.
- Sometimes items were swapped or shifted by 1 position (e.g., ID 246 vs ID 260).
- Logs showed multiple rapid updates from the database during a single drag-and-drop operation.

## 2. Root Cause Analysis
The previous implementation of `SwipeToDeleteCallback_Queue.clearView` was updating indices one-by-one in separate coroutines.
- Each `repository.changeEpisodeQueueIndex` call triggered a database update.
- Since the repository returns a reactive `Flow`, every single index change pushed a "partial" new list to the UI.
- `DiffUtil` in the adapter was fighting with these intermediate states, leading to race conditions and corrupted `indexInQueue` values in the database.

## 3. Implemented Solution
We moved from individual updates to an **atomic batch update**.

### Changes:
1. **`FeedRepository`**: Added `suspend fun updateQueueOrder(episodeIds: List<Int>)`.
2. **`FeedRepositoryImpl`**: Implemented the method using `db.withTransaction`. Now all indices are updated in a single atomic block, and the UI receives only one "final" update.
3. **`QueueViewModel`**: Added `updateFullQueueOrder` to bridge the callback and repository.
4. **`SwipeToDeleteCallback_Queue`**: Updated `clearView` to collect all IDs from the adapter and send them as a single list.
5. **Logging**: Added detailed `ADAPTER` and `DRAG` logs to `EpisodeFeedAdapterV2` and the callback to monitor synchronization.

## 4. Observations & Further Recommendations
- **Date Headers Issue**: Dragging items when `showDateHeaders` is active is counter-intuitive. Date headers are treated as items in the list, so episodes can be "dragged over" or "swapped with" headers, which breaks the chronological logic.
- **Recommendation**: Disable `showDateHeaders` specifically for the Queue screen to ensure a pure, manageable list of playback items.
- **Current State**: The fix ensures that any new drag operation will overwrite all indices correctly, fixing any "legacy" order issues from previous versions.

## 5. Status
- [x] Atomic DB transaction for queue order.
- [x] Sync between Adapter and ViewModel.
- [x] Enhanced logging for verification.
- [ ] Task: Force disable Date Headers on Queue screen (Planned).
