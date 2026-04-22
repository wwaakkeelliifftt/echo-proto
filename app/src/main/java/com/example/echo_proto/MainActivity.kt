package com.example.echo_proto

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.echo_proto.databinding.ActivityMainBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.ui.common.PlayPauseButtonAnimator
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.SnackbarHelper
import com.example.echo_proto.util.getCurrentTimeFromLong
import com.example.echo_proto.util.getTimeFromSeconds
import com.example.echo_proto.util.loadSmallThumbnail
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var currentPLayingEpisode: Episode? = null
    private var playbackState: PlaybackStateCompat? = null

    private lateinit var floatingStroke: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbarMain)

        val navController = this.findNavController(R.id.nav_host_fragment_container)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.hostFeedPager,
                R.id.queueFragment,
                R.id.hostChannelsPager,
                R.id.downloadsFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

        setupBottomSheet()
        setupClickListeners()
        subscribeToObservers()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupBottomSheet() {
        floatingStroke = findViewById(R.id.tvTitle)

        val bs = BottomSheetBehavior.from(binding.bottomSheetContainer).apply {
            peekHeight = resources.getDimension(R.dimen.echo_appbar_height_x2).toInt()
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.bottomSheetContainer.setOnClickListener {
            bs.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun setupClickListeners() {
        binding.bottomPlayback.apply {
            ivPlayPause.setOnClickListener { onPlayPauseClickListener.invoke() }
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.currentPlayingEpisodeFromMediaServiceConnection.observe(this) { metadataEpisode ->
            if (metadataEpisode == null) return@observe
            metadataEpisode.description.mediaId?.toInt()?.let { id ->
                mainViewModel.getCurrentPlayEpisode(id = id)
                Timber.tag("PLAY").d("MainActivity: metadata changed, mediaId=$id")
            }
        }
        mainViewModel.currentEpisodeFromDb.observe(this) { episode ->
            currentPLayingEpisode = episode.also {
                bindEpisodeData(it)
            }
        }
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            changePlayPauseImageState()
            changeFloatingTextState()
        }
        mainViewModel.currentPlayerPosition.observe(this) { setCurrentTimeToTextView(ms = it) }

        mainViewModel.isConnected.observe(this) { event ->
            val resource = event?.peekContent()
            if (resource is Resource.Success && resource.data == true) {
                Timber.tag("PLAY").d("MainActivity: Media connected, initiating refresh after delay")
                lifecycleScope.launch {
                    delay(800)
                    mainViewModel.refreshPlayerPlaylist()
                }
            }
            
            event?.getContentIfNotHandled()?.let { result ->
                if (result is Resource.Error) {
                    Snackbar.make(
                        binding.root,
                        result.message ?: "connection error was happened..",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        mainViewModel.networkError.observe(this) { event ->
            event?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Error -> {
                        SnackbarHelper.showError(
                            binding.rootLayout,
                            binding.snackbarAnchor,
                            result.message ?: "some error was happened.."
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun changeFloatingTextState() {
        floatingStroke.isSelected = playbackState?.isPlaying != true
    }

    private fun setCurrentTimeToTextView(ms: Long) {
        val currentProgress = (ms / 1000).toInt()
        binding.bottomPlayback.progressBar.progress = currentProgress
        val currentTime = ms.getCurrentTimeFromLong()
        binding.bottomPlayback.tvCurrentTime.text = currentTime
    }

    private var previousPlaybackState: Boolean? = null
    private fun changePlayPauseImageState() {
        val isPlaying = playbackState?.isPlaying == true

        if (previousPlaybackState != isPlaying) {
            previousPlaybackState = isPlaying

            PlayPauseButtonAnimator.animate(
                activeView = binding.bottomPlayback.ivPlayPause,
                ghostView = binding.bottomPlayback.ivPlayPauseGhost,
                isPlaying = isPlaying
            ) { playing ->
                if (playing) R.drawable.ic_state_pause else R.drawable.ic_state_play
            }
        }
    }

    private fun bindEpisodeData(episode: Episode) {
        binding.bottomPlayback.apply {
            tvTitle.text = episode.title
            tvTotalTime.text = " • ${episode.duration.getTimeFromSeconds()}"
            progressBar.max = episode.duration
            ivEpisodeThumbnail.loadSmallThumbnail(
                url = episode.episodeImageUrl,
                fallbackUrl = episode.channelImageUrl
            )
        }
    }

    private val onPlayPauseClickListener: () -> Unit = {
        currentPLayingEpisode?.let { episode ->
            mainViewModel.playOrToggleEpisode(episode, true)
        }
    }

}
