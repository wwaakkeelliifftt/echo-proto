package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentDownloadsBinding
import com.example.echo_proto.ui.adapters.EpisodeFeedAdapterV2
import com.example.echo_proto.ui.adapters.ItemZoneTouchHandler
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.viewmodels.DownloadsViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.dialogs.DisplaySettingsBottomSheet
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DownloadsFragment : Fragment(), ItemZoneTouchHandler {

    private val viewModel by viewModels<DownloadsViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var downloadsAdapter: EpisodeFeedAdapterV2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDownloadsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        subscribeToObservers()
        setupRecyclerView()
        observePlaybackState(mainViewModel, downloadsAdapter)
        setupMenu()
    }

    private fun subscribeToObservers() {
        viewModel.rssDownloads.observe(viewLifecycleOwner) { downloadsList ->
            if (downloadsList.isNullOrEmpty()) {
                binding.containerEmptyDownloads.visibility = View.VISIBLE
                downloadsAdapter.submitList(emptyList())
            } else {
                binding.containerEmptyDownloads.visibility = View.GONE
                downloadsAdapter.submitList(downloadsList)
            }
        }

        // Observe display options
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.displayOptions.collectLatest { options ->
                val oldHasHeaders = downloadsAdapter.actualList.any { it is EpisodeFeedAdapterV2.FeedItem.DateHeader }
                val newHasHeaders = options.showDateHeaders
                
                downloadsAdapter.updateDisplayOptions(options)
                
                // If grouping changed, we MUST re-submit the list to rebuild FeedItems
                if (oldHasHeaders != newHasHeaders) {
                    viewModel.rssDownloads.value?.let { list ->
                        downloadsAdapter.submitList(list)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        downloadsAdapter = EpisodeFeedAdapterV2(this)
        binding.recyclerView.adapter = downloadsAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(downloadsMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val downloadsMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_downloads, menu)
        }

        override fun onPrepareMenu(menu: Menu) {}

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            .newInstance(DisplaySettingsBottomSheet.DOWNLOADS_SCREEN)
            .show(parentFragmentManager, DisplaySettingsBottomSheet.TAG)
    }

    // todo: need implement
    private fun startSelectionMode(position: Int) = 100500
    
    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        mainViewModel.navigateToDetailWithSharedPref(episode.id)
        findNavController().navigate(R.id.globalActionToEpisodeDetailFragment)
    }
    
    override fun playPauseStateChanger(episode: Episode) {
        mainViewModel.playOrToggleEpisode(episode, true)
    }

    override val isDraggableFragment: Boolean = false
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) = Unit
}
