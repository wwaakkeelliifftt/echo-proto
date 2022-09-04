package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.echo_proto.data.remote.FeedChannel
import com.example.echo_proto.databinding.ViewpagerChannelsHostBinding
import com.example.echo_proto.ui.adapters.ViewPagerFeedAdapter
import com.example.echo_proto.ui.adapters.ZoomOutPageTransformer
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HostChannelsPager : Fragment() {

    private var _binding: ViewpagerChannelsHostBinding? = null
    private val binding get() = _binding!!

    private lateinit var channelsHostPagerAdapter: ViewPagerFeedAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ViewpagerChannelsHostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        val fragments = mutableListOf<Fragment>()
        FeedChannel.listOfChannels.forEach { feedChannel ->
            val fragment = ChannelFragment.newInstance(feedChannel.id)
            fragments.add(fragment)
        }
        channelsHostPagerAdapter = ViewPagerFeedAdapter(fragments = fragments, requireActivity())
        binding.viewPagerChannelsHost.apply {
            adapter = channelsHostPagerAdapter
            setPageTransformer(ZoomOutPageTransformer())
            offscreenPageLimit = fragments.size

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            })
        }
        TabLayoutMediator(binding.tabLayoutChannelsHost, binding.viewPagerChannelsHost) { tab, position ->
            tab.text = "${position + 1}.${FeedChannel.listOfChannels[position].tabBadgeName}"
            tab.badge // todo: new episode counter??
        }.attach()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}