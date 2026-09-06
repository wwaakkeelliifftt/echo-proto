# Episode Detail V2 - Documentation & Future Plans

> **📋 Project Index**: See [.windsurf/workflows/PROJECT_WORK_LOG.md](../.windsurf/workflows/PROJECT_WORK_LOG.md) for complete overview of all project work.

## 📋 **Current Stage Summary**
- ✅ **New layout created**: `fragment_episode_detail_v2.xml` based on Nocturne Gold design.
- ✅ **New text styles added**: EpisodeDetail text appearances in `themes.xml`.
- ✅ **Fonts added**: Manrope font family fully configured.
- ✅ **Color Migration**: Material 3 Surface Containers and Nocturne Gold palette moved to `colors_raw.xml`.
- ✅ **Cleaned up layout**: Removed redundant Toolbar and LoadingBadge.
- ✅ **Progress Bar**: Now a full-width 4dp separator, always visible, with smooth transition (Dark Bronze to Bright Gold).
- ✅ **Action Buttons**: Updated states for Queue and Download (Gold background + Black text/icon when active).
- ✅ **Navigation**: Switched to V2 in `nav_graph.xml`.
- 🚧 **In Progress**: Implementing Collapsing Header functionality.

## 🗂️ **Files Analysis**
### **NEW FILES (keep)**
```
✅ fragment_episode_detail_v2.xml           - New layout with Nocturne Gold design
✅ EpisodeDetailFragmentV2.kt               - New fragment with state logic
✅ themes.xml (extended)                      - Text appearances and chip styles
✅ colors_raw.xml                            - Nocturne Gold testing palette
✅ manrope.xml                               - Font family configuration
```

## 📝 **Collapsing Header Plan**
### **Requirements:**
1. **Collapsed State**:
    - `tvTitle`: Single line, font size reduced by 20%.
    - `btnDownload`: Visible and pinned.
    - `progressBar`: Visible and pinned at the bottom.
    - `tvMetadata`, `btnAddToQueue`, `btnPlay`: Fully hidden (alpha 0).
2. **Implementation**:
    - Use `CollapsingToolbarLayout` inside `AppBarLayout`.
    - Custom logic via `AppBarLayout.OnOffsetChangedListener` in the Fragment.
    - Title scaling and translation.

---
*Last updated: Buttons polished, progress bar fixed, collapsing header plan added.*
