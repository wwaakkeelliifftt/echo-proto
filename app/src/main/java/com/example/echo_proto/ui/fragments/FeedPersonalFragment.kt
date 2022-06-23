package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentFeedPersonalBinding
import com.example.echo_proto.ui.adapters.FeedAdapter
import com.example.echo_proto.ui.dialogs.FeedFilterListDialogFragment
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FeedPersonalFragment: Fragment() {

    private var _binding: FragmentFeedPersonalBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedPersonalAdapter: FeedAdapter
    private val viewModel by viewModels<FeedViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedPersonalBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        binding.swipeRefreshFeedPersonal.setOnRefreshListener {
            swipeToUpdate()
        }

        viewModel.rssFeed.observe(viewLifecycleOwner) { list ->
            Timber.d("------------>>>>   UPDATE LIST on PERSONAL FEED Fragment    <<<--------")
            feedPersonalAdapter.submitList(list)
        }

        if (savedInstanceState != null) {
            // todo: runtime crash because Fragment has constructor params. need to fix with @static companion fun
            val feedFilterListDialogFragment = parentFragmentManager.findFragmentByTag(Constants.FEED_FILTER_LIST)
                    as FeedFilterListDialogFragment?
            feedFilterListDialogFragment?.setListener {
                "nichego"
            }
        }

    }

    private fun setupRecycler() {
        // todo: null here - interface for drag in queueFragment
        feedPersonalAdapter = FeedAdapter(null)
        binding.recyclerViewFeedPersonal.apply {
            adapter = feedPersonalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun swipeToUpdate() {
        val stopRefresh = viewModel.updateFeedRss()
        binding.swipeRefreshFeedPersonal.isRefreshing = stopRefresh
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_top_feed_personal, menu)

        val searchItem = menu.findItem(R.id.mabFeedPersSearch)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchByQuery(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    viewModel.searchByQuery(newText)
                } else if (newText?.isEmpty() == true) {
                    viewModel.updateFeedRss()
                }
                return true
            }
        })

        val btnFilter = menu.findItem(R.id.mabFeedPersFilter)
        btnFilter.setOnMenuItemClickListener {
            FeedFilterListDialogFragment(
                requireContext(),
                setOf("Veller", "Kogan", "Sonin", "Zhivoi Gvozd'")
            ).show(parentFragmentManager, Constants.FEED_FILTER_LIST)
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.mabFeedPersUpdate -> { Timber.d("FEED_PERSONAL menu select: UPDATE"); true }
            R.id.mabFeedPersFilter -> { Timber.d("FEED_PERSONAL menu select: FILTER"); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}