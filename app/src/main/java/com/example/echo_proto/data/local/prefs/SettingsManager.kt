package com.example.echo_proto.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class representing UI display options for episode items
 */
data class EpisodeDisplayOptions(
    val isCompactMode: Boolean = false,
    val showImageCover: Boolean = true,
    val showMetadata: Boolean = true,
    val isSpaceOptimized: Boolean = false,
    val showFavoriteButton: Boolean = true,
    val showQueueButton: Boolean = true,
    val showDateHeaders: Boolean = true // 🔧 NEW: Toggle for sticky date headers
)

/**
 * Manages application and screen-specific settings using SharedPreferences
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val flows = mutableMapOf<String, MutableStateFlow<EpisodeDisplayOptions>>()

    /**
     * Returns a reactive stream for a specific screen. 
     */
    fun getOptionsFlow(screenKey: String): StateFlow<EpisodeDisplayOptions> {
        return flows.getOrPut(screenKey) {
            MutableStateFlow(loadDisplayOptions(screenKey))
        }.asStateFlow()
    }

    /**
     * Load options for a specific screen (Direct Read)
     */
    fun loadDisplayOptions(screenKey: String): EpisodeDisplayOptions {
        // Default value for headers depends on screen
        val defaultHeaders = screenKey == "feed"
        
        val options = EpisodeDisplayOptions(
            isCompactMode = prefs.getBoolean("${screenKey}_compact_mode", false),
            showImageCover = prefs.getBoolean("${screenKey}_show_image_cover", true),
            showMetadata = prefs.getBoolean("${screenKey}_show_metadata", true),
            isSpaceOptimized = prefs.getBoolean("${screenKey}_is_space_optimized", false),
            showFavoriteButton = prefs.getBoolean("${screenKey}_show_favorite", true),
            showQueueButton = prefs.getBoolean("${screenKey}_show_queue", true),
            showDateHeaders = prefs.getBoolean("${screenKey}_show_date_headers", defaultHeaders)
        )

        // 🔧 ARCHITECTURAL CONSTRAINT: 
        // Always force-disable headers for Queue screen to prevent drag-and-drop conflicts.
        return if (screenKey == "queue") options.copy(showDateHeaders = false) else options
    }

    /**
     * Save options for a specific screen and notify observers instantly
     */
    fun saveDisplayOptions(screenKey: String, options: EpisodeDisplayOptions) {
        // Enforce the same constraint on save
        val finalOptions = if (screenKey == "queue") options.copy(showDateHeaders = false) else options

        prefs.edit().apply {
            putBoolean("${screenKey}_compact_mode", finalOptions.isCompactMode)
            putBoolean("${screenKey}_show_image_cover", finalOptions.showImageCover)
            putBoolean("${screenKey}_show_metadata", finalOptions.showMetadata)
            putBoolean("${screenKey}_is_space_optimized", finalOptions.isSpaceOptimized)
            putBoolean("${screenKey}_show_favorite", finalOptions.showFavoriteButton)
            putBoolean("${screenKey}_show_queue", finalOptions.showQueueButton)
            putBoolean("${screenKey}_show_date_headers", finalOptions.showDateHeaders)
            apply()
        }
        
        flows[screenKey]?.value = finalOptions
    }
}
