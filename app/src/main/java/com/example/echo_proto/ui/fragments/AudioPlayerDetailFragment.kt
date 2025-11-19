package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentAudioplayerDetailBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.ui.common.PlayPauseButtonAnimator
import com.example.echo_proto.ui.dialogs.OpenYoutubeDialogFragment
import com.example.echo_proto.ui.dialogs.SpeedControlBottomSheetFragment
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.getCurrentTimeFromLong
import com.example.echo_proto.util.getTimeFromSeconds
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AudioPlayerDetailFragment : Fragment() {

    private var _binding: FragmentAudioplayerDetailBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel by activityViewModels<MainViewModel>()

    private var currentEpisode: Episode? = null
    private var previousPlaybackState: Boolean? = null
    private var shouldUpdateSeekbar = true
    private lateinit var pagerAdapter: EpisodePagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudioplayerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("\n\nLAUNCH ------>>>>>    AudioPlayerDetailFragment    <<<<<----------\n\n")

        setupClickListeners()
        setupSeekbarListeners()
        setupViewPager()
        subscribeToObservers()
    }

    private fun setupClickListeners() = with(binding) {
        ivPlayPause.setOnClickListener { currentEpisode?.let { episode -> mainViewModel.playOrToggleEpisode(episode, true) } }
        ivSkipNext.setOnClickListener { mainViewModel.skipToNextEpisode() }
        ivSkipPrevious.setOnClickListener { mainViewModel.skipToPreviousEpisode() }
        ivForward.setOnClickListener {
            animateSeekButton(ivForward, clockwise = true)
            mainViewModel.seekForward()
        }
        ivReplay.setOnClickListener {
            animateSeekButton(ivReplay, clockwise = false)
            mainViewModel.seekReplay()
        }
    }

    private fun setupSeekbarListeners() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setCurrentTimeToText(progress.toLong() * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val result = (it.progress * 1000).toLong()
                    mainViewModel.seekTo(result)
                }
                shouldUpdateSeekbar = true
            }
        })
    }

    private fun subscribeToObservers() {
        mainViewModel.currentEpisodeFromDb.observe(viewLifecycleOwner) { episode ->
            currentEpisode = episode
            binding.apply {
                vpEpisodeContent.setCurrentItem(PAGE_INFO, false)
                seekBar.max = episode.duration
                tvTimerEpisodeTimeTotal.text = episode.duration.getTimeFromSeconds()
            }
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            changePlayPauseImageState()
        }

        mainViewModel.currentPlayerPosition.observe(viewLifecycleOwner) { position ->
            if (shouldUpdateSeekbar) {
                setCurrentTimeToText(position)
                binding.seekBar.progress = (position / 1000).toInt()
            }
        }

        mainViewModel.currentEpisodeDuration.observe(viewLifecycleOwner) { duration ->
            if (duration > 0) {
                val durationInSeconds = (duration / 1000).toInt()
                binding.seekBar.max = durationInSeconds
                binding.tvTimerEpisodeTimeTotal.text = durationInSeconds.getTimeFromSeconds()
            }
        }
    }

    private fun changePlayPauseImageState() {
        val isPlaying = mainViewModel.playbackState.value?.isPlaying == true
        if (previousPlaybackState != isPlaying) {
            previousPlaybackState = isPlaying
            PlayPauseButtonAnimator.animate(
                activeView = binding.ivPlayPause,
                ghostView = binding.ivPlayPauseGhost,
                isPlaying = isPlaying
            ) { playing ->
                if (playing) R.drawable.ic_menu_pause else R.drawable.ic_menu_play
            }
        }
    }

    private fun setCurrentTimeToText(ms: Long) {
        val currentTime = ms.getCurrentTimeFromLong()
        binding.tvCurrentTime.text = currentTime
    }

    private fun animateSeekButton(imageView: ImageView, clockwise: Boolean) {
        imageView.animate().cancel()
        imageView.rotation = 0f

        val rotationDelta = if (clockwise) 360f else -360f
        imageView.animate()
            .rotationBy(rotationDelta)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { imageView.rotation = 0f }
            .start()
    }

    fun scrollToDescription() {
        binding.vpEpisodeContent.currentItem = PAGE_DESCRIPTION
    }

    fun scrollToInfo() {
        binding.vpEpisodeContent.currentItem = PAGE_INFO
    }

    fun openSpeedControl() {
        SpeedControlBottomSheetFragment()
            .show(childFragmentManager, SpeedControlBottomSheetFragment.TAG)
    }

    fun openYoutube(url: String?) {
        showYoutubeDialog(url)
    }

    private fun setupViewPager() {
        pagerAdapter = EpisodePagerAdapter(this)
        binding.vpEpisodeContent.apply {
            adapter = pagerAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 1
        }
    }

    private fun showYoutubeDialog(url: String?) {
        if (!OpenYoutubeDialogFragment.isYoutubeLink(url)) return
        OpenYoutubeDialogFragment.newInstance(url)
            .show(childFragmentManager, OpenYoutubeDialogFragment.TAG)
    }

    private inner class EpisodePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            PAGE_INFO -> AudioPlayerInfoFragment()
            PAGE_DESCRIPTION -> AudioPlayerDescriptionFragment()
            else -> throw IllegalArgumentException("Unsupported page index: $position")
        }
    }

    companion object {
        private const val PAGE_INFO = 0
        private const val PAGE_DESCRIPTION = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}