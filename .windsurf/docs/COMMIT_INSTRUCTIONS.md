# Git Commands for Episode Feed V2 Implementation

## Files to Add (Core Implementation)
```bash
git add app/src/main/java/com/example/echo_proto/ui/adapters/EpisodeFeedAdapterV2.kt
git add app/src/main/java/com/example/echo_proto/ui/fragments/FeedFragment.kt
git add app/src/main/res/layout/item_episode_v2.xml
git add app/src/main/res/layout/item_episode_header_v2.xml
git add app/src/main/res/drawable/ic_favorite.xml
git add app/src/main/res/drawable/ic_pause_circle.xml
git add app/src/main/res/drawable/ic_play_circle.xml
git add app/src/main/res/values/themes.xml
git add app/src/main/res/drawable/button_ic_queue_add.xml
git add app/src/main/res/values/colors_raw.xml
```

## Files to Add (Documentation)
```bash
git add .windsurf/workflows/episode-feed-v2-implementation.md
```

## Commit Message
```bash
git commit -m "feat: implement Episode Feed V2 with date headers and modern UI

- Add EpisodeFeedAdapterV2 with multiple view types (DateHeader + Episode)
- Implement date grouping with sticky headers
- Add modern episode card design with drag handle and action buttons
- Update FeedFragment to use new adapter with ItemZoneTouchHandler integration
- Add playback state management and action mode support
- Implement proper drag handle visibility control based on fragment type
- Add color filter system for consistent theming
- Update text styling for better readability

Breaking Changes:
- Replace FeedAdapter with EpisodeFeedAdapterV2 in FeedFragment
- Update episode item layout to new design system

Features:
- Date-based episode grouping
- Modern card-based UI with rounded corners
- Drag handle with blue accent color
- Favorite/Queue/Playback buttons with proper state management
- DiffUtil optimization for smooth updates
- Full ItemZoneTouchHandler integration"
```

## Single Command Alternative
```bash
git add app/src/main/java/com/example/echo_proto/ui/adapters/EpisodeFeedAdapterV2.kt app/src/main/java/com/example/echo_proto/ui/fragments/FeedFragment.kt app/src/main/res/layout/item_episode_v2.xml app/src/main/res/layout/item_episode_header_v2.xml app/src/main/res/drawable/ic_favorite.xml app/src/main/res/drawable/ic_pause_circle.xml app/src/main/res/drawable/ic_play_circle.xml app/src/main/res/values/themes.xml app/src/main/res/drawable/button_ic_queue_add.xml app/src/main/res/values/colors_raw.xml .windsurf/workflows/episode-feed-v2-implementation.md && git commit -m "feat: implement Episode Feed V2 with date headers and modern UI

- Add EpisodeFeedAdapterV2 with multiple view types (DateHeader + Episode)
- Implement date grouping with sticky headers
- Add modern episode card design with drag handle and action buttons
- Update FeedFragment to use new adapter with ItemZoneTouchHandler integration
- Add playback state management and action mode support
- Implement proper drag handle visibility control based on fragment type
- Add color filter system for consistent theming
- Update text styling for better readability

Breaking Changes:
- Replace FeedAdapter with EpisodeFeedAdapterV2 in FeedFragment
- Update episode item layout to new design system

Features:
- Date-based episode grouping
- Modern card-based UI with rounded corners
- Drag handle with blue accent color
- Favorite/Queue/Playback buttons with proper state management
- DiffUtil optimization for smooth updates
- Full ItemZoneTouchHandler integration"
```

## Files Excluded (as requested)
- `.gitignore` - Git configuration file
- Design files (`design.md`, `design_layouts.html`, etc.)
- Cleanup instructions and documentation files
- Unnecessary drawable files (`drag_dot.xml`)
