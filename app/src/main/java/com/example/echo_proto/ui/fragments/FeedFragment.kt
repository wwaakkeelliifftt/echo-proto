package com.example.echo_proto.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentFeedBinding
import com.example.echo_proto.ui.adapters.*
import com.example.echo_proto.ui.dialogs.EmptyDatabaseDialogFragment
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedAdapter: FeedAdapter
    private val viewModel by viewModels<FeedViewModel>()
    @Inject lateinit var sharedPref: SharedPreferences
//    @Inject lateinit var filterList: Set<String>
    private var actionMode: ActionMode? = null

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

    }

    private fun setupRecyclerView() {
        // todo: null here - interface for drag in queueFragment
        feedAdapter = FeedAdapter(null)
        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter

            onItemClick { position ->
                if (actionMode != null) {
                    viewModel.selectEpisodeField(position)
                } else {
                    Timber.d("action mode == NULL")
                }
            }
            onLongItemClick { position ->
                if (actionMode == null) {
                    actionMode = startActionMode(actionModeCallback, ActionMode.TYPE_PRIMARY)
                    viewModel.selectEpisodeField(position = position)
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

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.menu_feed_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModel.unselectAllFields()
            actionMode = null
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.amFeed_1 -> { Toast.makeText(requireContext(), "FIRST", Toast.LENGTH_SHORT).show(); mode?.finish() }
                R.id.amFeed_2 -> { Toast.makeText(requireContext(), "SECOND", Toast.LENGTH_SHORT).show(); mode?.finish() }
                R.id.amFeed_AddToQueue -> {
                    viewModel.addSelectedEpisodesToQueue()
                    Toast.makeText(requireContext(), "ADD TO QUEUE", Toast.LENGTH_SHORT).show()
                    mode?.finish()
                }
            }
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_top_feed, menu)

        val searchItem: MenuItem = menu.findItem(R.id.mabFeedSearch)
        val searchView: SearchView = (searchItem.actionView as SearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchByQuery(query)
                }
                return true
            }
            override fun onQueryTextChange(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchByQuery(query)
                } else if (query?.isEmpty() == true) {
                    viewModel.updateFeedRss()
                }
                return true
            }
        })

        val btnUpdateFeed = menu.findItem(R.id.mabFeedUpdate)
        btnUpdateFeed.setOnMenuItemClickListener {
            val done = viewModel.updateFeedRss()
            if (done) { /** todo: make progress bar indicator for upload */ }
            Timber.d("rabotaet update?")
            true
        }

        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mabAbout -> { Timber.d("INTERCEPT FROM fragment"); true }
            R.id.mabFeedUpdate -> { Timber.d("HANDLE mabUpdateFeed"); true }
            R.id.mabFeedSearch -> { Timber.d("onCLick Search"); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}