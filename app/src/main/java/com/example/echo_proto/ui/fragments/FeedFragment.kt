package com.example.echo_proto.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
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
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.common.ActionModeHelper

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
        
        binding.swipeRefreshFeed.setOnRefreshListener {
            swipeToUpdate()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshFeed.isRefreshing = isLoading
        }

        viewModel.rssFeed.observe(viewLifecycleOwner) { list ->
            Timber.d("🎯 FEED_FRAGMENT: RSS feed observed, list size=${list.size}")
            val feedItems = feedAdapter.submitFeedItems(list)
            
            if (isActionModeActive) {
                val selectedCount = feedItems.count { it is EpisodeFeedAdapterV2.FeedItem.Episode && it.episode.isSelected }
                actionMode?.title = "Selected: $selectedCount"
                if (selectedCount == 0) {
                    actionMode?.finish()
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
        setupMenu()
    }

    private fun setupRecyclerView() {
        feedAdapter = EpisodeFeedAdapterV2(this)
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            itemAnimator = null
            
            // 🔧 ADDED: Sticky Header Decoration
            addItemDecoration(StickyHeaderDecoration(feedAdapter))

            onItemClick { position ->
                if (isActionModeActive) {
                    val item = feedAdapter.actualList.getOrNull(position)
                    if (item is EpisodeFeedAdapterV2.FeedItem.Episode) {
                        viewModel.selectEpisodeField(position)
                    }
                } else {
                    val item = feedAdapter.actualList.getOrNull(position)
                    if (item is EpisodeFeedAdapterV2.FeedItem.Episode) {
                        navigateToEpisodeDetailScreen(item.episode)
                    }
                }
            }
            onLongItemClick { position ->
                if (!isActionModeActive) {
                    val item = feedAdapter.actualList.getOrNull(position)
                    if (item is EpisodeFeedAdapterV2.FeedItem.Episode) {
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
                } else {
                    actionMode?.finish()
                }
            }
        }
    }

    private fun swipeToUpdate() {
        viewModel.updateFeedRss()
    }

    private fun handleActionModeItemClick(itemId: Int) {
        when (itemId) {
            R.id.amFeed_AddToQueue -> {
                viewModel.addSelectedEpisodesToQueue()
                actionMode?.finish()
            }
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
            configureSearch(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabFeedUpdate -> {
                    viewModel.updateFeedRss()
                    true
                }
                else -> false
            }
        }

        private fun configureSearch(menu: Menu) {
            val searchItem = menu.findItem(R.id.mabFeedSearch)
            val searchView = searchItem?.actionView as? SearchView ?: return
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrEmpty()) {
                        viewModel.searchByQuery(query)
                    }
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    if (!query.isNullOrEmpty()) {
                        viewModel.searchByQuery(query)
                    } else if (query?.isEmpty() == true) {
                        viewModel.getRssFeedFromDatabase()
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
}
