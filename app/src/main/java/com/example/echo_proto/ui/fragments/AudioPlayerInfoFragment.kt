package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.echo_proto.databinding.FragmentAudioPlayerInfoPageBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.loadLargeCover
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AudioPlayerInfoFragment : Fragment() {

    private var _binding: FragmentAudioPlayerInfoPageBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel by activityViewModels<MainViewModel>()

    private var currentEpisode: Episode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAudioPlayerInfoPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("\n\nLAUNCH ------>>>>>    AudioPlayerInfoFragment    <<<<<----------\n\n")

        setupClickListeners()
        subscribeToEpisodeUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupClickListeners() = with(binding) {
        btnChangeSpeed.setOnClickListener {
            (parentFragment as? AudioPlayerDetailFragment)?.openSpeedControl()
        }
        dtnToDescription.setOnClickListener {
            (parentFragment as? AudioPlayerDetailFragment)?.scrollToDescription()
        }
        btnGoToYoutube.setOnClickListener {
            (parentFragment as? AudioPlayerDetailFragment)?.openYoutube(currentEpisode?.videoLink)
        }
    }

    private fun subscribeToEpisodeUpdates() {
        mainViewModel.currentEpisodeFromDb.observe(viewLifecycleOwner) { episode ->
            currentEpisode = episode
            bindEpisodeInfo(episode)
        }
    }

    private fun bindEpisodeInfo(episode: Episode) = with(binding) {
        tvTitle.text = episode.title
        tvPubDateAndSize.text = episode.timestamp.getDateFromLong()
        
        // 🎨 Загружаем обложку эпизода, а если её нет - обложку канала
        ivEpisodeCover.loadLargeCover(
            url = episode.episodeImageUrl,
            fallbackUrl = episode.channelImageUrl
        )
    }
}
