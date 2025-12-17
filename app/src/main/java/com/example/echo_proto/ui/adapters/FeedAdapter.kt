package com.example.echo_proto.ui.adapters

import android.annotation.SuppressLint
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.ItemEpisodeBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.common.PlaybackStateAware
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.getTimeFromSeconds
import timber.log.Timber


interface ItemZoneTouchHandler {
    val isDraggableFragment: Boolean
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    fun navigateToEpisodeDetailScreen(episode: Episode)
    fun playPauseStateChanger(episode: Episode)
}

class FeedAdapter(
    private val itemZoneHandler: ItemZoneTouchHandler?
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>(), PlaybackStateAware {

    companion object {
        // константы для частичного обновления
        const val PAYLOAD_SELECTED = "selected"
        const val PAYLOAD_QUEUE = "queue"
        const val PAYLOAD_DOWNLOADED = "downloaded"
        const val PAYLOAD_LISTENED = "listened"
        const val PAYLOAD_PLAYBACK = "playback"
    }

    class FeedViewHolder(val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root)

    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false
    var isActionModeActive: Boolean = false

    private val diffCallback = object : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val items = mutableListOf<Episode>()
    var dragHandleAlpha: Float = 0f
    val actualList: List<Episode> get() = items

    fun submitList(list: List<Episode>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = items.size
            override fun getNewListSize(): Int = list.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == list[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition] == list[newItemPosition]
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                val oldItem = items[oldItemPosition]
                val newItem = list[newItemPosition]
                val payload = mutableSetOf<String>()
                
                if (oldItem.isSelected != newItem.isSelected) payload.add(PAYLOAD_SELECTED)
                if (oldItem.isInQueue != newItem.isInQueue) payload.add(PAYLOAD_QUEUE)
                if (oldItem.isDownloaded != newItem.isDownloaded) payload.add(PAYLOAD_DOWNLOADED)
                if (oldItem.hasListened != newItem.hasListened) payload.add(PAYLOAD_LISTENED)
                
                return payload.ifEmpty { null }
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items.clear()
        items.addAll(list)
        diffResult.dispatchUpdatesTo(this)
        Timber.d("FEED_ADAPTER: submit size=${list.size}")
    }

    fun currentItems(): List<Episode> = items.toList()

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        if (fromPosition !in items.indices || toPosition !in items.indices) return
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * Обновляет информацию о том, какой эпизод сейчас играет и состояние воспроизведения.
     * Вызывать из фрагментов при изменении playbackState или текущего эпизода.
     */
    override fun updatePlaybackState(playingEpisodeId: Int?, isPlaying: Boolean) {
        val oldPlayingId = currentPlayingEpisodeId
        val oldIsPlaying = isCurrentlyPlaying
        
        // Если ничего не изменилось — не обновляем
        if (oldPlayingId == playingEpisodeId && oldIsPlaying == isPlaying) return
        
        currentPlayingEpisodeId = playingEpisodeId
        isCurrentlyPlaying = isPlaying
        
        // Обновляем только изменённые элементы
        val oldPosition = items.indexOfFirst { it.id == oldPlayingId }
        val newPosition = items.indexOfFirst { it.id == playingEpisodeId }
        
        if (oldPosition != -1) notifyItemChanged(oldPosition, PAYLOAD_PLAYBACK)
        if (newPosition != -1 && newPosition != oldPosition) notifyItemChanged(newPosition, PAYLOAD_PLAYBACK)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(
            ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        
        val episode = items[position]
        val view = holder.binding
        
        val allPayloads = mutableSetOf<String>()
        payloads.forEach { payload ->
            when (payload) {
                is Set<*> -> allPayloads.addAll(payload.filterIsInstance<String>())
                is String -> allPayloads.add(payload)
            }
        }
        
        with(view) {
            if (PAYLOAD_SELECTED in allPayloads) {
                ivSelected.visibility = if (episode.isSelected) View.VISIBLE else View.INVISIBLE
            }
            if (PAYLOAD_QUEUE in allPayloads) {
                ivQueue.alpha = if (episode.isInQueue) 1.0f else 0.2f
            }
            if (PAYLOAD_DOWNLOADED in allPayloads) {
                ivDownload.alpha = if (episode.isDownloaded) 1.0f else 0.2f
            }
            if (PAYLOAD_LISTENED in allPayloads) {
                viewListenedOverlay.isVisible = episode.hasListened
            }
            if (PAYLOAD_PLAYBACK in allPayloads) {
                val isCurrentEpisode = currentPlayingEpisodeId != null && episode.id == currentPlayingEpisodeId
                val iconRes = if (isCurrentEpisode && isCurrentlyPlaying) {
                    R.drawable.ic_rv_pause
                } else {
                    R.drawable.ic_rv_play
                }
                btnPlayPause.setImageResource(iconRes)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val episode = items[position]
        val view = holder.binding

        with(view) {
            tvPubDateAndSize.text = episode.timestamp.getDateFromLong()
            tvTitle.text = episode.title
            tvTime.text = episode.duration.getTimeFromSeconds()

            ivSelected.visibility = if (episode.isSelected) View.VISIBLE else View.INVISIBLE
            ivQueue.alpha = if (episode.isInQueue) 1.0f else 0.2f
            ivDownload.alpha = if (episode.isDownloaded) 1.0f else 0.2f
            
            viewListenedOverlay.isVisible = episode.hasListened

            // Отображение play/pause в списке в зависимости от текущего эпизода и состояния плеера
            val isCurrentEpisode = currentPlayingEpisodeId != null && episode.id == currentPlayingEpisodeId
            val iconRes = if (isCurrentEpisode && isCurrentlyPlaying) {
                R.drawable.ic_rv_pause
            } else {
                R.drawable.ic_rv_play
            }
            btnPlayPause.setImageResource(iconRes)

            btnNavigateToEpisodeDetail.setOnClickListener {
                if (!isActionModeActive) {
                    itemZoneHandler?.navigateToEpisodeDetailScreen(episode = episode)
                    Timber.d("onEpisodeNavClick: episode=${episode.title}")
                }
            }

            btnPlayPause.setOnClickListener {
                itemZoneHandler?.playPauseStateChanger(episode = episode)
            }

            dragAndDrop.setOnTouchListener { _, _ ->
                if (itemZoneHandler?.isDraggableFragment == true) {
                    itemZoneHandler.onStartDrag(holder)
                }
                false
            }
            dragAndDrop.alpha = if (itemZoneHandler?.isDraggableFragment == true) dragHandleAlpha else 0f

            // todo: possible to delete soon
            setClickListener {
                onItemClickListener?.invoke(episode)
            }
        }
    }

    private var onItemClickListener: ((Episode) -> Unit)? = null
    fun setClickListener(listener: (Episode) -> Unit) { onItemClickListener = listener }

    private fun animatePlayPauseRotation(btn: View?) {
        val button = btn as? ImageButton ?: return
        button.animate()
            .rotationBy(360f)
            .setDuration(666)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .start()
    }

}
