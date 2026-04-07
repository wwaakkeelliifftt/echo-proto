package com.example.echo_proto.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentQueueBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.*
import com.example.echo_proto.ui.dialogs.DisplaySettingsBottomSheet
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.ui.viewmodels.QueueViewModel
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.getTimeFromSeconds
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.echo_proto.ui.common.observePlaybackState
import kotlinx.coroutines.flow.collectLatest

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
    private var pendingHandleAnimation = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentQueueBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        subscribeToObservers()
        setupRecyclerView()
        viewModel.updateQueueRss()
        setupMenu()
        updateQueueSummary()
        observePlaybackState(mainViewModel, queueAdapter)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshPlayerPlaylist()
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

        viewModel.queueCount.observe(viewLifecycleOwner) { count ->
            currentQueueCount = count
            updateQueueSummary()
        }

        viewModel.queueDurationSeconds.observe(viewLifecycleOwner) { totalSeconds ->
            currentQueueDurationSeconds = totalSeconds
            updateQueueSummary()
        }

        viewModel.isLockedQueue.observe(viewLifecycleOwner) { isLocked ->
            val animate = pendingHandleAnimation
            pendingHandleAnimation = false
            changeQueueLocker(isLocked = isLocked, animateHandles = animate)
        }

        // Observe display options
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.displayOptions.collectLatest { options ->
                val oldHasHeaders = queueAdapter.actualList.any { it is EpisodeFeedAdapterV2.FeedItem.DateHeader }
                val newHasHeaders = options.showDateHeaders
                
                queueAdapter.updateDisplayOptions(options)
                
                // If grouping changed, we MUST re-submit the list to rebuild FeedItems
                if (oldHasHeaders != newHasHeaders) {
                    viewModel.rssQueue.value?.let { list ->
                        queueAdapter.submitList(list)
                    }
                }
            }
        }
    }

    private fun animateDragHandles(isLocked: Boolean) {
        val targetMidScale = 2f
        val finalScale = if (isLocked) 0f else 1f
        val startScale = if (isLocked) 1f else 0f
        val startAlpha = if (isLocked) 0.8f else 0.15f
        val finalAlpha = if (isLocked) 0.15f else 0.8f
        binding.recyclerView.post {
            binding.recyclerView.children.forEach { child ->
                val handle = child.findViewById<View>(R.id.dragHandle) ?: return@forEach
                handle.animate().cancel()
                handle.alpha = startAlpha
                handle.scaleX = startScale
                handle.scaleY = startScale
                handle.animate()
                    .scaleX(targetMidScale)
                    .scaleY(targetMidScale)
                    .setDuration(200)
                    .withEndAction {
                        handle.animate()
                            .scaleX(finalScale)
                            .scaleY(finalScale)
                            .alpha(finalAlpha)
                            .setDuration(200)
                            .start()
                    }
                    .start()
            }
        }
    }

    private fun updatePlayerPlaylistIfNeeded(queueList: List<Episode>) {
        val newSnapshot = queueList.map { it.id }
        if (newSnapshot != lastQueueIdsSnapshot) {
            lastQueueIdsSnapshot = newSnapshot
            mainViewModel.refreshPlayerPlaylist()
        }
    }

    private fun setupRecyclerView() {
        queueAdapter = EpisodeFeedAdapterV2(this)
        binding.recyclerView.apply {
            adapter = queueAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                syncHandleAlpha(view)
            }
            override fun onChildViewDetachedFromWindow(view: View) {}
        })
    }

    private fun syncHandleAlpha(child: View? = null) {
        val alpha = queueAdapter.dragHandleAlpha
        if (child != null) {
            child.findViewById<View>(R.id.dragHandle)?.alpha = alpha
        } else {
            binding.recyclerView.children.forEach { item ->
                item.findViewById<View>(R.id.dragHandle)?.alpha = alpha
            }
        }
    }

    override val isDraggableFragment: Boolean = true

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    private fun changeQueueLocker(isLocked: Boolean, animateHandles: Boolean = false) {
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
        if (animateHandles) {
            animateDragHandles(isLocked)
        }
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

    override fun onEpisodeLongClick(episode: Episode, position: Int) {
        showItemActionDialog(position)
    }

    private fun showItemActionDialog(position: Int) {
        val options = arrayOf("Display Settings", "Multiple Choice Selection")
        AlertDialog.Builder(requireContext())
            .setTitle("Episode Actions")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openDisplaySettings()
                    1 -> startSelectionMode(position)
                }
            }
            .show()
    }

    private fun openDisplaySettings() {
        DisplaySettingsBottomSheet
            .newInstance(DisplaySettingsBottomSheet.QUEUE_SCREEN)
            .show(parentFragmentManager, DisplaySettingsBottomSheet.TAG)
    }

    // todo: need implement
    private fun startSelectionMode(position: Int) = 100500

    private fun getSwipeCallback(context: Context, source: ViewModel, adapter: EpisodeFeedAdapterV2): SwipeToDeleteCallback_Queue {
        return object : SwipeToDeleteCallback_Queue(context = context, sourceViewModel = source, queueAdapter = adapter) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                viewModel.changeEpisodeInQueueStatus(
                    position = pos,
                    source = viewModel.rssQueue
                )
                queueAdapter.notifyItemRemoved(pos)
                Toast.makeText(requireContext(), "Remove from queue, pos = $pos", Toast.LENGTH_SHORT).show()
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
            configureSearch(menu)
        }

        override fun onPrepareMenu(menu: Menu) {
            queueLockMenuItem = menu.findItem(R.id.mabQueueFix)
            updateQueueLockMenuIcon(viewModel.isLockedQueue.value ?: true)
            configureSearch(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabQueueFix -> {
                    pendingHandleAnimation = true
                    viewModel.updateQueueLocker()
                    true
                }
                else -> false
            }
        }

        private fun configureSearch(menu: Menu) {
            val searchItem = menu.findItem(R.id.mabQueueSearch)
            val searchView = searchItem?.actionView as? SearchView ?: return
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrEmpty()) {
                        viewModel.searchByQuery(query)
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    when {
                        !newText.isNullOrEmpty() -> viewModel.searchByQuery(newText)
                        newText == "" -> viewModel.updateQueueRss()
                    }
                    return true
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
