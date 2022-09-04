package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.echo_proto.databinding.ViewpagerFeedHostBinding
import com.example.echo_proto.ui.adapters.ViewPagerFeedAdapter
import com.example.echo_proto.ui.adapters.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostFeedPager: Fragment() {

    private var _binding: ViewpagerFeedHostBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedHostPagerAdapter: ViewPagerFeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ViewpagerFeedHostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val fragments = listOf(
            FeedFragment(),
            FeedPersonalFragment()
        )
        feedHostPagerAdapter = ViewPagerFeedAdapter(fragments, requireActivity())
        binding.viewPagerFeedHost.apply {
            adapter = feedHostPagerAdapter
            setPageTransformer(ZoomOutPageTransformer())
            offscreenPageLimit = fragments.size
        }
        TabLayoutMediator(binding.tabLayoutFeedHost, binding.viewPagerFeedHost) { tab, position ->
            tab.text = feedHostPagerAdapter.fragments[position]::class.java.simpleName
            tab.badge // todo: new episode counter??
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}