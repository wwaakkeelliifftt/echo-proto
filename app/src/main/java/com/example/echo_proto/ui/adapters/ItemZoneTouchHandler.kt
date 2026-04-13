package com.example.echo_proto.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.domain.model.Episode

/**
 * Interface for handling user interactions with episode list items.
 */
interface ItemZoneTouchHandler {
    /**
     * Whether the current fragment supports drag & drop.
     */
    val isDraggableFragment: Boolean

    /**
     * Called when a drag operation starts (e.g., via drag handle).
     */
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)

    /**
     * Navigates to the episode detail screen.
     */
    fun navigateToEpisodeDetailScreen(episode: Episode)

    /**
     * Toggles play/pause state for an episode.
     */
    fun playPauseStateChanger(episode: Episode)

    /**
     * Toggles favorite status for an episode.
     */
    fun toggleEpisodeFavorite(episode: Episode)

    /**
     * Toggles queue status for an episode.
     */
    fun toggleEpisodeQueue(episode: Episode)

    /**
     * Starts downloading an episode.
     */
    fun downloadEpisode(episode: Episode)

    /**
     * Deletes a downloaded episode from storage.
     */
    fun deleteEpisode(episode: Episode)

    /**
     * Handles long click on an episode item to show context actions.
     * @param episode The clicked episode.
     * @param position Adapter position.
     */
    fun onEpisodeLongClick(episode: Episode, position: Int)
}
