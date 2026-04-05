package com.example.echo_proto.ui.adapters

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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

/**
 * Специализированный адаптер для фрагмента канала (ChannelFragment)
 * Использует дизайн V2 без заголовков дат.
 */
class ChannelEpisodeAdapter(
    private val itemZoneHandler: ItemZoneTouchHandler
) : ListAdapter<Episode, ChannelEpisodeAdapter.EpisodeViewHolder>(DiffCallback()), PlaybackStateAware {

    private var currentPlayingEpisodeId: Int? = null
    private var isCurrentlyPlaying: Boolean = false
    
    // анимация фона (0.0 - черный, 1.0 - серый/urface)
    var itemsBackgroundFactor: Float = 0f
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, "background_alpha")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeV2Binding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EpisodeViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("background_alpha")) {
            holder.updateBackgroundAlpha()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
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

        private val colorSurface = ContextCompat.getColor(itemView.context, R.color.colorSurface)
        private val colorBlack = ContextCompat.getColor(itemView.context, R.color.colorBackground)

        fun bind(episode: Episode) {
            binding.apply {
                tvEpisodeTitle.text = episode.title
                val size = episode.duration.getSizeFromTimeDuration()
                val duration = episode.duration.getTimeFromSeconds().checkLessThenHour()
                val date = episode.timestamp.getDateFromLong()
                tvEpisodeMetadata.text = "$duration  ·  $size  ·  $date"
                dragHandle.visibility = View.INVISIBLE

                updateBackgroundAlpha()
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

        fun updateBackgroundAlpha() {
            val factor = adapter.itemsBackgroundFactor
            val blendedColor = blendColors(colorBlack, colorSurface, factor)
            binding.cardEpisode.setCardBackgroundColor(ColorStateList.valueOf(blendedColor))
        }

        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val inverseRatio = 1f - ratio
            val r = (ColorPalette.red(color1) * inverseRatio + ColorPalette.red(color2) * ratio).toInt()
            val g = (ColorPalette.green(color1) * inverseRatio + ColorPalette.green(color2) * ratio).toInt()
            val b = (ColorPalette.blue(color1) * inverseRatio + ColorPalette.blue(color2) * ratio).toInt()
            return ColorPalette.rgb(r, g, b)
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
            
            val color = R.color.colorNocturneSand
            binding.btnPlayback.drawable.colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(itemView.context, color), PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean = oldItem == newItem
    }

    object ColorPalette {
        fun red(color: Int): Int = (color shr 16) and 0xff
        fun green(color: Int): Int = (color shr 8) and 0xff
        fun blue(color: Int): Int = color and 0xff
        fun rgb(r: Int, g: Int, b: Int): Int = (0xff shl 24) or (r shl 16) or (g shl 8) or b
    }
}
