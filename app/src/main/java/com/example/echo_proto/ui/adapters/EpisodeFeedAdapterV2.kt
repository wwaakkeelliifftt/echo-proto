package com.example.echo_proto.ui.adapters

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
import com.example.echo_proto.ui.common.PlaybackStateAware
import com.example.echo_proto.domain.model.Episode as EpisodeFromDatabase
import com.example.echo_proto.util.*
import timber.log.Timber

/**
 * Adapter for Episode Feed V2 with date headers and episode items
 */
class EpisodeFeedAdapterV2(
    private val itemZoneHandler: ItemZoneTouchHandler
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PlaybackStateAware {
    
    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false
    private var displayOptions: EpisodeDisplayOptions = EpisodeDisplayOptions()
    
    private val items = mutableListOf<FeedItem>()
    var dragHandleAlpha: Float = 0f
    var isActionModeActive: Boolean = false
    val actualList: List<FeedItem> get() = items

    companion object {
        const val TYPE_DATE_HEADER = 0
        const val TYPE_EPISODE = 1
        const val PAYLOAD_PLAYBACK = "playback"
        const val PAYLOAD_SELECTED = "selected"
        const val PAYLOAD_QUEUE = "queue"
        const val PAYLOAD_DISPLAY_OPTIONS = "display_options"
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FeedItem.DateHeader -> TYPE_DATE_HEADER
            is FeedItem.Episode -> TYPE_EPISODE
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
            is FeedItem.Episode -> (holder as EpisodeViewHolder).bind(item.episode, displayOptions)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = items[position]
            if (item is FeedItem.Episode && holder is EpisodeViewHolder) {
                val payloadSet = (payloads.firstOrNull() as? Set<*>) ?: payloads.toSet()
                
                if (payloadSet.contains(PAYLOAD_DISPLAY_OPTIONS)) {
                    holder.updateVisibility(displayOptions)
                }
                if (payloadSet.contains(PAYLOAD_QUEUE)) {
                    holder.updateQueueButton(item.episode.isInQueue)
                }
                if (payloadSet.contains(PAYLOAD_PLAYBACK)) {
                    holder.updatePlaybackButton(item.episode.hasListened)
                }
            }
        }
    }

    fun updateDisplayOptions(newOptions: EpisodeDisplayOptions) {
        val oldOptions = this.displayOptions
        this.displayOptions = newOptions
        
        // If showDateHeaders changed, we need a full re-submit of items
        if (oldOptions.showDateHeaders != newOptions.showDateHeaders) {
            // This is handled by the submitList call in Fragment
        } else {
            notifyItemRangeChanged(0, itemCount, PAYLOAD_DISPLAY_OPTIONS)
        }
    }

    fun submitFeedItems(list: List<EpisodeFromDatabase>): List<FeedItem>  {
        val oldList = ArrayList(items)
        val newList = mutableListOf<FeedItem>()

        if (displayOptions.showDateHeaders) {
            // Group episodes by date
            val episodesByDate = list.groupBy { it.timestamp.getDateFromLong() }
            episodesByDate.forEach { (date, dateEpisodes) ->
                newList.add(FeedItem.DateHeader(date, dateEpisodes.size))
                dateEpisodes.forEach { newList.add(FeedItem.Episode(it)) }
            }
        } else {
            // Just add episodes without headers
            list.forEach { newList.add(FeedItem.Episode(it)) }
        }
        
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return when {
                    oldItem is FeedItem.DateHeader && newItem is FeedItem.DateHeader -> oldItem.date == newItem.date
                    oldItem is FeedItem.Episode && newItem is FeedItem.Episode -> oldItem.episode.id == newItem.episode.id
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

    sealed class FeedItem {
        data class DateHeader(val date: String, val episodeCount: Int) : FeedItem()
        data class Episode(val episode: EpisodeFromDatabase) : FeedItem()
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

    class EpisodeViewHolder(private val binding: ItemEpisodeV2Binding, private val adapter: EpisodeFeedAdapterV2) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var currentEpisode: EpisodeFromDatabase

        fun bind(episode: EpisodeFromDatabase, options: EpisodeDisplayOptions) {
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
                updatePlaybackButton(episode.hasListened)
                setupClickListeners(episode)
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
                
                root.requestLayout()
            }
        }

        fun updateFavoriteButton(isFavorite: Boolean) {
            binding.btnFavorite.drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(itemView.context, if (isFavorite) R.color.colorPrimary else R.color.colorOnSurfaceVariant),
                PorterDuff.Mode.SRC_ATOP
            )
        }

        fun updateQueueButton(isInQueue: Boolean) {
            binding.btnQueue.drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(itemView.context, if (isInQueue) R.color.colorPrimary else R.color.colorOnSurfaceVariant),
                PorterDuff.Mode.SRC_ATOP
            )
        }

        fun updatePlaybackButton(hasListened: Boolean) {
            binding.btnPlayback.apply {
                val isCurrent = currentEpisode.id == adapter.currentPlayingEpisodeId
                setImageResource(if (isCurrent && adapter.isCurrentlyPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle)
                drawable.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.colorAccentBlue),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }

        private fun setupClickListeners(episode: EpisodeFromDatabase) {
            binding.btnPlayback.setOnClickListener { adapter.itemZoneHandler.playPauseStateChanger(episode) }
        }

        companion object {
            fun create(parent: ViewGroup, adapter: EpisodeFeedAdapterV2) = EpisodeViewHolder(ItemEpisodeV2Binding.inflate(LayoutInflater.from(parent.context), parent, false), adapter)
        }
    }
}
