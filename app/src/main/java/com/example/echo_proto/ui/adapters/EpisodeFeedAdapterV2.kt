package com.example.echo_proto.ui.adapters

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.ItemEpisodeHeaderV2Binding
import com.example.echo_proto.databinding.ItemEpisodeV2Binding
import com.example.echo_proto.ui.adapters.FeedAdapter.Companion.PAYLOAD_PLAYBACK
import com.example.echo_proto.ui.common.PlaybackStateAware
import com.example.echo_proto.domain.model.Episode as EpisodeFromDatabase
import com.example.echo_proto.util.checkLessThenHour
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.getSizeFromTimeDuration
import com.example.echo_proto.util.getTimeFromSeconds
import timber.log.Timber

/**
 * Adapter for Episode Feed V2 with date headers and episode items
 */
class EpisodeFeedAdapterV2(
    private val itemZoneHandler: ItemZoneTouchHandler
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), PlaybackStateAware {
    
    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false
    
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
        const val PAYLOAD_DOWNLOADED = "downloaded"
        const val PAYLOAD_LISTENED = "listened"
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
            is FeedItem.Episode -> (holder as EpisodeViewHolder).bind(item.episode)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = items[position]
            if (item is FeedItem.Episode && holder is EpisodeViewHolder) {
                val payloadSet = payloads.firstOrNull() as? Set<*>
                if (payloadSet != null) {
                    if (payloadSet.contains(PAYLOAD_SELECTED)) {
                        // UI for selection if needed
                    }
                    if (payloadSet.contains(PAYLOAD_QUEUE)) {
                        holder.updateQueueButton(item.episode.isInQueue)
                    }
                    if (payloadSet.contains(PAYLOAD_DOWNLOADED)) {
                        // UI for downloaded if needed
                    }
                    if (payloadSet.contains(PAYLOAD_LISTENED)) {
                        holder.updatePlaybackButton(item.episode.hasListened)
                    }
                    if (payloadSet.contains(PAYLOAD_PLAYBACK)) {
                        holder.updatePlaybackButton(item.episode.hasListened)
                    }
                } else if (payloads.contains(PAYLOAD_PLAYBACK)) {
                     holder.updatePlaybackButton(item.episode.hasListened)
                }
            }
        }
    }

    fun submitFeedItems(list: List<EpisodeFromDatabase>): List<FeedItem>  {
        // 🔧 FIX: Take a snapshot of the current list for DiffUtil
        val oldList = ArrayList(items)
        val newList = mutableListOf<FeedItem>()

        // Group episodes by date
        val episodesByDate = list.groupBy { episode ->
            episode.timestamp.getDateFromLong()
        }
        
        episodesByDate.forEach { (date, dateEpisodes) ->
            newList.add(FeedItem.DateHeader(date, dateEpisodes.size))
            dateEpisodes.forEach { episode ->
                newList.add(FeedItem.Episode(episode))
            }
        }
        
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                
                return when {
                    oldItem is FeedItem.DateHeader && newItem is FeedItem.DateHeader -> 
                        oldItem.date == newItem.date
                    oldItem is FeedItem.Episode && newItem is FeedItem.Episode -> 
                        oldItem.episode.id == newItem.episode.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                val payload = mutableSetOf<String>()
                
                if (oldItem is FeedItem.Episode && newItem is FeedItem.Episode) {
                    if (oldItem.episode.isSelected != newItem.episode.isSelected) payload.add(PAYLOAD_SELECTED)
                    if (oldItem.episode.isInQueue != newItem.episode.isInQueue) payload.add(PAYLOAD_QUEUE)
                    if (oldItem.episode.isDownloaded != newItem.episode.isDownloaded) payload.add(PAYLOAD_DOWNLOADED)
                    if (oldItem.episode.hasListened != newItem.episode.hasListened) payload.add(PAYLOAD_LISTENED)
                }
                
                return payload.ifEmpty { null }
            }
        }
        
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        // Update the internal list AFTER calculating the diff
        items.clear()
        items.addAll(newList)
        
        // Dispatch updates to the adapter
        diffResult.dispatchUpdatesTo(this)
        
        Timber.d("FEED_ADAPTER_V2: submit size=${list.size}, items size=${items.size}")
        return newList
    }

    override fun updatePlaybackState(playingEpisodeId: Int?, isPlaying: Boolean) {
        val oldPlayingId = currentPlayingEpisodeId
        val oldIsPlaying = isCurrentlyPlaying
        
        if (oldPlayingId == playingEpisodeId && oldIsPlaying == isPlaying) return
        
        currentPlayingEpisodeId = playingEpisodeId
        isCurrentlyPlaying = isPlaying
        
        val oldPosition = items.indexOfFirst { 
            (it as? FeedItem.Episode)?.episode?.id == oldPlayingId 
        }
        val newPosition = items.indexOfFirst { 
            (it as? FeedItem.Episode)?.episode?.id == playingEpisodeId 
        }
        
        if (oldPosition != -1) notifyItemChanged(oldPosition, PAYLOAD_PLAYBACK)
        if (newPosition != -1 && newPosition != oldPosition) notifyItemChanged(newPosition, PAYLOAD_PLAYBACK)
    }

    /**
     * Sealed class for different feed item types
     */
    sealed class FeedItem {
        data class DateHeader(
            val date: String,
            val episodeCount: Int
        ) : FeedItem()

        data class Episode(
            val episode: EpisodeFromDatabase
        ) : FeedItem()
    }

    /**
     * ViewHolder for date header items
     */
    class DateHeaderViewHolder(
        private val binding: ItemEpisodeHeaderV2Binding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FeedItem.DateHeader) {
            binding.apply {
                tvDateHeader.text = item.date
                tvEpisodeCount.text = "${item.episodeCount} EPISODES"
            }
        }

        companion object {
            fun create(parent: ViewGroup): DateHeaderViewHolder {
                val binding = ItemEpisodeHeaderV2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return DateHeaderViewHolder(binding)
            }
        }
    }

    /**
     * ViewHolder for episode items
     */
    class EpisodeViewHolder(
        private val binding: ItemEpisodeV2Binding,
        private val adapter: EpisodeFeedAdapterV2
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var currentEpisode: EpisodeFromDatabase

        fun bind(episode: EpisodeFromDatabase) {
            currentEpisode = episode
            binding.apply {
                tvEpisodeTitle.text = episode.title
                val size = episode.duration.getSizeFromTimeDuration()
                val duration = episode.duration.getTimeFromSeconds().checkLessThenHour()
                tvEpisodeMetadata.text = "$duration  ·  $size"
                
                updateFavoriteButton(episode.isFavorite)
                updateQueueButton(episode.isInQueue)
                updatePlaybackButton(episode.hasListened)
                updateDragHandle()
                setupClickListeners(episode)
            }
        }

        fun updateFavoriteButton(isFavorite: Boolean) {
            binding.btnFavorite.apply {
                drawable.colorFilter = if (isFavorite) {
                    PorterDuffColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.colorPrimary), 
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    PorterDuffColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.colorOnSurfaceVariant), 
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
        }

        fun updateQueueButton(isInQueue: Boolean) {
            binding.btnQueue.apply {
                drawable.colorFilter = if (isInQueue) {
                    PorterDuffColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.colorPrimary), 
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    PorterDuffColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.colorOnSurfaceVariant), 
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
        }

        fun updatePlaybackButton(hasListened: Boolean) {
            binding.btnPlayback.apply {
                val isCurrentEpisode = adapter.currentPlayingEpisodeId != null && currentEpisode.id == adapter.currentPlayingEpisodeId
                val iconRes = if (isCurrentEpisode && adapter.isCurrentlyPlaying) {
                    R.drawable.ic_pause_circle
                } else {
                    R.drawable.ic_play_circle
                }
                setImageResource(iconRes)
                // Use colorNocturneSand for all states as in original code, or customize
                drawable.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.colorNocturneSand), 
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }

        private fun updateDragHandle() {
            binding.dragHandle.alpha = if (adapter.itemZoneHandler.isDraggableFragment) {
                adapter.dragHandleAlpha
            } else {
                0f
            }
        }

        private fun setupClickListeners(episode: EpisodeFromDatabase) {
            binding.btnFavorite.setOnClickListener {
                // TODO: Toggle favorite state
            }
            
            binding.btnQueue.setOnClickListener {
                // TODO: Toggle queue state
            }
            
            binding.btnPlayback.setOnClickListener {
                if (!adapter.isActionModeActive) {
                    adapter.itemZoneHandler.playPauseStateChanger(episode)
                    Timber.d("onPlayPauseClick: episode=${episode.title}")
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup, adapter: EpisodeFeedAdapterV2): EpisodeViewHolder {
                val binding = ItemEpisodeV2Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return EpisodeViewHolder(binding, adapter)
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    class DiffCallback : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return when {
                oldItem is FeedItem.DateHeader && newItem is FeedItem.DateHeader -> 
                    oldItem.date == newItem.date
                oldItem is FeedItem.Episode && newItem is FeedItem.Episode -> 
                    oldItem.episode.id == newItem.episode.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return when {
                oldItem is FeedItem.DateHeader && newItem is FeedItem.DateHeader -> 
                    oldItem == newItem
                oldItem is FeedItem.Episode && newItem is FeedItem.Episode -> 
                    oldItem.episode == newItem.episode
                else -> false
            }
        }
    }
}
