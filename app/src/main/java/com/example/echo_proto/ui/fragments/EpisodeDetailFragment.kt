package com.example.echo_proto.ui.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.echo_proto.MainActivity
import com.example.echo_proto.databinding.FragmentEpisodeDetailBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.worker.DownloadWorker
import com.example.echo_proto.ui.viewmodels.EpisodeDetailViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.requestPermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.UUID

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
        val isDownloaded = episode.isDownloaded

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

            btnDownload.isChecked = isDownloaded
            btnDownload.text = if (isDownloaded) "Delete" else "Download"
            btnDownload.setOnClickListener {
                Timber.d("EpisodeDetailFragment::btnDownload=${episode.isDownloaded}\n" +
                        "audioLink=${episode.audioLink}\n" +
                        "downloadUrl=${episode.downloadUrl}\n")
                if (isDownloaded) {
                    viewModel.deleteEpisodeFromDevice(episodeId = episode.id)
                } else {
                    downloadEpisode(episodeId = episode.id)
                }
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
        Timber.d("🕒 1. DOWN::EpisodeDetailFragment: downloadEpisode() called")
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(
                Data.Builder()
                    .putInt(DownloadWorker.KEY_CONTENT_URI, episodeId)
                    .build()
            )
            .build()

        Timber.d("🕒 2. DOWN::EpisodeDetailFragment: WorkRequest created")
        WorkManager.getInstance(requireContext()).enqueue(request)
        Timber.d("🕒 3. DOWN::EpisodeDetailFragment: Work enqueued")
        showDownloadProgress(request.id)
        if (!viewModel.currentEpisode.value!!.isInQueue) {
            viewModel.changeEpisodeQueueStatus()
        }
    }

    private fun showDownloadProgress(id: UUID) {
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(id)
            .observe(this) { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.ENQUEUED -> {
                        binding.btnDownload.text = "Queued"
                    }
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(DownloadWorker.PROGRESS, 0)
                        binding.btnDownload.text = "$progress%"
                        binding.progressBar.progress = progress
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        binding.btnDownload.text = "DELETE"
                        binding.btnDownload.isChecked = true
                    }
                    WorkInfo.State.FAILED -> {
                        binding.btnDownload.text = "Download"
                        binding.btnDownload.isChecked = false
                    }
                    else -> Unit
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}