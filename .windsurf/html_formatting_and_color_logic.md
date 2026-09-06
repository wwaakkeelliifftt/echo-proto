# HTML Formatting and Color Logic Report

**Date:** April 29, 2026  
**Status:** ✅ Implemented and Working

## Overview

This document describes the current implementation of HTML formatting and color selection for WebView and TextView components in the podcast app. The system supports both dark and light themes with centralized color management.

## Architecture

### Color Management (Util.kt)

All color-related logic is centralized in `Util.kt` to ensure consistency across the app and simplify future theme additions.

#### Functions

```kotlin
// Theme detection
fun isDarkTheme(context: Context): Boolean

// Color getters for WebView (returns hex strings)
fun getTextColorForTheme(context: Context): String
fun getLinkColorForTheme(context: Context): String
fun getTimestampColorForTheme(context: Context): String

// Color getter for TextView (returns Int)
fun getLinkTextColorBlue(): Int
```

#### Color Values

| Theme | Text Color | Link Color | Timestamp Color |
|-------|-----------|------------|-----------------|
| Dark  | #FFFFFF   | #4FC3F7    | #E6AF2E         |
| Light | #000000   | #1976D2    | #E6AF2E         |

**Note:** Timestamp color (#E6AF2E - gold) is the same for both themes.

### HTML Enrichment Functions

#### `highlightTimestamps(timestampColor: String = "#E6AF2E")`

- **Purpose:** Wraps timestamps (e.g., "01:23:45", "12:34") in clickable links
- **Pattern:** `\b(\d{1,2}:\d{2}(?::\d{2})?)\b`
- **Output:** `<a href="seek://TIMESTAMP" style="color: #E6AF2E; text-decoration: none; font-weight: 600;">TIMESTAMP</a>`
- **Usage:** Custom seek protocol for audio player navigation

#### `makeLinksClickable(linkColor: String = "#4FC3F7")`

- **Purpose:** Converts plain URLs to clickable HTML links
- **Pattern:** `\b((?:https?://|www\.)[^\s<>]+)\b`
- **Output:** `<a href="URL" target="_blank" rel="noopener noreferrer" style="color: #4FC3F7; text-decoration: underline; font-weight: 600;">URL</a>`
- **Features:** 
  - Auto-prepends "https://" to www. links
  - Opens in external browser
  - Security attributes (noopener noreferrer)

#### `enrichForWebView(context: Context)`

- **Purpose:** Main entry point for HTML enrichment
- **Logic:**
  1. Checks if text already contains HTML tags
  2. If yes: Only applies `highlightTimestamps()`
  3. If no: Applies `makeLinksClickable()` then `highlightTimestamps()`
- **Theme Support:** Uses context to get theme-appropriate colors

#### `enrichForWebView()` (legacy)

- **Purpose:** Backward compatibility version without theme support
- **Uses:** Hardcoded default colors
- **Status:** Kept for compatibility, prefer `enrichForWebView(context)`

## Implementation by Screen

### 1. EpisodeDetailFragmentV2.kt (WebView)

**Component:** WebView for episode description

**Setup:**
```kotlin
private fun setupWebView() {
    binding.wvDescription.apply {
        settings.javaScriptEnabled = false
        webViewClient = CustomWebViewClient()
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }
}
```

**HTML Generation:**
```kotlin
private fun createStyledHtml(description: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    font-size: 16sp;
                    line-height: 1.6;
                    color: ${getTextColorForTheme(requireContext())};
                    margin: 0;
                    padding: 0;
                    background-color: transparent;
                }
                a {
                    color: ${getLinkColorForTheme(requireContext())};
                    text-decoration: underline;
                }
                /* Additional styles for p, strong, em, code, ul, ol, li */
            </style>
        </head>
        <body>
            ${description.enrichForWebView(requireContext())}
        </body>
        </html>
    """.trimIndent()
}
```

**URL Handling:**
- `seek://` protocol: Triggers audio player seek
- Other URLs: Opens in external browser

### 2. AudioPlayerDescriptionFragment.kt (TextView)

**Component:** TextView for audio player description

**Setup:**
```kotlin
private fun setupDescriptionText() {
    binding.tvPlayerDescription.apply {
        movementMethod = LinkMovementMethod.getInstance()
        highlightColor = Color.TRANSPARENT
        setLinkTextColor(getLinkTextColorBlue()) // #4FC3F7
    }
}
```

**Processing:**
```kotlin
private fun updateDescription(description: String) {
    val enrichedHtml = description.enrichForWebView(requireContext())
    
    val spannedText = Html.fromHtml(enrichedHtml, Html.FROM_HTML_MODE_COMPACT)
    val builder = SpannableStringBuilder(spannedText)
    
    // Custom timestamp handling with custom span renderer
    val spans = builder.getSpans(0, builder.length, URLSpan::class.java)
    for (span in spans) {
        if (span.url.startsWith("seek://")) {
            // Replace with custom ClickableSpan and TimestampTagSpan
        }
    }
    
    binding.tvPlayerDescription.text = builder
}
```

**Custom Timestamp Rendering:**
- Uses `TimestampTagSpan` (ReplacementSpan)
- Gold background with rounded corners
- Gold text color
- Consistent width and positioning

## Theme Support

### Current State
- ✅ Dark theme: Fully implemented
- ⏳ Light theme: Prepared (color functions return different values based on theme)

### Future Implementation
When adding light theme support:
1. No code changes needed in Util.kt (already theme-aware)
2. Color values are already defined for light theme
3. All screens will automatically adapt

## Design Decisions

### 1. Centralized Color Management
- **Rationale:** Single source of truth for colors
- **Benefit:** Easy to update colors globally
- **Benefit:** Simplifies theme addition

### 2. Default Parameters
- **Rationale:** Backward compatibility
- **Benefit:** Existing code continues to work
- **Trade-off:** Two versions of `enrichForWebView()`

### 3. Hex Strings vs Int Colors
- **WebView:** Uses hex strings (CSS requirement)
- **TextView:** Uses Int (Android API requirement)
- **Solution:** Separate functions for each use case

### 4. Link Color Consistency
- **Decision:** Links are blue (#4FC3F7) in both themes
- **Rationale:** User preference for consistent link color
- **Future:** Can be made theme-dependent if needed

## Testing Checklist

- [x] Timestamps are clickable in WebView
- [x] Timestamps are clickable in TextView
- [x] Links open in external browser
- [x] Seek protocol works in both screens
- [x] Colors match in both screens
- [x] Dark theme colors are correct
- [x] Light theme colors are defined (not yet tested)

## Files Modified

1. **Util.kt**
   - Added `Context` import
   - Added `Color` import
   - Added theme detection function
   - Added color getter functions
   - Updated `highlightTimestamps()` with parameter
   - Updated `makeLinksClickable()` with parameter
   - Added `enrichForWebView(context: Context)`

2. **EpisodeDetailFragmentV2.kt**
   - Removed local color functions
   - Updated imports to use Util.kt functions
   - Updated `createStyledHtml()` to use centralized functions
   - Updated `enrichForWebView()` call with context

3. **AudioPlayerDescriptionFragment.kt**
   - Added `getLinkTextColorBlue` import
   - Updated `setupDescriptionText()` to set link color
   - Updated `enrichForWebView()` call with context

## Future Improvements

1. **Remove Legacy Function:** Deprecate `enrichForWebView()` without context parameter
2. **Color Resources:** Move hex colors to colors.xml for better maintainability
3. **Theme Configuration:** Consider using Material Design color system
4. **Custom Timestamp Color:** Make timestamp color theme-dependent if needed
5. **Testing:** Add automated tests for HTML enrichment functions

## Summary

The HTML formatting and color system is now:
- ✅ Centralized in Util.kt
- ✅ Theme-aware (ready for light theme)
- ✅ Consistent across both screens
- ✅ Easy to maintain and extend
- ✅ Backward compatible

All colors are managed in one place, making future theme additions straightforward without code changes in individual screens.
