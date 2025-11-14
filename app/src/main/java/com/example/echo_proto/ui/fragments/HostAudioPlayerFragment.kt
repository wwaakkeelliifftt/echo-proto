package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.echo_proto.databinding.ViewpagerAudioplayerHostBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostAudioPlayerFragment : Fragment() {

    private var _binding: ViewpagerAudioplayerHostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ViewpagerAudioplayerHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        binding.vpAudioPlayer.apply {
            adapter = AudioPlayerPagerAdapter(this@HostAudioPlayerFragment)
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
        }
    }

    fun scrollToDescription() {
        binding.vpAudioPlayer.currentItem = PAGE_DESCRIPTION
    }

    fun scrollToControls() {
        binding.vpAudioPlayer.currentItem = PAGE_DETAIL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class AudioPlayerPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val pages = listOf(
            AudioPlayerDetailFragment(),
            AudioPlayerDescriptionFragment()
        )

        override fun getItemCount(): Int = pages.size

        override fun createFragment(position: Int): Fragment = pages[position]
    }

    companion object {
        private const val PAGE_DETAIL = 0
        private const val PAGE_DESCRIPTION = 1
    }
}