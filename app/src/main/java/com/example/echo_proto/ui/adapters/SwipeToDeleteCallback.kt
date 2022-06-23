package com.example.echo_proto.ui.adapters

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.viewmodels.FeedViewModel
import com.example.echo_proto.ui.viewmodels.QueueViewModel
import timber.log.Timber

abstract class SwipeToDeleteCallback(context: Context):
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT) {

    private val iconDelete = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!
    private val intrinsicWidth = iconDelete.intrinsicWidth
    private val intrinsicHeight = iconDelete.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#F50057")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        iconDelete.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        iconDelete.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(canvas: Canvas?, top: Float, left: Float, right: Float, bottom: Float) {
        canvas?.drawRect(left, top, right, bottom, clearPaint)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean = false

}

abstract class SwipeToDeleteCallback_Queue(icon: Drawable, private val sourceViewModel: ViewModel):
    ItemTouchHelper.Callback() {

    private val iconDelete = icon
    private val intrinsicWidth = iconDelete.intrinsicWidth
    private val intrinsicHeight = iconDelete.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#F50057")
    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlag = ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlag, swipeFlag)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
        iconDelete.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        iconDelete.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(canvas: Canvas?, top: Float, left: Float, right: Float, bottom: Float) {
        canvas?.drawRect(left, top, right, bottom, clearPaint)
    }

    // need "off" to modify drag&drop without long press
    override fun isLongPressDragEnabled(): Boolean = false // super.isLongPressDragEnabled()

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        Timber.d("MOVE ON !!\n")
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        Timber.d("FROM POS: $fromPosition --> TO POS: $toPosition")

        val newList = (sourceViewModel as QueueViewModel).rssQueue.value as MutableList
        // ^^ newList is direct link to viewModel.rssQueue.value and straight modify them..

        Timber.d("newList hash = ${newList.hashCode()}")
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Timber.d("GO DOWN_1\n POS: i=$i -> ${newList[i].title} \n POS i++= ${i+1} -> ${newList[i+1].title}\n BEFORE--END")
                newList[i] = newList.set(i+1, newList[i])
                Timber.d("GO DOWN_2\n POS: i=$i -> ${newList[i].title} \n POS i++= ${i+1} -> ${newList[i+1].title}\n AFTER--END")
            }
        } else {
            for (i in fromPosition..toPosition+1) {
                Timber.d("GO UP_1\n POS: i=$i -> ${newList[i].title} \n POS i--= ${i-1} -> ${newList[i-1].title}\n BEFORE--END")
                newList[i] = newList.set(i-1, newList[i])
                Timber.d("GO UP_2\n POS: i=$i -> ${newList[i].title} \n POS i--= ${i-1} -> ${newList[i-1].title}\n AFTER--END")
            }
        }
        recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
        val size = recyclerView.adapter?.itemCount
        Timber.d("IN LIST -> $size item")
        newList.forEach {
            Timber.d("NEW_ORDER_LIST::${it.title}")
        }
        return true
    }

}