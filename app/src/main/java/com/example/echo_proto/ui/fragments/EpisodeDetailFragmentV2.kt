package com.example.echo_proto.ui.fragments

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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
import com.example.echo_proto.databinding.FragmentEpisodeDetailV2Binding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.worker.DownloadWorker
import com.example.echo_proto.ui.dialogs.OpenYoutubeDialogFragment
import com.example.echo_proto.ui.viewmodels.EpisodeDetailViewModel
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.getDateFromLong
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.N) // progressbar
class EpisodeDetailFragmentV2 : Fragment() {

    private var _binding: FragmentEpisodeDetailV2Binding? = null
    private val binding get() = _binding!!

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<EpisodeDetailViewModel>()
    private var currentEpisode: Episode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEpisodeDetailV2Binding.inflate(inflater, container, false)
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
                    is Resource.Success -> result.data?.let { updateEpisodeInfo(it) }
                    else -> Unit
                }
            }
        }
    }

    private fun updateEpisodeInfo(episode: Episode) {
        currentEpisode = episode

        binding.apply {
            tvTitle.text = episode.title
            tvDescription.text = episode.description
            
            val date = episode.timestamp.getDateFromLong()
            val size = 66 
            tvMetadata.text = "$date • ${size}mb"

            updateActionButtons(episode)
        }

        activity?.invalidateOptionsMenu()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updateActionButtons(episode: Episode) {
        val context = requireContext()
        val goldColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val blackColor = ContextCompat.getColor(context, R.color.colorBackground)
        val sandColor = ContextCompat.getColor(context, R.color.colorOnSurfaceVariant)
        val darkSurface = ContextCompat.getColor(context, R.color.colorSurfaceContainerHighest)

        // Queue button logic
        binding.btnAddToQueue.apply {
            text = if (episode.isInQueue) "IN QUEUE" else "ADD TO QUEUE"
            icon = ContextCompat.getDrawable(context, R.drawable.button_ic_queue_add)
            
            if (episode.isInQueue) {
                setBackgroundColor(goldColor)
                setTextColor(blackColor)
                iconTint = ColorStateList.valueOf(blackColor)
            } else {
                setBackgroundColor(darkSurface)
                setTextColor(sandColor)
                iconTint = ColorStateList.valueOf(sandColor)
            }
            
            setOnClickListener { 
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    viewModel.changeEpisodeQueueStatus()
                }
            }
        }

        // Play button logic
        binding.btnPlay.apply {
            setBackgroundColor(darkSurface)
            setTextColor(sandColor)
            iconTint = ColorStateList.valueOf(sandColor)
            setOnClickListener {
                if (!episode.isInQueue) {
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        viewModel.changeEpisodeQueueStatus()
                        mainViewModel.refreshPlayerPlaylist()
                        delay(300)
                        currentEpisode?.let { mainViewModel.playOrToggleEpisode(it) }
                    }
                } else {
                    mainViewModel.playOrToggleEpisode(episode)
                }
            }
        }

        updateDownloadButton(episode, goldColor, blackColor, sandColor)
    }

    private fun updateDownloadButton(episode: Episode, gold: Int, black: Int, sand: Int) {
        val darkLowest = ContextCompat.getColor(requireContext(), R.color.colorSurfaceContainerLowest)
        
        binding.btnDownload.apply {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_download)
            if (episode.isDownloaded) {
                text = "DELETE"
                setBackgroundColor(gold)
                setTextColor(black)
                iconTint = ColorStateList.valueOf(black)
                setOnClickListener { viewModel.deleteEpisodeFromDevice(episode.id) }
            } else {
                text = "DOWNLOAD"
                setBackgroundColor(darkLowest)
                setTextColor(sand)
                iconTint = ColorStateList.valueOf(sand)
                setOnClickListener { downloadEpisode(episode.id) }
            }
        }
    }

    private fun downloadEpisode(episodeId: Int) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(Data.Builder().putInt(DownloadWorker.KEY_CONTENT_URI, episodeId).build())
            .build()

        WorkManager.getInstance(requireContext()).enqueue(request)
        showDownloadProgress(request.id)
        
        if (!viewModel.currentEpisode.value?.isInQueue!!) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.changeEpisodeQueueStatus()
            }
        }
    }

    private fun showDownloadProgress(id: UUID) {
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(id)
            .observe(viewLifecycleOwner) { workInfo ->
                val gold = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                val black = ContextCompat.getColor(requireContext(), R.color.colorBackground)
                
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(DownloadWorker.PROGRESS, 0)
                        updateDownloadButtonState("$progress%", true)
                        updateProgressBar(progress)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        updateDownloadButtonState("DELETE", false)
                        updateProgressBar(0) 
                        currentEpisode?.let { updateDownloadButton(it.copy(isDownloaded = true), gold, black, 0) }
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        updateDownloadButtonState("DOWNLOAD", false)
                        updateProgressBar(0)
                    }
                    else -> Unit
                }
            }
    }

    private fun updateDownloadButtonState(text: String, isLoading: Boolean) {
        binding.btnDownload.text = text
        if (isLoading) {
            binding.btnDownload.strokeWidth = 2
            binding.btnDownload.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        } else {
            binding.btnDownload.strokeWidth = 0
        }
    }

    private fun updateProgressBar(progress: Int) {
        binding.progressBar.setProgress(progress, true)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menu.clear()
            menuInflater.inflate(R.menu.menu_top_episode_detail, menu)
        }

        override fun onPrepareMenu(menu: Menu) {
            val btnGoToYoutube = menu.findItem(R.id.mabEpisodeYoutubeLink)
            btnGoToYoutube?.isVisible = OpenYoutubeDialogFragment.isYoutubeLink(currentEpisode?.videoLink)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.mabEpisodeYoutubeLink -> {
                    currentEpisode?.videoLink?.let { link ->
                        if (OpenYoutubeDialogFragment.isYoutubeLink(link)) {
                            OpenYoutubeDialogFragment.newInstance(link)
                                .show(childFragmentManager, OpenYoutubeDialogFragment.TAG)
                        }
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
