package com.example.echo_proto.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.data.local.prefs.EpisodeDisplayOptions
import com.example.echo_proto.databinding.ItemEpisodeHeaderV2Binding
import com.example.echo_proto.databinding.ItemEpisodeV2Binding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.common.PlaybackStateAware
import com.example.echo_proto.util.*

/**
 * Unified Adapter for Episode Feed V2.
 * Supports date headers, background animations, and various display options.
 */
class EpisodeFeedAdapterV2(
    private val itemZoneHandler: ItemZoneTouchHandler
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PlaybackStateAware {
    
    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false
    private var displayOptions: EpisodeDisplayOptions = EpisodeDisplayOptions()
    
    // Background animation support
    var itemsBackgroundFactor: Float = 0f
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_BACKGROUND)
        }
    
    // Drag handle alpha support
    var dragHandleAlpha: Float = 0f
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_DRAG_ALPHA)
        }
    
    private val items = mutableListOf<FeedItem>()
    var isActionModeActive: Boolean = false
    val actualList: List<FeedItem> get() = items

    companion object {
        const val TYPE_DATE_HEADER = 0
        const val TYPE_EPISODE = 1
        
        const val PAYLOAD_PLAYBACK = "playback"
        const val PAYLOAD_SELECTED = "selected"
        const val PAYLOAD_QUEUE = "queue"
        const val PAYLOAD_DISPLAY_OPTIONS = "display_options"
        const val PAYLOAD_BACKGROUND = "background_alpha"
        const val PAYLOAD_DRAG_ALPHA = "drag_alpha"
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
                
                if (payloadSet.contains(PAYLOAD_DISPLAY_OPTIONS)) holder.updateVisibility(displayOptions)
                if (payloadSet.contains(PAYLOAD_QUEUE)) holder.updateQueueButton(item.episode.isInQueue)
                if (payloadSet.contains(PAYLOAD_PLAYBACK)) holder.updatePlaybackButton()
                if (payloadSet.contains(PAYLOAD_BACKGROUND)) holder.updateBackgroundAlpha()
                if (payloadSet.contains(PAYLOAD_DRAG_ALPHA)) holder.updateDragHandleVisibility()
            }
        }
    }

    fun updateDisplayOptions(newOptions: EpisodeDisplayOptions) {
        this.displayOptions = newOptions
        notifyItemRangeChanged(0, itemCount, PAYLOAD_DISPLAY_OPTIONS)
    }

    /**
     * Submits a list of episodes, optionally grouping them by date.
     */
    fun submitList(list: List<Episode>): List<FeedItem> {
        val oldList = ArrayList(items)
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
        
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return when {
                    oldItem is FeedItem.DateHeader && newItem is FeedItem.DateHeader -> oldItem.date == newItem.date
                    oldItem is FeedItem.EpisodeItem && newItem is FeedItem.EpisodeItem -> oldItem.episode.id == newItem.episode.id
                    else -> false
                }
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = oldList[oldItemPosition] == newList[newItemPosition]
        }
        
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
        return newList
    }

    override fun updatePlaybackState(playingEpisodeId: Int?, isPlaying: Boolean) {
        currentPlayingEpisodeId = playingEpisodeId
        isCurrentlyPlaying = isPlaying
        notifyItemRangeChanged(0, itemCount, PAYLOAD_PLAYBACK)
    }

    fun currentEpisodes(): List<Episode> {
        return items.filterIsInstance<FeedItem.EpisodeItem>().map { it.episode }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        if (fromPosition !in items.indices || toPosition !in items.indices) return
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
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
                
                btnPlayback.setOnClickListener { adapter.itemZoneHandler.playPauseStateChanger(episode) }
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
            binding.btnFavorite.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }

        fun updateQueueButton(isInQueue: Boolean) {
            val color = ContextCompat.getColor(itemView.context, if (isInQueue) R.color.colorPrimary else R.color.colorOnSurfaceVariant)
            binding.btnQueue.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }

        fun updatePlaybackButton() {
            val isCurrent = currentEpisode?.id == adapter.currentPlayingEpisodeId
            binding.btnPlayback.setImageResource(if (isCurrent && adapter.isCurrentlyPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle)
            binding.btnPlayback.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorAccentBlue), PorterDuff.Mode.SRC_ATOP)
        }

        fun updateDragHandleVisibility() {
            val isVisible = adapter.itemZoneHandler.isDraggableFragment && adapter.dragHandleAlpha > 0f
            binding.dragHandle.apply {
                visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                alpha = adapter.dragHandleAlpha
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
