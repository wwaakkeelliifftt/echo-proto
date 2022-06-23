package com.example.echo_proto.ui.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.FragmentQueueBinding
import com.example.echo_proto.ui.adapters.*
import com.example.echo_proto.ui.viewmodels.QueueViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class QueueFragment : Fragment(), OnStartDragListener {

    private var _binding: FragmentQueueBinding? = null
    private val binding get() = _binding!!

    private lateinit var queueAdapter: FeedAdapter
    private val viewModel by viewModels<QueueViewModel>()

//    private lateinit var itemTouchHelper: ItemTouchHelper
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentQueueBinding.inflate(layoutInflater)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateQueueRss()
        setupRecyclerView()

        viewModel.rssQueue.observe(viewLifecycleOwner) { queueList ->
            if (queueList.isNullOrEmpty()) {
                binding.containerEmptyQueue.visibility = View.VISIBLE
                queueAdapter.submitList(emptyList())
            } else {
                binding.containerEmptyQueue.visibility = View.GONE
                queueAdapter.submitList(queueList)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        updateQueueIndexes()
    }

    private fun setupRecyclerView() {
        queueAdapter = FeedAdapter(this)

//        val holderBackgroundIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!
//        itemTouchHelper = ItemTouchHelper(getSwipeCallback(holderBackgroundIcon, viewModel))
//        itemTouchHelper?.attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.apply {
            adapter = queueAdapter
            layoutManager = LinearLayoutManager(requireContext())

            onItemClick {  }
            onLongItemClick { position -> }
        }

    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
    }

    override fun changeDragIconVisibilityAlpha(): Float {
        return when (viewModel.isLockedQueue.value) {
            true -> 0.3f // View.INVISIBLE (=4)
            else -> 1.0f // View.VISIBLE (=0)
        }
    }

    private fun getSwipeCallback(icon: Drawable, source: ViewModel): SwipeToDeleteCallback_Queue {
        return object : SwipeToDeleteCallback_Queue(icon = icon, sourceViewModel = source) {
            // todo: make diff action with approve to delete by swipe
            //  ...still works OK with itemTouchHelper==null (by button @lock)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                viewModel.changeEpisodeInQueueStatus(position = pos, source = viewModel.rssQueue)
                queueAdapter.notifyItemRemoved(pos)
                Toast.makeText(requireContext(), "Remove from queue, pos = $pos", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun updateQueueIndexes() {
        queueAdapter.actualList.forEachIndexed { index, episode ->
            viewModel.updateEpisodeIndex(episode.id, index)
            Timber.d("actualList -- index=$index-->> episodeIndex=${episode.indexInQueue}, q?=${episode.isInQueue}, title=${episode.title}")
        }
    }

    // todo: cannot get menu items form activity..??
    override fun onPrepareOptionsMenu(menu: Menu) {
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
        viewModel.isLockedQueue.observe(viewLifecycleOwner) { isLocked ->
            when (isLocked) {
                false -> {
                    btnFixQuery.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_open, null)
                    changeQueueLocker(isLocked = false)
                }
                true -> {
                    btnFixQuery.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_lock_close, null)
                    changeQueueLocker(isLocked = true)
                }
            }
        }
        super.onPrepareOptionsMenu(menu)
    }

    private fun changeQueueLocker(isLocked: Boolean) {
        if (isLocked) {
            itemTouchHelper = null
            changeDragIconVisibilityAlpha()
            queueAdapter.notifyDataSetChanged()
        } else {
            val holderBackgroundIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!
            itemTouchHelper = ItemTouchHelper(getSwipeCallback(holderBackgroundIcon, viewModel))
            itemTouchHelper?.attachToRecyclerView(binding.recyclerView)
            changeDragIconVisibilityAlpha()
            queueAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}