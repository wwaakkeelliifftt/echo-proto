package com.example.echo_proto.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.echo_proto.databinding.FragmentEpisodeDetailBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.worker.DownloadWorker
import com.example.echo_proto.ui.viewmodels.EpisodeDetailViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.getDateFromLong
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EpisodeDetailFragment : Fragment() {

    private var _binding: FragmentEpisodeDetailBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<EpisodeDetailViewModel>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEpisodeDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
//        viewModel.currentEpisode.observe(viewLifecycleOwner) { episode ->
//            updateEpisodeInfo(episode = episode)
//        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.curStateFlowEpisode.collect { result ->
                when (result) {
                    is Resource.Success -> updateEpisodeInfo(result.data!!)
                    else -> Unit
                }
            }
        }
    }

    private fun updateEpisodeInfo(episode: Episode) {
        val title = episode.title
        val description = episode.description
        val date = episode.timestamp.getDateFromLong()
        val size = 66

        binding.apply {
            tvTitle.text = title
            tvDescription.text = description
            tvPubDateAndSize.text = "$date\n${size}mb"

            btnAddToQueue.apply {

            }
            btnAddToQueue.isChecked = episode.isInQueue
            btnAddToQueue.setOnClickListener {
                viewModel.changeEpisodeQueueStatus()
            }

            btnDownload.isChecked = episode.isDownloaded
            btnDownload.setOnClickListener {
                Toast.makeText(requireContext(), "start download", Toast.LENGTH_SHORT).show()
                downloadEpisode(episodeId = episode.id)
            }

            // need to check this "check-state"
            btnPlay.isChecked = episode.isDownloaded == false &&
                    mainViewModel.currentPlayingEpisodeFromMediaServiceConnection.value?.description?.mediaId == episode.mediaId
            btnPlay.setOnClickListener {
                mainViewModel.playOrToggleEpisode(mediaItem = episode)
                Toast.makeText(requireContext(), "press play??", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun downloadEpisode(episodeId: Int) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(DownloadWorker.KEY_CONTENT_URI, episodeId)
                    .build()
            )
            .build()
        val x = WorkManager.getInstance(requireContext()).enqueue(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}