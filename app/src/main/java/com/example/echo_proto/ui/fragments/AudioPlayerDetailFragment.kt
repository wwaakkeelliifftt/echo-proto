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
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentAudioplayerDetailBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.ui.dialogs.OpenYoutubeDialogFragment
import com.example.echo_proto.ui.dialogs.SpeedControlBottomSheetFragment
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.getCurrentTimeFromLong
import com.example.echo_proto.util.getDateFromLong
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudioplayerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("\n\nLAUNCH ------>>>>>    AudioPlayerDetailFragment    <<<<<----------\n\n")

        setupClickListeners()
        setupSeekbarListeners()
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
        dtnToDescription.setOnClickListener {
            (parentFragment as? HostAudioPlayerFragment)?.scrollToDescription()
        }
        btnGoToYoutube.setOnClickListener {
            showYoutubeDialog(currentEpisode?.videoLink)
        }
        btnChangeSpeed.setOnClickListener {
            SpeedControlBottomSheetFragment()
                .show(childFragmentManager, SpeedControlBottomSheetFragment.TAG)
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
            bindEpisodeData(episode)
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

    private fun bindEpisodeData(episode: Episode) = with(binding) {
        tvTitle.text = episode.title
        tvPubDateAndSize.text = episode.timestamp.getDateFromLong()
        seekBar.max = episode.duration
        tvTimerEpisodeTimeTotal.text = episode.duration.getTimeFromSeconds()
    }

    private fun changePlayPauseImageState() {
        val isPlaying = mainViewModel.playbackState.value?.isPlaying == true
        if (previousPlaybackState != isPlaying) {
            previousPlaybackState = isPlaying

            binding.ivPlayPause.animate().cancel()
            animatePlayPauseButton(binding.ivPlayPause, isPlaying)
        }
    }

    private fun setCurrentTimeToText(ms: Long) {
        val currentTime = ms.getCurrentTimeFromLong()
        binding.tvCurrentTime.text = currentTime
    }

    private fun animatePlayPauseButton(imageView: ImageView, isPlaying: Boolean) {
        imageView.rotation = 0f

        imageView.animate()
            .rotation(90f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                imageView.rotation = 0f
                val iconRes = if (isPlaying) R.drawable.ic_menu_pause else R.drawable.ic_menu_play
                imageView.setImageResource(iconRes)
            }
            .start()
    }

    private fun showYoutubeDialog(url: String?) {
        if (!OpenYoutubeDialogFragment.isYoutubeLink(url)) return
        OpenYoutubeDialogFragment.newInstance(url)
            .show(childFragmentManager, OpenYoutubeDialogFragment.TAG)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}