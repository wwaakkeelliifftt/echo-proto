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
//    private val dragListener: OnStartDragListener?,
    private val itemZoneHandler: ItemZoneTouchHandler?
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>(), PlaybackStateAware {

    inner class FeedViewHolder(val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root)

    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false

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
        currentPlayingEpisodeId = playingEpisodeId
        isCurrentlyPlaying = isPlaying
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(
            ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
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
            Timber.tag("FEED").d("episode=${episode.title.subSequence(0, 10)}, isVisible=${episode.hasListened}")

            // Отображение play/pause в списке в зависимости от текущего эпизода и состояния плеера
            val isCurrentEpisode = currentPlayingEpisodeId != null && episode.id == currentPlayingEpisodeId
            val iconRes = if (isCurrentEpisode && isCurrentlyPlaying) {
                R.drawable.ic_rv_pause
            } else {
                R.drawable.ic_rv_play
            }
            btnPlayPause.setImageResource(iconRes)

            btnNavigateToEpisodeDetail.setOnClickListener {
                itemZoneHandler?.navigateToEpisodeDetailScreen(episode = episode)
                Timber.d("onEpisodeNavClick: episode=${episode.title}")
            }

            btnPlayPause.setOnClickListener { pp ->
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
