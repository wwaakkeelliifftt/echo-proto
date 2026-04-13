package com.example.echo_proto.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentFeedBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.*
import com.example.echo_proto.ui.dialogs.EmptyDatabaseDialogFragment
import com.example.echo_proto.ui.dialogs.DisplaySettingsBottomSheet
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.common.ActionModeHelper
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class FeedFragment : Fragment(), ItemZoneTouchHandler {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedAdapter: EpisodeFeedAdapterV2
    private val viewModel by viewModels<FeedViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private var actionMode: ActionMode? = null
    private var isActionModeActive: Boolean = false
    private lateinit var actionModeHelper: ActionModeHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("🎯 FEED_FRAGMENT: Starting FeedFragment initialization")
        viewModel.getRssFeedFromDatabase()
        
        setupRecyclerView()
        observeViewModel()
        setupMenu()
    }

    private fun observeViewModel() {
        binding.swipeRefreshFeed.setOnRefreshListener { viewModel.updateFeedRss() }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshFeed.isRefreshing = isLoading
        }

        viewModel.rssFeed.observe(viewLifecycleOwner) { list ->
            feedAdapter.submitList(list)
            
            if (isActionModeActive) {
                val selectedCount = feedAdapter.currentEpisodes().count { it.isSelected }
                if (selectedCount == 0) {
                    actionMode?.finish()
                } else {
                    actionMode?.title = "$selectedCount selected"
                }
            }
        }

        // Observe display options for this screen
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.displayOptions.collectLatest { options ->
                val oldHasHeaders = feedAdapter.actualList.any { it is EpisodeFeedAdapterV2.FeedItem.DateHeader }
                val newHasHeaders = options.showDateHeaders
                
                feedAdapter.updateDisplayOptions(options)
                
                if (oldHasHeaders != newHasHeaders) {
                    viewModel.rssFeed.value?.let { list ->
                        feedAdapter.submitList(list)
                    }
                }
            }
        }

        viewModel.isDatabaseEmptyDialog.observe(viewLifecycleOwner) { show ->
            if (show == true) {
                EmptyDatabaseDialogFragment().apply {
                    setListener { viewModel.updateFeedRss() }
                }.show(parentFragmentManager, Constants.DATABASE_EMPTY_TAG)
                viewModel.initDatabaseMessageSuccess()
            }
        }

        observePlaybackState(mainViewModel, feedAdapter)
    }

    private fun setupRecyclerView() {
        feedAdapter = EpisodeFeedAdapterV2(this)
        feedAdapter.setPlaybackButtonMode(EpisodeFeedAdapterV2.Companion.PlaybackButtonMode.DOWNLOAD)
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            itemAnimator = null
            addItemDecoration(StickyHeaderDecoration(feedAdapter))
        }
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
            .newInstance(DisplaySettingsBottomSheet.FEED_SCREEN)
            .show(parentFragmentManager, DisplaySettingsBottomSheet.TAG)
    }

    private fun startSelectionMode(position: Int) {
        actionModeHelper = ActionModeHelper(
            R.menu.menu_feed_action_mode,
            onActionItemClicked = { itemId -> handleActionModeItemClick(itemId) },
            onDestroyActionMode = { handleActionModeDestroy() }
        )
        actionMode = requireActivity().startActionMode(actionModeHelper)
        isActionModeActive = true
        feedAdapter.isActionModeActive = true
        viewModel.selectEpisodeField(position)
    }

    private fun handleActionModeItemClick(itemId: Int) {
        if (itemId == R.id.amFeed_AddToQueue) {
            viewModel.addSelectedEpisodesToQueue()
            actionMode?.finish()
        }
    }

    private fun handleActionModeDestroy() {
        viewModel.unselectAllFields()
        isActionModeActive = false
        feedAdapter.isActionModeActive = false
        actionMode = null
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(feedMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val feedMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_feed, menu)
            val searchItem = menu.findItem(R.id.mabFeedSearch)
            val searchView = searchItem?.actionView as? SearchView ?: return
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true
                override fun onQueryTextChange(query: String?): Boolean {
                    if (!query.isNullOrEmpty()) viewModel.searchByQuery(query)
                    else viewModel.clearSearch()
                    return true
                }
            })
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            if (menuItem.itemId == R.id.mabFeedUpdate) {
                viewModel.updateFeedRss()
                return true
            }
            return false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override val isDraggableFragment: Boolean = false
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) = Unit

    @SuppressLint("ResourceType")
    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        viewModel.navigateToDetailWithSharedPref(episode.id)
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
}
