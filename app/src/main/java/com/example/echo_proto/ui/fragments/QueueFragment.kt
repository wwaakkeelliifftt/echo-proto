package com.example.echo_proto.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.MainActivity
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentQueueBinding
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.adapters.*
import com.example.echo_proto.ui.viewmodels.MainViewModel
import com.example.echo_proto.ui.viewmodels.QueueViewModel
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class QueueFragment : Fragment(), ItemZoneTouchHandler {

    private var _binding: FragmentQueueBinding? = null
    private val binding get() = _binding!!

    private lateinit var queueAdapter: FeedAdapter
    private val viewModel by viewModels<QueueViewModel>()

    // todo: vm 1
//    private lateinit var mainViewModel: MainViewModel
    private val mainViewModel by activityViewModels<MainViewModel>() // <<- best approach??
    // todo: alter 1. second way??
    private var activityViewModel: MainViewModel? = null

    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentQueueBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // todo: vm2. we use this way to explicitly bind VM to activity lifecycle as host
//        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
        // todo: alter 2.
        activityViewModel = (requireActivity() as? MainActivity)?.mainViewModel
        activityViewModel?.mediaIdMapper(Constants.MEDIA_QUEUE_ID)

        binding.queueToolbar.inflateMenu(R.menu.menu_top_queue)
        binding.queueToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.mabQueueFix -> { viewModel.updateQueueLocker(); true }
                else -> false
            }
        }

        setupRecyclerView()
        viewModel.updateQueueRss()
    }



    // todo: vm3
    private fun subscribeToObservers() {
        viewModel.rssQueue.observe(viewLifecycleOwner) { queueList ->
            if (queueList.isNullOrEmpty()) {
                binding.containerEmptyQueue.visibility = View.VISIBLE
                queueAdapter.submitList(emptyList())
            } else {
                Timber.d("OBSERVE_RSS-QUEUE::::::::::::::::::getListUpdate")
                queueList.forEachIndexed { i, episode->
                    Timber.d("index=$i, queueIndex=${episode.indexInQueue}, title=${episode.title}")
                }
                // fix to viewModel or some menu sort methods with sort in fragment?
//                val sortedList = queueList.sortedBy { it.indexInQueue }
                binding.containerEmptyQueue.visibility = View.GONE
                queueAdapter.submitList(queueList)
            }
        }

        // think - doesn't need??
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> { /** progressBar.isVisible = true */ }
                is Resource.Success -> {
                    /** progressBar.isVisible = false */
//                    result.data?.let { queueAdapter.submitList(it) }
                }
                is Resource.Error -> Unit
            }
        }
        viewModel.isLockedQueue.observe(viewLifecycleOwner) { isLocked ->
            Timber.d("OBSERVE_SEPARATE:isLockedQueue::status=$isLocked")
            changeQueueLocker(isLocked = isLocked)
        }
    }

    private fun setupRecyclerView() {
        queueAdapter = FeedAdapter( this)
        binding.recyclerView.apply {
            adapter = queueAdapter
            layoutManager = LinearLayoutManager(requireContext())

            onItemClick {
                Timber.d("ON_ITEM_CLICK: pos=$it")
            }
            onLongItemClick { position -> }

            // todo: vm4. updated with interface clickListener
            queueAdapter.setClickListener { episode ->
//                Timber.d("CLICK_ON EPISODE TO PLAY: ${episode.title}")
            }
        }

    }

    override val isDraggableFragment: Boolean = true

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun changeDragIconVisibilityAlpha(): Float {
        return when (viewModel.isLockedQueue.value) {
            true -> 0.15f // View.INVISIBLE (=4)
            else -> 0.8f // View.VISIBLE (=0)
        }
    }

    private fun changeQueueLocker(isLocked: Boolean) {
        if (isLocked) {
            // todo: doesn't work..
            itemTouchHelper = null
            changeDragIconVisibilityAlpha()
//            viewModel.updateQueueRss()   // <<-- todo: fix with update a player-state queue
            queueAdapter.notifyDataSetChanged()
        } else if (!isLocked) {
            itemTouchHelper = ItemTouchHelper(getSwipeCallback(requireContext(), viewModel))
            itemTouchHelper?.attachToRecyclerView(binding.recyclerView)
            changeDragIconVisibilityAlpha()
            queueAdapter.notifyDataSetChanged()
        }
    }

    override fun navigateToEpisodeDetailScreen(episode: Episode) {
        // TODO: make viewPager with list of actual episodes

        viewModel.navigateToDetailWithSharedPref(episodeId = episode.id)
        findNavController().navigate(R.id.globalActionToEpisodeDetailFragment)
    }

    override fun playPauseStateChanger(episode: Episode) {
        Timber.d("CLICK_ON EPISODE TO PLAY: ${episode.title}")
        mainViewModel.playOrToggleEpisode(mediaItem = episode, true) // without "toggle" at this
    }

    private fun getSwipeCallback(context: Context, source: ViewModel): SwipeToDeleteCallback_Queue {
        return object : SwipeToDeleteCallback_Queue(context = context, sourceViewModel = source) {
            // todo: make diff action with approve to delete by swipe
            //  ...still works OK with itemTouchHelper==null (by button @lock)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                viewModel.changeEpisodeInQueueStatus(
                    position = pos,
                    source = viewModel.rssQueue
                )
                queueAdapter.notifyItemRemoved(pos)
                Toast.makeText(requireContext(), "Remove from queue, pos = $pos", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun updateQueueIndexes() {
//        queueAdapter.actualList.forEachIndexed { index, episode ->
//            Timber.d("updateQueueIndex: BEFORE: index=$index, episodeIndex=${episode.indexInQueue}, q=${episode.isInQueue}, title=${episode.title}")
//            viewModel.updateEpisodeIndex(episodeId = episode.id, newIndex = index)
//        }
        queueAdapter.actualList.forEachIndexed { index, episode ->
            Timber.d("actualList ------- AFTER: index=$index, episodeIndex=${episode.indexInQueue}, q=${episode.isInQueue}, title=${episode.title}")
        }
    }

    // todo: cannot get menu items form activity..??
    override fun onPrepareOptionsMenu(menu: Menu) {
        Timber.d(">>>>>>>>>>>>>>>--------------onPrepareOptionsMenu::::QUEUE")
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.menu_top_queue, menu)

        val searchItem = menu.findItem(R.id.mabQueueSearch)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    viewModel.searchByQuery(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    viewModel.searchByQuery(newText)
                } else if (newText == "") {
                    viewModel.updateQueueRss()
                }
                return true
            }
        })
        val btnFixQuery = menu.findItem(R.id.mabQueueFix)
        btnFixQuery.setOnMenuItemClickListener {
            viewModel.updateQueueLocker()
            true
        }
//        viewModel.isLockedQueue.observe(viewLifecycleOwner) { isLocked ->
//            when (isLocked) {
//                false -> {
//                    btnFixQuery.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_open, null)
//                    changeQueueLocker(isLocked = false)
//                }
//                true -> {
//                    btnFixQuery.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_close, null)
//                    changeQueueLocker(isLocked = true)
//                }
//            }
//        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onStop() {
        super.onStop()
        updateQueueIndexes()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // todo: alter 3.
        activityViewModel = null
    }

}