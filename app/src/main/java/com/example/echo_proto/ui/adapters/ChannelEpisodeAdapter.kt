package com.example.echo_proto.ui.adapters

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.ItemEpisodeV2Binding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.common.PlaybackStateAware
import com.example.echo_proto.util.checkLessThenHour
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.getTimeFromSeconds
import com.example.echo_proto.util.getSizeFromTimeDuration
import com.example.echo_proto.util.loadEpisodeImage
import timber.log.Timber

/**
 * Специализированный адаптер для фрагмента канала (ChannelFragment)
 * Использует дизайн V2 без заголовков дат.
 */
class ChannelEpisodeAdapter(
    private val itemZoneHandler: ItemZoneTouchHandler
) : ListAdapter<Episode, ChannelEpisodeAdapter.EpisodeViewHolder>(DiffCallback()), PlaybackStateAware {

    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeV2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EpisodeViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun updatePlaybackState(playingEpisodeId: Int?, isPlaying: Boolean) {
        val oldId = currentPlayingEpisodeId
        currentPlayingEpisodeId = playingEpisodeId
        isCurrentlyPlaying = isPlaying
        
        currentList.forEachIndexed { index, episode ->
            if (episode.id == oldId || episode.id == playingEpisodeId) {
                notifyItemChanged(index, "playback")
            }
        }
    }

    class EpisodeViewHolder(
        private val binding: ItemEpisodeV2Binding,
        private val adapter: ChannelEpisodeAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: Episode) {
            binding.apply {
                tvEpisodeTitle.text = episode.title
                val size = episode.duration.getSizeFromTimeDuration()
                val duration = episode.duration.getTimeFromSeconds().checkLessThenHour()
                val date = episode.timestamp.getDateFromLong()
                tvEpisodeMetadata.text = "$duration  ·  $size  ·  $date"
                dragHandle.visibility = View.INVISIBLE


                // Загрузка маленького превью (если нужно)
//                ivEpisodeImage.loadEpisodeImage(episode.episodeImageUrl, episode.channelImageUrl, size = 64)

                updateFavoriteButton(episode.isFavorite)
                updateQueueButton(episode.isInQueue)
                updatePlaybackButton(episode)
                
                root.setOnClickListener {
                    adapter.itemZoneHandler.navigateToEpisodeDetailScreen(episode)
                }
                
                btnPlayback.setOnClickListener {
                    adapter.itemZoneHandler.playPauseStateChanger(episode)
                }
            }
        }

        private fun updateFavoriteButton(isFavorite: Boolean) {
            val color = if (isFavorite) R.color.colorPrimary else R.color.colorOnSurfaceVariant
            binding.btnFavorite.drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(itemView.context, color), PorterDuff.Mode.SRC_ATOP
            )
        }

        private fun updateQueueButton(isInQueue: Boolean) {
            val color = if (isInQueue) R.color.colorPrimary else R.color.colorOnSurfaceVariant
            binding.btnQueue.drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(itemView.context, color), PorterDuff.Mode.SRC_ATOP
            )
        }

        private fun updatePlaybackButton(episode: Episode) {
            val isCurrent = adapter.currentPlayingEpisodeId == episode.id
            val icon = if (isCurrent && adapter.isCurrentlyPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle
            binding.btnPlayback.setImageResource(icon)
            
            val color = if (episode.hasListened) R.color.colorNocturneSand else R.color.colorNocturneSand
            binding.btnPlayback.drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(itemView.context, color), PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean = oldItem == newItem
    }
}
