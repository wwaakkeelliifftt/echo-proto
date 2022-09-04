package com.example.echo_proto.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.databinding.ItemEpisodeBinding
import com.example.echo_proto.domain.model.Episode
import timber.log.Timber

abstract class BaseAdapter(
    private val layoutId: Int
) : RecyclerView.Adapter<BaseAdapter.ViewHolder>() {

    inner class ViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<Episode>() {
        override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract var listDiffer: AsyncListDiffer<Episode>

    var episodesList: List<Episode>
        get() = listDiffer.currentList
        set(value) = listDiffer.submitList(value)

    fun submitList(list: List<Episode>) {
        listDiffer.submitList(list)
        Timber.d("FEED_ADAPTER: submit listDiffer")
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listDiffer.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId, parent, false
            )
        )
    }

    private var onItemClickListener: ((Episode) -> Unit)? = null
    fun setClickListener(listener: (Episode) -> Unit) { onItemClickListener = listener }
}