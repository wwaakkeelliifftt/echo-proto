package com.example.echo_proto.ui.adapters

import android.annotation.SuppressLint
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.ItemEpisodeBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.getTimeFromSeconds
import timber.log.Timber

//interface OnStartDragListener {
//    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
//    fun changeDragIconVisibilityAlpha(): Float
//}

interface ItemZoneTouchHandler {
    val isDraggableFragment: Boolean
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    fun changeDragIconVisibilityAlpha(): Float
    fun navigateToEpisodeDetailScreen(episode: Episode)
    fun playPauseStateChanger(episode: Episode)
}

class FeedAdapter(
//    private val dragListener: OnStartDragListener?,
    private val itemZoneHandler: ItemZoneTouchHandler?
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    inner class FeedViewHolder(val binding: ItemEpisodeBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val listDiffer = AsyncListDiffer(this, diffCallback)
    val actualList: MutableList<Episode> get() = listDiffer.currentList

    fun submitList(list: List<Episode>) {
        listDiffer.submitList(list)
        Timber.d("FEED_ADAPTER: submit listDiffer")
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listDiffer.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(
            ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val episode = listDiffer.currentList[position]
        val view = holder.binding

        with(view) {
            tvPubDateAndSize.text = episode.timestamp.getDateFromLong()
            tvTitle.text = episode.title
            tvTime.text = episode.duration.getTimeFromSeconds()

            ivSelected.visibility = if (episode.isSelected) View.VISIBLE else View.INVISIBLE
            ivQueue.visibility = if (episode.isInQueue) View.VISIBLE else View.INVISIBLE
            
            // Показываем иконку скачивания для скачанных эпизодов
            ivDownload.alpha = if (episode.isDownloaded) 1.0f else 0.2f
            
            // Приглушаем отображение прослушанных эпизодов
            if (episode.hasListened) {
                root.alpha = 0.5f
                tvTitle.alpha = 0.7f
                tvPubDateAndSize.alpha = 0.5f
                tvTime.alpha = 0.5f
            } else {
                root.alpha = 1.0f
                tvTitle.alpha = 1.0f
                tvPubDateAndSize.alpha = 1.0f
                tvTime.alpha = 1.0f
            }

            btnNavigateToEpisodeDetail.setOnClickListener {
                itemZoneHandler?.navigateToEpisodeDetailScreen(episode = episode)
                Timber.d("onEpisodeNavClick: episode=${episode.title}")
            }

            btnPlayPause.setOnClickListener { pp ->
                itemZoneHandler?.playPauseStateChanger(episode = episode)
            }

            if (itemZoneHandler?.isDraggableFragment == true) {
                dragAndDrop.setOnTouchListener { v, event ->
                    itemZoneHandler.onStartDrag(viewHolder = holder)
                    false
                }
            }

            dragAndDrop.setOnTouchListener { v, event ->
//                dragListener?.let { it.onStartDrag(holder) }
                itemZoneHandler?.onStartDrag(holder)
                false
            }
//            dragAndDrop.alpha = dragListener?.changeDragIconVisibilityAlpha() ?: 0f
            dragAndDrop.alpha = itemZoneHandler?.changeDragIconVisibilityAlpha() ?: 0f



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
