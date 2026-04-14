package com.example.echo_proto.ui.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.data.local.prefs.EpisodeDisplayOptions
import com.example.echo_proto.databinding.ItemEpisodeHeaderV2Binding
import com.example.echo_proto.databinding.ItemEpisodeV2Binding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.common.PlaybackStateAware
import com.example.echo_proto.util.*
import timber.log.Timber

/**
 * Unified Adapter for Episode Feed V2.
 * Supports date headers, background animations, and various display options.
 */
class EpisodeFeedAdapterV2(
    private val itemZoneHandler: ItemZoneTouchHandler,
    initialOptions: EpisodeDisplayOptions = EpisodeDisplayOptions()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PlaybackStateAware {
    
    private var currentPlayingEpisodeId: Int? = null
    private var previousPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false
    private var displayOptions: EpisodeDisplayOptions = initialOptions
    private var playbackButtonMode: PlaybackButtonMode = PlaybackButtonMode.PLAY_DOWNLOADED

    // Drag & drop protection flag
    var isDragAndDropActive: Boolean = false

    // Background animation support
    var itemsBackgroundFactor: Float = 0f
        set(value) {
            field = value
            // Note: Updating only visible items requires RecyclerView reference
            // For now keeping full update as this is P2 optimization
            notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND)
        }

    // Drag handle alpha support
    var dragHandleAlpha: Float = 0f
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_DRAG_ALPHA)
        }

    // AsyncListDiffer for background thread diff calculation
    private val differCallback = object : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return when {
                oldItem is FeedItem.DateHeader && newItem is FeedItem.DateHeader -> oldItem.date == newItem.date
                oldItem is FeedItem.EpisodeItem && newItem is FeedItem.EpisodeItem -> oldItem.episode.id == newItem.episode.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return if (oldItem is FeedItem.EpisodeItem && newItem is FeedItem.EpisodeItem) {
                // Skip stopListeningAt comparison for non-playing episodes to avoid unnecessary re-binds
                val oldEpisode = oldItem.episode
                val newEpisode = newItem.episode
                val isCurrentEpisode = oldEpisode.id == currentPlayingEpisodeId

                oldEpisode.hasListened == newEpisode.hasListened &&
                oldEpisode.isInQueue == newEpisode.isInQueue &&
                oldEpisode.isFavorite == newEpisode.isFavorite &&
                oldEpisode.isDownloaded == newEpisode.isDownloaded &&
                // Only compare stopListeningAt for current episode or when transitioning 0 -> >0
                (isCurrentEpisode || oldEpisode.stopListeningAt == newEpisode.stopListeningAt ||
                 (oldEpisode.stopListeningAt == 0L && newEpisode.stopListeningAt > 0L) ||
                 (oldEpisode.stopListeningAt > 0L && newEpisode.stopListeningAt == 0L)) &&
                oldEpisode.title == newEpisode.title
            } else {
                oldItem == newItem
            }
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    private val items: List<FeedItem> get() = differ.currentList
    var isActionModeActive: Boolean = false
    val actualList: List<FeedItem> get() = items

    companion object {
        const val TYPE_DATE_HEADER = 0
        const val TYPE_EPISODE = 1

        const val PAYLOAD_PLAYBACK = "playback"
        const val PAYLOAD_SELECTED = "selected"
        const val PAYLOAD_FAVORITE = "favorite"
        const val PAYLOAD_QUEUE = "queue"
        const val PAYLOAD_LISTENED = "listened"
        const val PAYLOAD_DISPLAY_OPTIONS = "display_options"
        const val PAYLOAD_BACKGROUND = "background_alpha"
        const val PAYLOAD_DRAG_ALPHA = "drag_alpha"

        enum class PlaybackButtonMode {
            PLAY_DOWNLOADED,  // Синий play/pause для скачанных эпизодов
            PLAY_STREAMING,   // Акцентный золотой круг play/pause (QueueFragment)
            DOWNLOAD,         // Акцентный золотой круг download (FeedFragment, ChannelFragment)
            DELETE            // Красный круг delete (DownloadsFragment)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FeedItem.DateHeader -> TYPE_DATE_HEADER
            is FeedItem.EpisodeItem -> TYPE_EPISODE
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> DateHeaderViewHolder.create(parent)
            TYPE_EPISODE -> EpisodeViewHolder.create(parent, this)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FeedItem.DateHeader -> (holder as DateHeaderViewHolder).bind(item)
            is FeedItem.EpisodeItem -> (holder as EpisodeViewHolder).bind(item.episode, displayOptions)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = items[position]
            if (item is FeedItem.EpisodeItem && holder is EpisodeViewHolder) {
                val payloadSet = (payloads.firstOrNull() as? Set<*>) ?: payloads.toSet()
                
                // 🚀 CRITICAL: Update the internal reference in ViewHolder for subsequent clicks
                holder.refreshCurrentEpisode(item.episode)

                if (payloadSet.contains(PAYLOAD_DISPLAY_OPTIONS)) holder.updateVisibility(displayOptions)
                if (payloadSet.contains(PAYLOAD_FAVORITE)) holder.updateFavoriteButton(item.episode.isFavorite)
                if (payloadSet.contains(PAYLOAD_QUEUE)) holder.updateQueueButton(item.episode.isInQueue)
                if (payloadSet.contains(PAYLOAD_PLAYBACK)) {
                    holder.updatePlaybackButton()
                    holder.updateListenedState() // Update alpha when playback state changes
                }
                if (payloadSet.contains(PAYLOAD_LISTENED)) holder.updateListenedState()
                if (payloadSet.contains(PAYLOAD_BACKGROUND)) holder.updateBackgroundAlpha()
                if (payloadSet.contains(PAYLOAD_DRAG_ALPHA)) holder.updateDragHandleVisibility()
            }
        }
    }

    fun updateDisplayOptions(newOptions: EpisodeDisplayOptions) {
        this.displayOptions = newOptions
        notifyItemRangeChanged(0, itemCount, PAYLOAD_DISPLAY_OPTIONS)
    }

    fun setPlaybackButtonMode(mode: PlaybackButtonMode) {
        this.playbackButtonMode = mode
        notifyItemRangeChanged(0, itemCount, PAYLOAD_PLAYBACK)
    }

    /**
     * Submits a list of episodes, optionally grouping them by date.
     * Uses AsyncListDiffer for background thread diff calculation.
     * Skips updates during drag & drop to prevent conflicts.
     */
    fun submitList(list: List<Episode>): List<FeedItem> {
        // Skip updates during drag & drop to prevent conflicts
        if (isDragAndDropActive) {
            Timber.tag("ADAPTER").d("submitList: skipped due to active drag & drop")
            return items
        }

        val newList = mutableListOf<FeedItem>()

        if (displayOptions.showDateHeaders) {
            val episodesByDate = list.groupBy { it.timestamp.getDateFromLong() }
            episodesByDate.forEach { (date, dateEpisodes) ->
                newList.add(FeedItem.DateHeader(date, dateEpisodes.size))
                dateEpisodes.forEach { newList.add(FeedItem.EpisodeItem(it)) }
            }
        } else {
            list.forEach { newList.add(FeedItem.EpisodeItem(it)) }
        }

        Timber.tag("ADAPTER").d("submitList: oldSize=${items.size}, newSize=${newList.size}")

        // AsyncListDiffer handles diff calculation on background thread
        differ.submitList(newList)
        return newList
    }

    override fun updatePlaybackState(playingEpisodeId: Int?, isPlaying: Boolean) {
        previousPlayingEpisodeId = currentPlayingEpisodeId
        currentPlayingEpisodeId = playingEpisodeId
        isCurrentlyPlaying = isPlaying

        // Update only the old and new track positions instead of entire list
        val positionsToUpdate = mutableListOf<Int>()

        // Find position of previous playing episode
        previousPlayingEpisodeId?.let { oldId ->
            val oldPosition = items.indexOfFirst { it is FeedItem.EpisodeItem && it.episode.id == oldId }
            if (oldPosition != -1) {
                positionsToUpdate.add(oldPosition)
            }
        }

        // Find position of current playing episode
        playingEpisodeId?.let { newId ->
            val newPosition = items.indexOfFirst { it is FeedItem.EpisodeItem && it.episode.id == newId }
            if (newPosition != -1 && newPosition !in positionsToUpdate) {
                positionsToUpdate.add(newPosition)
            }
        }

        // If track changed, update only specific positions
        if (positionsToUpdate.isNotEmpty() && previousPlayingEpisodeId != playingEpisodeId) {
            positionsToUpdate.forEach { position ->
                notifyItemChanged(position, PAYLOAD_PLAYBACK)
            }
            Timber.tag("ADAPTER").d("updatePlaybackState: updated positions $positionsToUpdate (old=$previousPlayingEpisodeId, new=$playingEpisodeId)")
        } else if (previousPlayingEpisodeId == playingEpisodeId) {
            // Same track, just play/pause state changed - update only current position
            playingEpisodeId?.let { id ->
                val position = items.indexOfFirst { it is FeedItem.EpisodeItem && it.episode.id == id }
                if (position != -1) {
                    notifyItemChanged(position, PAYLOAD_PLAYBACK)
                }
            }
        } else {
            // Fallback: update all items (should rarely happen)
            notifyItemRangeChanged(0, itemCount, PAYLOAD_PLAYBACK)
        }
    }

    fun currentEpisodes(): List<Episode> {
        return items.filterIsInstance<FeedItem.EpisodeItem>().map { it.episode }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val currentList = differ.currentList.toMutableList()
        if (fromPosition !in currentList.indices || toPosition !in currentList.indices) {
            Timber.tag("ADAPTER").e("moveItem: Index out of bounds: from=$fromPosition, to=$toPosition, size=${currentList.size}")
            return
        }

        val item = currentList.removeAt(fromPosition)
        currentList.add(toPosition, item)

        Timber.tag("ADAPTER").d("moveItem: moved from $fromPosition to $toPosition. New sequence IDs: ${currentList.filterIsInstance<FeedItem.EpisodeItem>().map { it.episode.id }}")

        // Update AsyncListDiffer with new order
        differ.submitList(currentList)
    }

    fun onDragAndDropFinished() {
        isDragAndDropActive = false
        Timber.tag("ADAPTER").d("onDragAndDropFinished: flag reset")
    }

    // 🔧 Internal update method for optimistic UI
    fun updateInternalItemState(episodeId: Int, update: (Episode) -> Episode) {
        val currentList = differ.currentList.toMutableList()
        val index = currentList.indexOfFirst { it is FeedItem.EpisodeItem && it.episode.id == episodeId }
        if (index != -1) {
            val oldItem = currentList[index] as FeedItem.EpisodeItem
            val newEpisode = update(oldItem.episode)
            currentList[index] = FeedItem.EpisodeItem(newEpisode)
            differ.submitList(currentList)
            
            // Determine what actually changed to send specific payloads
            if (newEpisode.isFavorite != oldItem.episode.isFavorite) {
                notifyItemChanged(index, PAYLOAD_FAVORITE)
            }
            if (newEpisode.isInQueue != oldItem.episode.isInQueue) {
                notifyItemChanged(index, PAYLOAD_QUEUE)
            }
            if (newEpisode.isDownloaded != oldItem.episode.isDownloaded) {
                notifyItemChanged(index, PAYLOAD_PLAYBACK)
            }
            if (newEpisode.hasListened != oldItem.episode.hasListened) {
                notifyItemChanged(index, PAYLOAD_LISTENED)
            }
        }
    }

    sealed class FeedItem {
        data class DateHeader(val date: String, val episodeCount: Int) : FeedItem()
        data class EpisodeItem(val episode: Episode) : FeedItem()
    }

    class DateHeaderViewHolder(private val binding: ItemEpisodeHeaderV2Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedItem.DateHeader) {
            binding.tvDateHeader.text = item.date
            binding.tvEpisodeCount.text = "${item.episodeCount} EPISODES"
        }
        companion object {
            fun create(parent: ViewGroup) = DateHeaderViewHolder(ItemEpisodeHeaderV2Binding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    class EpisodeViewHolder(
        private val binding: ItemEpisodeV2Binding, 
        private val adapter: EpisodeFeedAdapterV2
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private var currentEpisode: Episode? = null
        private val colorSurface = ContextCompat.getColor(itemView.context, R.color.colorSurface)
        private val colorBackground = ContextCompat.getColor(itemView.context, R.color.colorBackground)

        fun refreshCurrentEpisode(episode: Episode) {
            currentEpisode = episode
        }

        @SuppressLint("ClickableViewAccessibility")
        fun bind(episode: Episode, options: EpisodeDisplayOptions) {
            currentEpisode = episode
            binding.apply {
                tvEpisodeTitle.text = episode.title
                val size = episode.duration.getSizeFromTimeDuration()
                val duration = episode.duration.getTimeFromSeconds().checkLessThenHour()
                tvEpisodeMetadata.text = "$duration  ·  $size"
                
                ivCover.loadEpisodeImage(
                    url = episode.episodeImageUrl,
                    fallbackUrl = episode.channelImageUrl,
                    cornerRadius = 12 
                )
                
                updateVisibility(options)
                updateFavoriteButton(episode.isFavorite)
                updateQueueButton(episode.isInQueue)
                updatePlaybackButton()
                updateBackgroundAlpha()
                updateDragHandleVisibility()
                updateListenedState()
                
                // Set up touch listener for immediate drag starting
                dragHandle.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        adapter.itemZoneHandler.onStartDrag(this@EpisodeViewHolder)
                    }
                    false
                }
                
                // 🔧 ACTION BUTTONS CLICK LISTENERS (Stage 2)
                btnPlayback.setOnClickListener { 
                    when (adapter.playbackButtonMode) {
                        PlaybackButtonMode.PLAY_DOWNLOADED, 
                        PlaybackButtonMode.PLAY_STREAMING -> adapter.itemZoneHandler.playPauseStateChanger(episode)
                        PlaybackButtonMode.DOWNLOAD -> {
                            Toast.makeText(itemView.context, "Starting download: ${episode.title}", Toast.LENGTH_SHORT).show()
                            // Mock optimistic update for download start
                            adapter.updateInternalItemState(episode.id) { it.copy(isDownloaded = true) }
                            adapter.itemZoneHandler.downloadEpisode(episode)
                        }
                        PlaybackButtonMode.DELETE -> {
                            Toast.makeText(itemView.context, "Deleting episode: ${episode.title}", Toast.LENGTH_SHORT).show()
                            adapter.updateInternalItemState(episode.id) { it.copy(isDownloaded = false) }
                            adapter.itemZoneHandler.deleteEpisode(episode)
                        }
                    }
                }
                
                btnFavorite.setOnClickListener { 
                    // 🚀 TRULY Optimistic UI update: change the model inside adapter list
                    val currentStatus = currentEpisode?.isFavorite ?: episode.isFavorite
                    adapter.updateInternalItemState(episode.id) { it.copy(isFavorite = !currentStatus) }
                    adapter.itemZoneHandler.toggleEpisodeFavorite(episode)
                }
                
                btnQueue.setOnClickListener { 
                    // 🚀 TRULY Optimistic UI update: change the model inside adapter list
                    val currentStatus = currentEpisode?.isInQueue ?: episode.isInQueue
                    adapter.updateInternalItemState(episode.id) { it.copy(isInQueue = !currentStatus) }
                    adapter.itemZoneHandler.toggleEpisodeQueue(episode)
                }
                
                root.setOnClickListener { adapter.itemZoneHandler.navigateToEpisodeDetailScreen(episode) }
                root.setOnLongClickListener { 
                    adapter.itemZoneHandler.onEpisodeLongClick(episode, bindingAdapterPosition)
                    true
                }
            }
        }

        fun updateVisibility(options: EpisodeDisplayOptions) {
            binding.apply {
                ivCover.visibility = if (options.showImageCover) View.VISIBLE else View.GONE
                btnFavorite.visibility = if (options.showFavoriteButton) View.VISIBLE else View.GONE
                btnQueue.visibility = if (options.showQueueButton) View.VISIBLE else View.GONE
                
                val density = root.resources.displayMetrics.density
                val coverSize = if (options.isSpaceOptimized && !options.showMetadata) 40 else 52
                ivCover.layoutParams.width = (coverSize * density).toInt()
                ivCover.layoutParams.height = (coverSize * density).toInt()
                
                if (options.showMetadata) {
                    tvEpisodeMetadata.visibility = View.VISIBLE
                } else {
                    tvEpisodeMetadata.visibility = if (options.isSpaceOptimized) View.GONE else View.INVISIBLE
                }
                
                val padding = if (options.isCompactMode) 8 else 16
                container.setPadding(
                    container.paddingStart,
                    (padding * density).toInt(),
                    container.paddingEnd,
                    (padding * density).toInt()
                )
            }
        }

        fun updateFavoriteButton(isFavorite: Boolean) {
            val color = ContextCompat.getColor(itemView.context, if (isFavorite) R.color.colorPrimary else R.color.colorOnSurfaceVariant)
            binding.btnFavorite.setImageResource(if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_outline)
            binding.btnFavorite.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }

        fun updateQueueButton(isInQueue: Boolean) {
            val color = ContextCompat.getColor(itemView.context, if (isInQueue) R.color.colorPrimary else R.color.colorOnSurfaceVariant)
            binding.btnQueue.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }

        fun updatePlaybackButton() {
            val episode = currentEpisode ?: return
            val isCurrent = episode.id == adapter.currentPlayingEpisodeId
            
            // ARCHITECTURAL RULE: Use BLUE for downloaded episodes WITH progress. Use YELLOW/GOLD for others.
            val useBlueStyle = episode.isDownloaded && episode.stopListeningAt > 0

            when (adapter.playbackButtonMode) {
                PlaybackButtonMode.PLAY_DOWNLOADED, PlaybackButtonMode.PLAY_STREAMING -> {
                    binding.btnPlayback.clearColorFilter()
                    if (useBlueStyle) {
                        // Blue style for downloaded content with progress
                        binding.btnPlayback.setImageResource(if (isCurrent && adapter.isCurrentlyPlaying) R.drawable.ic_pause_circle_blue else R.drawable.ic_play_circle_blue)
                    } else {
                        // Standard gold style
                        binding.btnPlayback.setImageResource(if (isCurrent && adapter.isCurrentlyPlaying) R.drawable.ic_pause_circle_yellow else R.drawable.ic_play_circle_yellow)
                    }
                }
                PlaybackButtonMode.DOWNLOAD -> {
                    binding.btnPlayback.clearColorFilter()
                    binding.btnPlayback.setImageResource(R.drawable.ic_download_circle_yellow)
                }
                PlaybackButtonMode.DELETE -> {
                    binding.btnPlayback.clearColorFilter()
                    binding.btnPlayback.setImageResource(R.drawable.ic_delete_circle_red)
                }
            }
        }

        fun updateDragHandleVisibility() {
            val isVisible = adapter.itemZoneHandler.isDraggableFragment && adapter.dragHandleAlpha > 0f
            binding.dragHandle.apply {
                visibility = if (isVisible) View.VISIBLE else View.GONE
                alpha = adapter.dragHandleAlpha
            }
        }

        fun updateListenedState() {
            val episode = currentEpisode ?: return
            val isCurrent = episode.id == adapter.currentPlayingEpisodeId
            
            // Apply dimming for listened episodes in all fragments except QueueFragment
            // QueueFragment uses PLAY_STREAMING mode and removes listened episodes from queue
            // ARCHITECTURAL RULE: Current playing episode is NEVER dimmed.
            if (episode.hasListened && !isCurrent && adapter.playbackButtonMode != PlaybackButtonMode.PLAY_STREAMING) {
                binding.cardEpisode.alpha = 0.5f
            } else {
                binding.cardEpisode.alpha = 1.0f
            }
        }
        
        fun updateBackgroundAlpha() {
            val factor = adapter.itemsBackgroundFactor
            val blendedColor = blendColors(colorBackground, colorSurface, factor)
            binding.cardEpisode.setCardBackgroundColor(ColorStateList.valueOf(blendedColor))
        }
        
        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val r = (Color.red(color1) * inverseRatio + Color.red(color2) * ratio).toInt()
            val g = (Color.green(color1) * inverseRatio + Color.green(color2) * ratio).toInt()
            val b = (Color.blue(color1) * inverseRatio + Color.blue(color2) * ratio).toInt()
            return Color.rgb(r, g, b)
        }

        companion object {
            fun create(parent: ViewGroup, adapter: EpisodeFeedAdapterV2) = 
                EpisodeViewHolder(ItemEpisodeV2Binding.inflate(LayoutInflater.from(parent.context), parent, false), adapter)
        }
    }
}
