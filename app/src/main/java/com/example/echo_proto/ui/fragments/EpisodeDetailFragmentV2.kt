package com.example.echo_proto.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.example.echo_proto.util.getSizeFromTimeDuration
import com.example.echo_proto.util.enrichForWebView
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.content.ContextCompat
import com.example.echo_proto.util.checkLessThenHour
import com.example.echo_proto.util.getTimeFromSeconds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.abs

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.N)
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
        setupCollapsingLogic()
        setupWebView()
    }

    private fun setupCollapsingLogic() {
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange
            if (totalScrollRange == 0) return@OnOffsetChangedListener

            val percentage = abs(verticalOffset).toFloat() / totalScrollRange

            // Плавное исчезновение расширенного контента
            binding.expandedContent.alpha = 1f - percentage
            
            // Плавное появление элементов в Toolbar (Title + Pinned Download Button)
            // Появляются после 60% скролла
            val pinnedAlpha = if (percentage > 0.6f) (percentage - 0.6f) / 0.4f else 0f
            binding.tvTitleCollapsed.alpha = pinnedAlpha
            binding.btnDownloadPinned.alpha = pinnedAlpha
        })
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
            tvTitleCollapsed.text = episode.title
            loadDescriptionInWebView(episode.description)
            
            val date = episode.timestamp.getDateFromLong()
            val size = episode.duration.getSizeFromTimeDuration()
            var duration = episode.duration.getTimeFromSeconds().checkLessThenHour()
            tvMetadata.text = "$date  ·  $size  ·  $duration"

            updateActionButtons(episode)
        }

        activity?.invalidateOptionsMenu()
    }

    private fun setupWebView() {
        binding.wvDescription.apply {
            settings.javaScriptEnabled = false
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Timber.d("🎯 WebView: Episode description page finished loading")
                }
                
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    // Открывать ссылки во внешнем браузере
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        Timber.d("🎯 WebView: Opening link in external browser: $url")
                    } catch (e: Exception) {
                        Timber.e("🚨 WebView: Failed to open link: $url", e)
                    }
                    return true // Блокируем загрузку в WebView
                }
            }
            
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
    }

    private fun loadDescriptionInWebView(description: String) {
        val formattedHtml = createStyledHtml(description)
        binding.wvDescription.loadDataWithBaseURL(
            null,
            formattedHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun createStyledHtml(description: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        font-size: 16sp;
                        line-height: 1.6;
                        color: ${getCurrentTextColor()};
                        margin: 0;
                        padding: 0;
                        background-color: transparent;
                    }
                    a {
                        color: ${getCurrentLinkColor()};
                        text-decoration: underline;
                    }
                    p {
                        margin: 0.5em 0;
                    }
                    strong, b {
                        font-weight: bold;
                    }
                    em, i {
                        font-style: italic;
                    }
                    code {
                        background-color: rgba(128, 128, 128, 0.2);
                        padding: 2px 4px;
                        border-radius: 3px;
                        font-family: monospace;
                    }
                    ul, ol {
                        margin: 0.5em 0;
                        padding-left: 1.5em;
                    }
                    li {
                        margin: 0.2em 0;
                    }
                </style>
            </head>
            <body>
                ${description.enrichForWebView()}
            </body>
            </html>
        """.trimIndent()
    }

    private fun getCurrentTextColor(): String {
        return if (isDarkTheme()) {
            "#FFFFFF" // белый для темной темы
        } else {
            "#000000" // черный для светлой темы
        }
    }

    private fun getCurrentLinkColor(): String {
        return if (isDarkTheme()) {
            "#4FC3F7" // голубой для темной темы
        } else {
            "#1976D2" // синий для светлой темы
        }
    }

    private fun isDarkTheme(): Boolean {
        return when (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

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
        
        // Синхронно обновляем обе кнопки
        listOf(binding.btnDownload, binding.btnDownloadPinned).forEach { button ->
            button.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_download)
            if (episode.isDownloaded) {
                button.text = "DELETE"
                button.setBackgroundColor(gold)
                button.setTextColor(black)
                button.iconTint = ColorStateList.valueOf(black)
                button.setOnClickListener { viewModel.deleteEpisodeFromDevice(episode.id) }
            } else {
                button.text = "DOWNLOAD"
                button.setBackgroundColor(darkLowest)
                button.setTextColor(sand)
                button.iconTint = ColorStateList.valueOf(sand)
                button.setOnClickListener { downloadEpisode(episode.id) }
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
        listOf(binding.btnDownload, binding.btnDownloadPinned).forEach { button ->
            button.text = text
            if (isLoading) {
                button.strokeWidth = 2
                button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
            } else {
                button.strokeWidth = 0
            }
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
