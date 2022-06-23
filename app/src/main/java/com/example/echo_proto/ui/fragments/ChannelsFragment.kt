package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentChannelsBinding
import com.example.echo_proto.ui.adapters.FeedAdapter
import com.example.echo_proto.ui.adapters.SwipeToDeleteCallback
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.ui.viewmodels.QueueViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelsFragment : Fragment() {

    private var _binding: FragmentChannelsBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvAdapter: FeedAdapter
    private val viewModel by viewModels<QueueViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChannelsBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.rssQueue.observe(viewLifecycleOwner) { channelList ->
            if (channelList.isNullOrEmpty()) {
                binding.containerEmptyQueue.visibility = View.VISIBLE
                rvAdapter.submitList(emptyList())
            } else {
                binding.containerEmptyQueue.visibility = View.GONE
                rvAdapter.submitList(channelList)
            }
        }
    }

    private fun setupRecyclerView() {
        // todo: null here - interface for drag in queueFragment
        rvAdapter = FeedAdapter(null)
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())

        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.menu_top_channels, menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}