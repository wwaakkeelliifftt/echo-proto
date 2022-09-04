package com.example.echo_proto.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentFeedPersonalBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.FeedAdapter
import com.example.echo_proto.ui.adapters.ItemZoneTouchHandler
import com.example.echo_proto.ui.dialogs.FeedFilterListDialogFragment
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FeedPersonalFragment: Fragment(), ItemZoneTouchHandler {

    private var _binding: FragmentFeedPersonalBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedPersonalAdapter: FeedAdapter
    private val viewModel by viewModels<FeedViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedPersonalBinding.inflate(layoutInflater)
//        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
            filterList.forEach {
                Timber.d("FeedPersonalFragment OBSERVE: Episode=${it.title}")
            }
        }

        if (savedInstanceState != null) {
            // todo: runtime crash because Fragment has constructor params. need to fix with @static companion fun
//            val feedFilterListDialogFragment = parentFragmentManager.findFragmentByTag(Constants.FEED_FILTER_DIALOG_TAG)
//                    as FeedFilterListDialogFragment?
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshRssFeedPersonal()
    }

    private fun setupRecycler() {
        // todo: null here - interface for drag in queueFragment
        feedPersonalAdapter = FeedAdapter(this)
        binding.recyclerViewFeedPersonal.apply {
            adapter = feedPersonalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun swipeToUpdate() {
        val stopRefresh = viewModel.updateFeedRss()
        binding.swipeRefreshFeedPersonal.isRefreshing = stopRefresh
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.menu_top_feed_personal, menu)

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
            val dialog = FeedFilterListDialogFragment(requireContext())
            dialog.show(childFragmentManager, Constants.FEED_FILTER_DIALOG_TAG)
            true
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveRssFeedPersonalFiltersIntoSharedPref()
        Timber.d("ON PAUSE  -----  STORE DATASET TO SHARED_PREF")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override val isDraggableFragment: Boolean = false
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) { }
    override fun changeDragIconVisibilityAlpha(): Float = 0f

    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        Timber.d("FEED PERSONAL: navigateToEpisodeDetail")
    }
    override fun playPauseStateChanger(episode: Episode) { }
}