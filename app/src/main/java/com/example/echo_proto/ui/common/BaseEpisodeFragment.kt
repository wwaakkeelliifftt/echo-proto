package com.example.echo_proto.ui.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.ItemZoneTouchHandler
import com.example.echo_proto.ui.viewmodels.MainViewModel

/**
 * Base Fragment for all screens displaying episode lists.
 * Implements standard ItemZoneTouchHandler logic to avoid code duplication.
 */
abstract class BaseEpisodeFragment : Fragment(), ItemZoneTouchHandler {

    protected val mainViewModel by activityViewModels<MainViewModel>()

    // --- ItemZoneTouchHandler Default Implementation ---

    override val isDraggableFragment: Boolean = false

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        // Default: do nothing, override in fragments that support drag & drop
    }

    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        mainViewModel.navigateToDetailWithSharedPref(episode.id)
        findNavController().navigate(R.id.globalActionToEpisodeDetailFragment)
    }

    override fun playPauseStateChanger(episode: Episode) {
        mainViewModel.playOrToggleEpisode(episode, true)
    }

    override fun toggleEpisodeFavorite(episode: Episode) {
        mainViewModel.toggleEpisodeFavorite(episode)
    }

    override fun toggleEpisodeQueue(episode: Episode) {
        mainViewModel.toggleEpisodeQueue(episode)
    }

    override fun toggleEpisodeQueueInQueueFragment(episode: Episode) {
        // Default: do nothing, override in QueueFragment
    }

    override fun downloadEpisode(episode: Episode) {
        mainViewModel.downloadEpisode(episode)
    }

    override fun deleteEpisode(episode: Episode) {
        mainViewModel.deleteEpisode(episode)
    }

    // Abstract because every screen has its own DisplaySettings context
    abstract override fun onEpisodeLongClick(episode: Episode, position: Int)
}
