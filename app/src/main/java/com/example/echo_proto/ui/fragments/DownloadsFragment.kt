package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentDownloadsBinding
import com.example.echo_proto.ui.adapters.FeedAdapter
import com.example.echo_proto.ui.viewmodels.DownloadsViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DownloadsFragment : Fragment() {

    private val viewModel by viewModels<DownloadsViewModel>()
    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!
    private lateinit var downloadsAdapter: FeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDownloadsBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupRecyclerView()
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

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.menu_top_downloads, menu)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}