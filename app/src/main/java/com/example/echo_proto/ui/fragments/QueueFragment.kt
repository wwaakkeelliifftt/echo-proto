package com.example.echo_proto.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.data.local.prefs.EpisodeDisplayOptions
import com.example.echo_proto.databinding.FragmentQueueBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.*
import com.example.echo_proto.ui.dialogs.DisplaySettingsBottomSheet
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.ui.viewmodels.QueueViewModel
import com.example.echo_proto.util.getTimeFromSeconds
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.util.Resource
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class QueueFragment : Fragment(), ItemZoneTouchHandler {

    private var _binding: FragmentQueueBinding? = null
    private val binding get() = _binding!!

    private lateinit var queueAdapter: EpisodeFeedAdapterV2
    private val viewModel by viewModels<QueueViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private var itemTouchHelper: ItemTouchHelper? = null
    private var queueLockMenuItem: MenuItem? = null
    private var currentQueueCount: Int = 0
    private var currentQueueDurationSeconds: Int = 0
    private var lastQueueIdsSnapshot: List<Int> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentQueueBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        subscribeToObservers()
        viewModel.updateQueueRss()
        setupMenu()
        updateQueueSummary()
        observePlaybackState(mainViewModel, queueAdapter)
    }

    override fun onResume() {
        super.onResume()
        // Safety check: only refresh if connected
        val isConnected = mainViewModel.isConnected.value?.peekContent()?.data == true
        if (isConnected) {
            Timber.tag("PLAY").d("QueueFragment.onResume -> refreshPlayerPlaylist()")
            mainViewModel.refreshPlayerPlaylist()
        }
    }

    private fun subscribeToObservers() {
        viewModel.rssQueue.observe(viewLifecycleOwner) { queueList ->
            if (queueList.isNullOrEmpty()) {
                binding.containerEmptyQueue.visibility = View.VISIBLE
                queueAdapter.submitList(emptyList())
                updatePlayerPlaylistIfNeeded(emptyList())
            } else {
                binding.containerEmptyQueue.visibility = View.GONE
                queueAdapter.submitList(queueList)
                updatePlayerPlaylistIfNeeded(queueList)
            }
        }

        mainViewModel.isConnected.observe(viewLifecycleOwner) { event ->
            val resource = event.peekContent()
            if (resource is Resource.Success && resource.data == true) {
                Timber.tag("PLAY").d("QueueFragment: Media connected, refreshing playlist")
                updatePlayerPlaylistIfNeeded(viewModel.rssQueue.value ?: emptyList(), force = true)
            }
        }

        viewModel.queueCount.observe(viewLifecycleOwner) { count ->
            currentQueueCount = count
            updateQueueSummary()
        }

        viewModel.queueDurationSeconds.observe(viewLifecycleOwner) { totalSeconds ->
            currentQueueDurationSeconds = totalSeconds
            updateQueueSummary()
        }

        viewModel.isLockedQueue.observe(viewLifecycleOwner) { isLocked ->
            changeQueueLocker(isLocked = isLocked)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.displayOptions.collectLatest { options ->
                val queueSpecificOptions = options.copy(showDateHeaders = false)
                queueAdapter.updateDisplayOptions(queueSpecificOptions)
            }
        }
    }

    private fun updatePlayerPlaylistIfNeeded(queueList: List<Episode>, force: Boolean = false) {
        val newSnapshot = queueList.map { it.id }
        if (force || (newSnapshot != lastQueueIdsSnapshot && newSnapshot.isNotEmpty())) {
            lastQueueIdsSnapshot = newSnapshot
            
            val isConnected = mainViewModel.isConnected.value?.peekContent()?.data ?: false
            if (isConnected) {
                mainViewModel.updateQueueInService()
                mainViewModel.refreshPlayerPlaylist()
            } else {
                Timber.tag("PLAY").w("QueueFragment: Cannot update service playlist, not connected yet")
            }
        }
    }

    private fun setupRecyclerView() {
        queueAdapter = EpisodeFeedAdapterV2(
            this, 
            EpisodeDisplayOptions(showDateHeaders = false)
        )
        queueAdapter.setPlaybackButtonMode(EpisodeFeedAdapterV2.Companion.PlaybackButtonMode.PLAY_STREAMING)
        binding.recyclerView.apply {
            adapter = queueAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override val isDraggableFragment: Boolean = true

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    private fun changeQueueLocker(isLocked: Boolean) {
        queueAdapter.dragHandleAlpha = if (isLocked) 0f else 0.8f

        if (isLocked) {
            itemTouchHelper?.attachToRecyclerView(null)
            itemTouchHelper = null
        } else {
            itemTouchHelper = ItemTouchHelper(getSwipeCallback(requireContext(), viewModel, queueAdapter)).also {
                it.attachToRecyclerView(binding.recyclerView)
            }
        }
        queueAdapter.notifyDataSetChanged()
        updateQueueLockMenuIcon(isLocked)
    }

    private fun updateQueueLockMenuIcon(isLocked: Boolean) {
        val menuItem = queueLockMenuItem ?: return
        val iconRes = if (isLocked) R.drawable.ic_lock_close else R.drawable.ic_lock_open
        val tintColor = ContextCompat.getColor(
            requireContext(),
            if (isLocked) R.color.snackbar_error_background else R.color.green_light
        )

        val icon = ContextCompat.getDrawable(requireContext(), iconRes)?.mutate()
        if (icon != null) {
            DrawableCompat.setTint(icon, tintColor)
            menuItem.icon = icon
        } else {
            menuItem.setIcon(iconRes)
        }
    }

    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        viewModel.navigateToDetailWithSharedPref(episodeId = episode.id)
        findNavController().navigate(R.id.globalActionToEpisodeDetailFragment)
    }

    override fun playPauseStateChanger(episode: Episode) {
        mainViewModel.playOrToggleEpisode(mediaItem = episode, true)
    }

    override fun toggleEpisodeFavorite(episode: Episode) {
        mainViewModel.toggleEpisodeFavorite(episode)
    }

    override fun toggleEpisodeQueue(episode: Episode) {
        mainViewModel.toggleEpisodeQueue(episode)
    }

    override fun downloadEpisode(episode: Episode) {
        mainViewModel.downloadEpisode(episode)
    }

    override fun deleteEpisode(episode: Episode) {
        mainViewModel.deleteEpisode(episode)
    }

    override fun onEpisodeLongClick(episode: Episode, position: Int) {
        showItemActionDialog(position)
    }

    private fun showItemActionDialog(position: Int) {
        val options = arrayOf("Display Settings", "Clear Queue")
        AlertDialog.Builder(requireContext())
            .setTitle("Queue Actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openDisplaySettings()
                    1 -> {} // Clear queue logic
                }
            }
            .show()
    }

    private fun openDisplaySettings() {
        DisplaySettingsBottomSheet
            .newInstance(DisplaySettingsBottomSheet.QUEUE_SCREEN)
            .show(parentFragmentManager, DisplaySettingsBottomSheet.TAG)
    }

    private fun getSwipeCallback(context: Context, source: ViewModel, adapter: EpisodeFeedAdapterV2): SwipeToDeleteCallback_Queue {
        return object : SwipeToDeleteCallback_Queue(context = context, sourceViewModel = source, queueAdapter = adapter) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                viewModel.changeEpisodeInQueueStatus(
                    position = pos,
                    source = viewModel.rssQueue
                )
                queueAdapter.notifyItemRemoved(pos)
                Toast.makeText(requireContext(), "Removed from queue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateQueueSummary() {
        val formattedDuration = currentQueueDurationSeconds.getTimeFromSeconds()
        binding.tvQueueSummary.text = getString(
            R.string.queue_summary_template,
            currentQueueCount,
            formattedDuration
        )
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(queueMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val queueMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_queue, menu)
            queueLockMenuItem = menu.findItem(R.id.mabQueueFix)
            updateQueueLockMenuIcon(viewModel.isLockedQueue.value ?: true)
        }

        override fun onPrepareMenu(menu: Menu) {
            queueLockMenuItem = menu.findItem(R.id.mabQueueFix)
            updateQueueLockMenuIcon(viewModel.isLockedQueue.value ?: true)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabQueueFix -> {
                    viewModel.updateQueueLocker()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
