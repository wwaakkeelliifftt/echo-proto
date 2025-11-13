package com.example.echo_proto

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.echo_proto.databinding.ActivityMainBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.ui.view.ToolbarConfigurator
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
import com.example.echo_proto.util.SnackbarHelper
import com.example.echo_proto.util.getCurrentTimeFromLong
import com.example.echo_proto.util.getDateFromLong
import com.example.echo_proto.util.getTimeFromSeconds
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mainViewModel: MainViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    private var currentPLayingEpisode: Episode? = null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true
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
        setupSeekbarListeners()
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

        binding.player.apply {
            ivPlayPause.setOnClickListener { onPlayPauseClickListener.invoke() }
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

    }

    private fun setupSeekbarListeners() {
        binding.player.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekBar?.let {
                        val result = (it.progress * 1000).toLong()
                        setCurrentTimeToTextView(result)
                    }
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
        mainViewModel.currentPlayingEpisodeFromMediaServiceConnection.observe(this) { metadataEpisode ->
            if (metadataEpisode == null) return@observe
            metadataEpisode.description.mediaId?.toInt()?.let { id ->
                mainViewModel.getCurrentPlayEpisode(id = id)
                Timber.d("MainActivity::subscribeToObservers:mediaId=$id")
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
        mainViewModel.currentPlayerPosition.observe(this) {
            if (shouldUpdateSeekbar) {
                setCurrentTimeToTextView(ms = it)
            }
        }

        // observer for handling error only
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Error ->
                        Snackbar.make(
                            binding.root,
                            result.message ?: "connection error was happened..",
                            Snackbar.LENGTH_LONG
                        ).show()
                    else -> Unit
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

        // think, we don't need this observers
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result) {
                    is Resource.Loading -> Unit
                    is Resource.Error -> Unit
                    is Resource.Success -> {
                        result.data?.let { episodes ->
                            // Episodes loaded from media service
                            if (currentPLayingEpisode == null && episodes.isNotEmpty()) {
                                // Could auto-play first episode here if needed
                            }
                        }
                    }
                }
            }
        }
        mainViewModel.currentFragmentViewModelState.observe(this) { state ->
            mainViewModel.setViewModelState(state = state)
        }


    }

    private fun changeFloatingTextState() {
        floatingStroke.isSelected = playbackState?.isPlaying != true
    }

    private fun setCurrentTimeToTextView(ms: Long) {
        val currentProgress = (ms / 1000).toInt()
        binding.player.seekBar.progress = currentProgress
        binding.bottomPlayback.progressBar.progress = currentProgress
        val currentTime = ms.getCurrentTimeFromLong()
        Timber.d("-------->>>>>>>>curTime=$currentTime")
        binding.player.tvCurrentTime.text = currentTime
        binding.bottomPlayback.tvCurrentTime.text = currentTime
    }

    private var previousPlaybackState: Boolean? = null
    private fun changePlayPauseImageState() {
        val isPlaying = playbackState?.isPlaying == true

        if (previousPlaybackState != isPlaying) {
            previousPlaybackState = isPlaying

            binding.bottomPlayback.ivPlayPause.animate().cancel()
            binding.player.ivPlayPause.animate().cancel()

            animatePlayPauseButton(
                binding.bottomPlayback.ivPlayPause,
                isPlaying,
                fromBottomPlayback = true
            )
            animatePlayPauseButton(binding.player.ivPlayPause, isPlaying)
        }
    }

    private fun animatePlayPauseButton(imageView: ImageView, isPlaying: Boolean, fromBottomPlayback: Boolean = false) {
        imageView.rotation = 0f

        imageView.animate()
            .rotation(90f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                imageView.rotation = 0f
                val iconRes = if (isPlaying) {
                    if (fromBottomPlayback) R.drawable.ic_state_pause else R.drawable.ic_menu_pause
                } else {
                    if (fromBottomPlayback) R.drawable.ic_state_play else R.drawable.ic_menu_play
                }
                imageView.setImageResource(iconRes)
            }
            .start()
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

    private fun bindEpisodeData(episode: Episode) {
        binding.bottomPlayback.apply {
            tvTitle.text = episode.title
            tvTotalTime.text = episode.duration.getTimeFromSeconds()
            progressBar.max = episode.duration
        }
        binding.player.apply {
            tvTitle.text = episode.title
            tvTimerEpisodeTimeTotal.text = episode.duration.getTimeFromSeconds()
            tvPubDateAndSize.text = episode.timestamp.getDateFromLong()
            seekBar.max = episode.duration
        }
    }

    private val onPlayPauseClickListener: () -> Unit = {
        currentPLayingEpisode?.let { episode ->
            mainViewModel.playOrToggleEpisode(episode, true)
        }
    }

}
