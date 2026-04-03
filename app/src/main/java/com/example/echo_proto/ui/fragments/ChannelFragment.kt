package com.example.echo_proto.ui.fragments

import com.example.echo_proto.data.remote.FeedChannel
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentChannelsBinding
import com.example.echo_proto.ui.adapters.FeedAdapter
import com.example.echo_proto.ui.common.observePlaybackState
import com.example.echo_proto.ui.viewmodels.ChannelViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

@AndroidEntryPoint
class ChannelFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvAdapter: FeedAdapter
    private val viewModel by viewModels<ChannelViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()

    private lateinit var source: FeedChannel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sourceId = arguments?.getInt(Constants.CHANNEL_ID) ?: 0
        source = FeedChannel.channels[sourceId]

        viewModel.getRssChannelFromDatabase(feedChannel = source)
        setupRecyclerView()
        observePlaybackState(mainViewModel, rvAdapter)
        binding.swipeRefreshChannel.setOnRefreshListener { swipeToUpdate() }

        viewModel.rssChannel.observe(viewLifecycleOwner) { channelList ->
            if (channelList.isNullOrEmpty()) {
                binding.containerEmptyQueue.visibility = View.VISIBLE
                rvAdapter.submitList(emptyList())
            } else {
                binding.containerEmptyQueue.visibility = View.GONE
                rvAdapter.submitList(channelList)
                channelList.forEach {
                    Timber.d("CHANNEL FEED: rssId= ${it.rssId}, title=${it.title}, id=${it.id}, timestamp = ${it.timestamp}")
                }
            }
        }

        setupMenu()
    }

    private fun setupRecyclerView() {
        rvAdapter = FeedAdapter(null)
        binding.recyclerViewChannel.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun swipeToUpdate() {
        val stopRefresh = viewModel.updateChannelRss(feedChannel = source)
        binding.swipeRefreshChannel.isRefreshing = stopRefresh
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(channelMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val channelMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_channels, menu)
        }

        override fun onPrepareMenu(menu: Menu) { }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabChannelUpdate -> {
                    viewModel.updateChannelRss(feedChannel = source)
                    Toast.makeText(requireContext(), "UPDATE CHANNEL", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.mabChannelFavourites -> {
                    Toast.makeText(requireContext(), "ON SCREEN: #${source.name}", Toast.LENGTH_SHORT).show()
                    viewModel.getRssChannelFromDatabase(feedChannel = source)
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

    companion object {
        fun newInstance(id: Int): ChannelFragment {
            val args = Bundle().apply { putInt(Constants.CHANNEL_ID, id) }
            return ChannelFragment().apply { arguments = args }
        }
    }
}