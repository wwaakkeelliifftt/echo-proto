package com.example.echo_proto.ui.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
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
import javax.inject.Inject
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.common.ActionModeHelper

@AndroidEntryPoint
class FeedFragment : Fragment(), ItemZoneTouchHandler { //, ToolbarConfigurator {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var feedAdapter: EpisodeFeedAdapterV2
    private val viewModel by viewModels<FeedViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>() // <<- best approach??
    private var actionMode: ActionMode? = null
    private var isActionModeActive: Boolean = false
    private lateinit var actionModeHelper: ActionModeHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getRssFeedFromDatabase()
        setupRecyclerView()
        binding.swipeRefreshFeed.setOnRefreshListener {
            swipeToUpdate()
        }

        viewModel.rssFeed.observe(viewLifecycleOwner) { list ->
            val feedItems = feedAdapter.submitFeedItems(list)
            feedAdapter.notifyDataSetChanged()
            // Обновляем заголовок action mode если он активен
            if (isActionModeActive) {
                val selectedCount = feedItems.count { it is EpisodeFeedAdapterV2.FeedItem.Episode && it.episode.isSelected }
                actionMode?.title = "Selected: $selectedCount"
                Timber.tag("ACTION_MODE").d("Observer:: Selected count from list: $selectedCount")
                // Если нет выбранных эпизодов, закрываем action mode
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

        if (savedInstanceState != null) {
            val emptyDatabaseDialog = parentFragmentManager.findFragmentByTag(Constants.DATABASE_EMPTY_TAG)
                    as EmptyDatabaseDialogFragment?
            emptyDatabaseDialog?.setListener {
                viewModel.updateFeedRss()
            }
        }

        observePlaybackState(mainViewModel, feedAdapter)
        setupMenu()
    }

    private fun setupRecyclerView() {
        // todo: null here - interface for drag in queueFragment
        feedAdapter = EpisodeFeedAdapterV2(this)
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
            itemAnimator = null  // Отключаем анимацию для устранения моргания

            onItemClick { position ->
                if (isActionModeActive) {
                    // TODO: Handle action mode click for new adapter
                    Timber.d("Action mode click at position: $position")
                } else {
                    // Navigate to episode detail
                    val item = feedAdapter.actualList.getOrNull(position)
                    if (item is EpisodeFeedAdapterV2.FeedItem.Episode) {
                        navigateToEpisodeDetailScreen(item.episode)
                        Timber.d("Navigate to episode: ${item.episode.title}")
                    }
                }
            }
            onLongItemClick { position ->
                if (!isActionModeActive) {
                    // Start action mode for new adapter
                    val item = feedAdapter.actualList.getOrNull(position)
                    if (item is EpisodeFeedAdapterV2.FeedItem.Episode) {
                        // TODO: Start action mode
                        Timber.d("Start action mode for episode: ${item.episode.title}")
                        actionModeHelper = ActionModeHelper(
                            R.menu.menu_feed_action_mode,
                            onActionItemClicked = { itemId -> handleActionModeItemClick(itemId) },
                            onDestroyActionMode = { handleActionModeDestroy() }
                        )
                        actionMode = startActionMode(actionModeHelper, ActionMode.TYPE_PRIMARY)
                        isActionModeActive = true
                        feedAdapter.isActionModeActive = true
                        // TODO: Select episode in adapter
                    }
                } else {
                    actionMode?.finish()
                }
            }
            onDoubleTapItemClick {
                // not work..
                Toast.makeText(requireContext(), "ONLY DOUBLE TAP $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun swipeToUpdate() {
        val stopRefresh = viewModel.updateFeedRss()
        binding.swipeRefreshFeed.isRefreshing = stopRefresh
    }

    private fun handleActionModeItemClick(itemId: Int) {
        when (itemId) {
            R.id.amFeed_1 -> {
                Toast.makeText(requireContext(), "FIRST", Toast.LENGTH_SHORT).show()
                actionMode?.finish()
            }
            R.id.amFeed_2 -> {
                Toast.makeText(requireContext(), "SECOND", Toast.LENGTH_SHORT).show()
                actionMode?.finish()
            }
            R.id.amFeed_AddToQueue -> {
                viewModel.addSelectedEpisodesToQueue()
                val count = viewModel.getSelectedEpisodesCount()
                Toast.makeText(requireContext(), "ADD TO QUEUE ($count)", Toast.LENGTH_SHORT).show()
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

    private fun updateActionModeTitle() {
        val count = viewModel.getSelectedEpisodesCount()
        val title = "Selected: $count"
        Timber.tag("ACTION_MODE").d("updateActionModeTitle:: $title")
        actionMode?.title = title
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
        isActionModeActive = false
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

        override fun onPrepareMenu(menu: Menu) {
            configureSearch(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabFeedUpdate -> {
                    val done = viewModel.updateFeedRss()
                    if (done) {
                        Timber.d("rabotaet update?")
                    }
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
                    when {
                        !query.isNullOrEmpty() -> viewModel.searchByQuery(query)
                        query?.isEmpty() == true -> viewModel.updateFeedRss()
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