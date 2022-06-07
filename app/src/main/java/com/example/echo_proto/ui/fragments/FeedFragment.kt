package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.adapters.FeedAdapter
import com.example.echo_proto.databinding.FragmentFeedBinding
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.util.Constants
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedAdapter: FeedAdapter
    private val viewModel by viewModels<FeedViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.swipeRefreshFeed.setOnRefreshListener {
            swipeToUpdate()
        }

        viewModel.rssFeed.observe(viewLifecycleOwner) { list ->
            Timber.d("------------>>>>   UPDATE LIST on FEED Fragment    <<<--------")
            feedAdapter.submitList(list)
        }

        viewModel.isFeedLock.observe(viewLifecycleOwner) {
            if (it == true) {
//                binding.recyclerViewFeed.cancelDragAndDrop()
            }
            Timber.d("isFeedLock = $it")
        }

        viewModel.isDatabaseEmptyDialog.observe(viewLifecycleOwner) { show ->
            if (show == true) {
                EmptyDatabaseDialogFragment().apply {
                    setListener { viewModel.getFeed() }
                    enterTransition = R.style.BottomSheetDialog
                    exitTransition = R.style.BottomSheetDialog
                }.show(parentFragmentManager, Constants.DATABASE_EMPTY_TAG)

                viewModel.initDatabaseMessageSuccess()
            }
        }

        if (savedInstanceState != null) {
            val emptyDatabaseDialog = parentFragmentManager.findFragmentByTag(Constants.DATABASE_EMPTY_TAG)
                    as EmptyDatabaseDialogFragment?
            emptyDatabaseDialog?.setListener {
                viewModel.getFeed()
            }
        }

    }


    private fun setupRecyclerView() {
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            feedAdapter = FeedAdapter()
            adapter = feedAdapter
        }
    }

    private fun swipeToUpdate() {
        val stopRefresh = viewModel.getFeed()
        binding.swipeRefreshFeed.isRefreshing = stopRefresh
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_top_feed_fragment, menu)

        val searchItem: MenuItem = menu.findItem(R.id.mabSearch)
        val searchView: SearchView = (searchItem.actionView as SearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.updateSearchByQuery(query)
                }
                return true
            }
            override fun onQueryTextChange(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.updateSearchByQuery(query)
                } else if (query?.isEmpty() == true) {
                    viewModel.getFeed()
                }
                return true
            }
        })

        val btnUpdateFeed = menu.findItem(R.id.mabUpdateFeed)
        btnUpdateFeed.setOnMenuItemClickListener {
            val done = viewModel.getFeed()
            if (done) { /** todo: make progress bar indicator for upload */ }
            Timber.d("rabotaet update?")
            true
        }
        val btnFixQuery = menu.findItem(R.id.mabFixQuery)
        btnFixQuery.setOnMenuItemClickListener {
            if (viewModel.isFeedLock.value == true) {
                it.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_open, null)
            } else {
                it.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_close, null)
            }
            viewModel.feedLockerUpdate()
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mabAbout -> { Timber.d("INTERCEPT FROM fragment"); true }
            R.id.mabFixQuery -> { Timber.d("HANDLE mabQueryFeed"); true }
            R.id.mabUpdateFeed -> { Timber.d("HANDLE mabUpdateFeed"); true }
            R.id.mabSearch -> { Timber.d("onCLick Search"); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}