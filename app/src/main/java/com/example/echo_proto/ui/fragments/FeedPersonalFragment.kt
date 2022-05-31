package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.echo_proto.databinding.FragmentFeedPersonalBinding
import com.example.echo_proto.ui.viewmodels.FeedPersonalViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedPersonalFragment: Fragment() {

    private var _binding: FragmentFeedPersonalBinding? = null
    private val binding get() = _binding!!

    val viewModel by viewModels<FeedPersonalViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFeedPersonalBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        binding.swipeRefreshFeedPersonal.setOnRefreshListener {

        }

    }

    private fun setupRecycler() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}