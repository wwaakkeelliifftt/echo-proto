package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentDownloadsBinding
import com.example.echo_proto.ui.adapters.FeedAdapter
import com.example.echo_proto.ui.viewmodels.DownloadsViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

@AndroidEntryPoint
class DownloadsFragment : Fragment() {

    private val viewModel by viewModels<DownloadsViewModel>()
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var downloadsAdapter: FeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDownloadsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        subscribeToObservers()
        setupRecyclerView()
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
    }

    private fun setupRecyclerView() {
        downloadsAdapter = FeedAdapter(null ) // todo replace arg -> "this"
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

        override fun onPrepareMenu(menu: Menu) {
            // nothing extra yet
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}