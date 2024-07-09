package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.echo_proto.MainActivity
import com.example.echo_proto.R
import com.example.echo_proto.databinding.ViewpagerAudioplayerHostBinding
import com.example.echo_proto.ui.adapters.ViewPagerFeedAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class HostAudioPlayerFragment : FragmentActivity() {

    private var _binding: ViewpagerAudioplayerHostBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPagerAdapter: ViewPagerFeedAdapter

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        Timber.d("ViewPagerAudioPlayer::onCreate::start")
        _binding = ViewpagerAudioplayerHostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        Timber.d("ViewPagerAudioPlayer::onCreate::end")
    }

    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerFeedAdapter(
            listOf(AudioPlayerDetailFragment(), AudioPlayerDescriptionFragment()),
            this
        )
        binding.vpAudioPlayer.adapter = viewPagerAdapter
    }

    fun getExternalPlayerHolder(): View {
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
//        super.onBackPressed()
        Toast.makeText(this, "PRESS BACK", Toast.LENGTH_SHORT).show()
        if (binding.vpAudioPlayer.currentItem == 1) {
            binding.vpAudioPlayer.currentItem = 0
        } else {
            BottomSheetBehavior.from(findViewById(R.id.bottomSheetContainer)).state =
                BottomSheetBehavior.STATE_COLLAPSED
        }
    }

}