package com.example.echo_proto.ui.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.databinding.FragmentChannelsBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.EpisodeFeedAdapterV2
import com.example.echo_proto.ui.adapters.ItemZoneTouchHandler
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.dialogs.DisplaySettingsBottomSheet
import com.example.echo_proto.ui.viewmodels.ChannelViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.loadFullScreenBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ChannelFragment : Fragment(), ItemZoneTouchHandler {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvAdapter: EpisodeFeedAdapterV2
    private val viewModel by viewModels<ChannelViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var source: FeedChannel
    private var dimJob: Job? = null
    private var isDimmed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sourceId = arguments?.getInt(Constants.CHANNEL_ID) ?: 0
        source = FeedChannel.channels[sourceId]

        setupRecyclerView()
        setupObservers()
        
        binding.swipeRefreshChannel.setOnRefreshListener { 
            viewModel.updateChannelRss(feedChannel = source) 
        }
        
        binding.recyclerViewChannel.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
                startBackgroundDimming()
            }
            false 
        }

        viewModel.getRssChannelFromDatabase(feedChannel = source)
    }

    override fun onResume() {
        super.onResume()
        resetAndStartAnimations()
    }

    private fun resetAndStartAnimations() {
        isDimmed = false
        dimJob?.cancel()
        
        binding.ivChannelBackground.alpha = 0.66f
        if (::rvAdapter.isInitialized) {
            rvAdapter.itemsBackgroundFactor = 0f
        }

        binding.recyclerViewChannel.alpha = 0.8f
        binding.recyclerViewChannel.animate()
            .alpha(1.0f)
            .setDuration(500)
            .start()
    }

    private fun setupRecyclerView() {
        rvAdapter = EpisodeFeedAdapterV2(this)
        binding.recyclerViewChannel.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null 
        }
        observePlaybackState(mainViewModel, rvAdapter)
    }

    private fun setupObservers() {
        viewModel.rssChannel.observe(viewLifecycleOwner) { channelList ->
            if (channelList.isNullOrEmpty()) {
                binding.containerEmptyQueue.visibility = View.VISIBLE
                rvAdapter.submitList(emptyList())
            } else {
                binding.containerEmptyQueue.visibility = View.GONE
                rvAdapter.submitList(channelList)
                updateBackground(channelList.first())
            }
        }

        // Observe display options
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.displayOptions.collectLatest { options ->
                val oldHasHeaders = rvAdapter.actualList.any { it is EpisodeFeedAdapterV2.FeedItem.DateHeader }
                val newHasHeaders = options.showDateHeaders
                
                rvAdapter.updateDisplayOptions(options)
                
                if (oldHasHeaders != newHasHeaders) {
                    viewModel.rssChannel.value?.let { list ->
                        rvAdapter.submitList(list)
                    }
                }
            }
        }
    }

    private fun updateBackground(episode: Episode) {
        binding.ivChannelBackground.loadFullScreenBackground(episode.channelImageUrl)
    }

    private fun startBackgroundDimming() {
        if (isDimmed || dimJob?.isActive == true) return
        
        dimJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(200)
            if (isActive && _binding != null) {
                binding.ivChannelBackground.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .start()
                
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 700
                    addUpdateListener { animator ->
                        if (_binding != null) {
                            rvAdapter.itemsBackgroundFactor = animator.animatedValue as Float
                        }
                    }
                    start()
                }

                isDimmed = true
            }
        }
    }

    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        viewModel.navigateToDetailWithSharedPref(episode.id)
        findNavController().navigate(R.id.globalActionToEpisodeDetailFragment)
    }

    override fun playPauseStateChanger(episode: Episode) {
        mainViewModel.playOrToggleEpisode(episode, true)
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
            .newInstance(DisplaySettingsBottomSheet.CHANNELS_SCREEN)
            .show(parentFragmentManager, DisplaySettingsBottomSheet.TAG)
    }

    // todo: need implement
    private fun startSelectionMode(position: Int) = 100500

    override val isDraggableFragment: Boolean = false
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) = Unit

    override fun onDestroyView() {
        dimJob?.cancel()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(id: Int): ChannelFragment {
            val args = Bundle().apply { putInt(Constants.CHANNEL_ID, id) }
            return ChannelFragment().apply { arguments = args }
        }
    }
}
