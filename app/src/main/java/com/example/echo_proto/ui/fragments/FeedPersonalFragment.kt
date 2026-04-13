package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentFeedPersonalBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.EpisodeFeedAdapterV2
import com.example.echo_proto.ui.adapters.ItemZoneTouchHandler
import com.example.echo_proto.ui.dialogs.FeedFilterListDialogFragment
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.dialogs.DisplaySettingsBottomSheet
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class FeedPersonalFragment: Fragment(), ItemZoneTouchHandler {

    private var _binding: FragmentFeedPersonalBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedPersonalAdapter: EpisodeFeedAdapterV2
    private val viewModel by viewModels<FeedViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedPersonalBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        binding.swipeRefreshFeedPersonal.setOnRefreshListener {
            swipeToUpdate()
        }

        viewModel.rssFeed.observe(viewLifecycleOwner) {
            viewModel.refreshRssFeedPersonal()
        }

        viewModel.rssFeedPersonal.observe(viewLifecycleOwner) { filterList ->
            feedPersonalAdapter.submitList(filterList)
        }

        // Observe display options for Personal Feed
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.displayOptionsPersonal.collectLatest { options ->
                val oldHasHeaders = feedPersonalAdapter.actualList.any { it is EpisodeFeedAdapterV2.FeedItem.DateHeader }
                val newHasHeaders = options.showDateHeaders
                
                feedPersonalAdapter.updateDisplayOptions(options)
                
                if (oldHasHeaders != newHasHeaders) {
                    viewModel.rssFeedPersonal.value?.let { list ->
                        feedPersonalAdapter.submitList(list)
                    }
                }
            }
        }

        observePlaybackState(mainViewModel, feedPersonalAdapter)
        setupMenu()
    }

    private fun setupRecycler() {
        feedPersonalAdapter = EpisodeFeedAdapterV2(this)
        feedPersonalAdapter.setPlaybackButtonMode(EpisodeFeedAdapterV2.Companion.PlaybackButtonMode.DOWNLOAD)
        binding.recyclerViewFeedPersonal.apply {
            adapter = feedPersonalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun swipeToUpdate() {
        val stopRefresh = viewModel.updateFeedRss()
        binding.swipeRefreshFeedPersonal.isRefreshing = stopRefresh
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
            .newInstance(DisplaySettingsBottomSheet.PERSONAL_SCREEN)
            .show(parentFragmentManager, DisplaySettingsBottomSheet.TAG)
    }

    // todo: need implement
    private fun startSelectionMode(position: Int) = 100500

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(feedPersonalMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val feedPersonalMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_feed_personal, menu)
            configureSearch(menu)
        }

        override fun onPrepareMenu(menu: Menu) {
            configureSearch(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabFeedPersFilter -> {
                    val dialog = FeedFilterListDialogFragment()
                    dialog.show(childFragmentManager, Constants.FEED_FILTER_DIALOG_TAG)
                    true
                }
                else -> false
            }
        }

        private fun configureSearch(menu: Menu) {
            val searchItem = menu.findItem(R.id.mabFeedPersSearch)
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
                        query?.isEmpty() == true -> viewModel.refreshRssFeedPersonal()
                    }
                    return true
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveRssFeedPersonalFiltersIntoSharedPref()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override val isDraggableFragment: Boolean = false
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) { }
    
    override fun playPauseStateChanger(episode: Episode) {
        mainViewModel.playOrToggleEpisode(episode, true)
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

    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        viewModel.navigateToDetailWithSharedPref(episode.id)
        findNavController().navigate(R.id.globalActionToEpisodeDetailFragment)
    }
}
