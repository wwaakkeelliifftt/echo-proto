package com.example.echo_proto.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentEpisodeDetailBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.worker.DownloadWorker
import com.example.echo_proto.ui.viewmodels.EpisodeDetailViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.getDateFromLong
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.UUID
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider

@AndroidEntryPoint
class EpisodeDetailFragment : Fragment() {

    private var _binding: FragmentEpisodeDetailBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<EpisodeDetailViewModel>()
    private var currentEpisode: Episode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEpisodeDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupMenu()
    }

    private fun subscribeToObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.curStateFlowEpisode.collect { result ->
                when (result) {
                    is Resource.Success -> updateEpisodeInfo(result.data!!)
                    else -> Unit
                }
            }
        }
    }

    private fun updateEpisodeInfo(episode: Episode) = with(binding) {
        currentEpisode = episode

        tvTitle.text = episode.title
        tvDescription.text = episode.description
        val date = episode.timestamp.getDateFromLong()
        val size = 66
        tvPubDateAndSize.text = "$date\n${size}mb"

        btnAddToQueue.apply {
            isChecked = episode.isInQueue
            setOnClickListener { viewModel.changeEpisodeQueueStatus() }
        }

        btnDownload.apply {
            isChecked = episode.isDownloaded
            text = if (episode.isDownloaded) "Delete" else "Download"
            setOnClickListener {
                if (episode.isDownloaded) viewModel.deleteEpisodeFromDevice(episodeId = episode.id)
                else downloadEpisode(episodeId = episode.id)
            }
        }

        val curPlayEpisodeMediaId = mainViewModel.currentPlayingEpisodeFromMediaServiceConnection.value?.description?.mediaId
        val isEpisodePlaying = !episode.isDownloaded && curPlayEpisodeMediaId == episode.mediaId
        btnPlay.apply {
            isChecked = isEpisodePlaying
            setOnClickListener { mainViewModel.playOrToggleEpisode(mediaItem = episode) }
        }

        activity?.invalidateOptionsMenu()
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


    private fun isYoutubeLink(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    private fun openYoutube(url: String) {
        try {
            showYoutubeConfirmDialog(url)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка со ссылкой на ютуб")
            Toast.makeText(requireContext(), R.string.youtube_open_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showYoutubeConfirmDialog(youtubeUrl: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Открыть оригинальное видео в YouTube?")
            .setMessage(youtubeUrl)
            .setPositiveButton("Открыть") { dialog, _ ->
                openYoutubeIntent(youtubeUrl)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun openYoutubeIntent(youtubeUrl: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, youtubeUrl.toUri()).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            startActivity(intent)
            true
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при открытии интента на ютуб")
            Toast.makeText(requireContext(), R.string.youtube_open_error, Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(youtubeMenuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val youtubeMenuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_episode_detail, menu)
            updateYoutubeMenuVisibility(menu)
        }

        override fun onPrepareMenu(menu: Menu) {
            updateYoutubeMenuVisibility(menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabEpisodeYoutubeLink -> {
                    val link = currentEpisode?.videoLink
                    if (!link.isNullOrBlank() && isYoutubeLink(link)) {
                        openYoutube(link)
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        }

        private fun updateYoutubeMenuVisibility(menu: Menu) {
            val btnGoToYoutube = menu.findItem(R.id.mabEpisodeYoutubeLink)
            val link = currentEpisode?.videoLink
            btnGoToYoutube?.isVisible = !link.isNullOrBlank() && isYoutubeLink(link)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}