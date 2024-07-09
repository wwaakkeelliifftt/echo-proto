package com.example.echo_proto

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.echo_proto.databinding.ActivityMainBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.exoplayer.isPlaying
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.util.Resource
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

    private var currentPLayingEpisode: Episode? = null
    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true
    private lateinit var floatingStroke: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        val navController = this.findNavController(R.id.nav_host_fragment_container)
        binding.bottomNavigationView.setupWithNavController(navController)
//        val appBarConfig = AppBarConfiguration.Builder(navGraph = navController.graph)
//            .build()

        setupBottomSheet()
        setupClickListeners()
        setupSeekbarListeners()
        subscribeToObservers()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.queueFragment -> {
                    Toast.makeText(this, "ON_QUEUE", Toast.LENGTH_SHORT).show()
//                    binding.rootAppBar.removeAllViews()
//                    binding.rootAppBar.addView(binding.toolbar)
//                    setSupportActionBar(binding.toolbar)
//                    NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig)
                }
                R.id.hostFeedPager -> Toast.makeText(this, "ON_FEED_HOST", Toast.LENGTH_SHORT).show()
                R.id.hostChannelsPager -> Toast.makeText(this, "ON_CHANNEL_HOST", Toast.LENGTH_SHORT).show()
                R.id.downloadsFragment -> Toast.makeText(this, "ON_DOWNLOADS", Toast.LENGTH_SHORT).show()
            }
        }

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
            ivForward.setOnClickListener { mainViewModel.seekForward() }
            ivReplay.setOnClickListener { mainViewModel.seekReplay() }
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
//            currentPLayingEpisode = metadataEpisode.toPlayerInfoEpisode()
        }
        mainViewModel.currentEpisodeFromDb.observe(this) { episode ->
//            Timber.d("subscribeToObservers at mainActivity ::::::: UPDATE ::::::: CURRENT_EPISODE_FROM_DB")
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
//                Timber.d("-----|||||----CURRENT_PLAYER_POSITION<Long>=$it")
                setCurrentTimeToTextView(ms = it)
            }
        }

        // observer for handling error only
        mainViewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Success -> Unit
                    is Resource.Loading -> Unit
                    is Resource.Error ->
                        Snackbar.make(
                            binding.root,
                            result.message ?: "connection error was happened..",
                            Snackbar.LENGTH_LONG
                        ).show()
                }
            }
        }
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result) {
                    is Resource.Error -> Snackbar.make(
                        binding.root,
                        result.message ?: "network error was happened..",
                        Snackbar.LENGTH_LONG
                    ).show()
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
                        result.data?.let { episides ->
                            // todo: bind episodes to..??
                            if (currentPLayingEpisode == null && episides.isNotEmpty()) {
//                                val initEpisode = episides[0]
//                                currentPLayingEpisode = initEpisode
//                                bindEpisodeData(episode = initEpisode)
                            } else {

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
//        Timber.d("\n>>>>>>>>------------------------------------------\n" +
//                "EPISODE_PROGRESS<Long>=$ms\n" +
//                "EPISODE_PROGRESS<Int>=$currentProgress\n" +
//                "player_seekbar<Int>=${binding.player.seekBar.progress}\n" +
//                "bottom_seekbar<Int>=${binding.bottomPlayback.progressBar.progress}")
        val currentTime = ms.getCurrentTimeFromLong()
        Timber.d("-------->>>>>>>>curTime=$currentTime")
        binding.player.tvCurrentTime.text = currentTime
        binding.bottomPlayback.tvCurrentTime.text = currentTime
    }

    private fun changePlayPauseImageState() {
        if (playbackState?.isPlaying == true) {
            binding.bottomPlayback.ivPlayPause.setImageResource(R.drawable.ic_state_pause)
            binding.player.ivPlayPause.setImageResource(R.drawable.ic_menu_pause)
        } else {
            binding.bottomPlayback.ivPlayPause.setImageResource(R.drawable.ic_state_play)
            binding.player.ivPlayPause.setImageResource(R.drawable.ic_menu_play)
        }
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

    // menu appbar setup
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_top_host_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mabSettings -> Toast.makeText(this, "menu: Settings", Toast.LENGTH_SHORT).show()
            R.id.mabAbout -> Snackbar.make(
                window.decorView.rootView, //<- works but not exactly!
                "menu: About",
                Snackbar.LENGTH_LONG
            ).show()
        }
        return true
    }

}
