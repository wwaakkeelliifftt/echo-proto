package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.adapters.FeedAdapter
import com.example.echo_proto.databinding.FragmentFeedBinding
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var progressBar: ProgressBar? = null

    private lateinit var feedAdapter: FeedAdapter
    private val viewModel by viewModels<FeedViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.rssFeed.observe(viewLifecycleOwner) {
            feedAdapter.submitList(it)
            progressBar?.visibility = View.GONE
        }

        binding.swipeRefreshFeed.setOnRefreshListener {
//            progressBar?.visibility = View.VISIBLE
            swipeToUpdate()
        }
    }

    private fun setupRecyclerView() {
        progressBar = requireActivity().findViewById(R.id.progress_bar)
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            feedAdapter = FeedAdapter()
            adapter = feedAdapter
        }
    }

    private fun swipeToUpdate() {
        feedAdapter.clear()
        val stopRefresh = viewModel.getFeed()
        binding.swipeRefreshFeed.isRefreshing = stopRefresh
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        progressBar = null
    }
}