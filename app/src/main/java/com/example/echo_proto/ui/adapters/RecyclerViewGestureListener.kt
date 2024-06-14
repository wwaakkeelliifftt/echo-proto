package com.example.echo_proto.ui.adapters

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView


inline fun RecyclerView.onItemClick(crossinline click: (position: Int) -> Unit) =
    setOnItemClickListener(onClick = click)

inline fun RecyclerView.onLongItemClick(crossinline click: (position: Int) -> Unit) =
    setOnItemClickListener(onLongClick = click)

inline fun RecyclerView.onDoubleTapItemClick(crossinline click: (position: Int) -> Unit) =
    setOnItemClickListener(onDoubleTapClick = click)

inline fun RecyclerView.setOnItemClickListener(
    crossinline onClick: (position: Int) -> Unit = { },
    crossinline onLongClick: (position: Int) -> Unit = { },
    crossinline onDoubleTapClick: (position: Int) -> Unit = { }
) {
    addOnItemTouchListener(
        RecyclerItemClickListener(
            this,
            object : RecyclerItemClickListener.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    onClick.invoke(position)
                }
                override fun onLongItemClick(child: View, position: Int) {
                    onLongClick.invoke(position)
                }

                override fun onDoubleTap(child: View, position: Int) {
                    onDoubleTapClick.invoke(position)
                }
            }
        )
    )
}

class RecyclerItemClickListener(
    recyclerView: RecyclerView,
    private val mListener: OnItemClickListener?
) : RecyclerView.OnItemTouchListener {

    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)
        fun onLongItemClick(child: View, position: Int)
        fun onDoubleTap(child: View, position: Int)
    }

    private var mGestureDetector: GestureDetector = GestureDetector(
        recyclerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                if (event != null) {
                    val child = recyclerView.findChildViewUnder(event.x, event.y)
                    if (child != null && mListener != null) {
                        mListener.onItemClick(child, recyclerView.getChildAdapterPosition(child))
                    }
                }
                return true
            }
            override fun onLongPress(event: MotionEvent) {
                val child = recyclerView.findChildViewUnder(event.x, event.y)
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                val child = recyclerView.findChildViewUnder(e!!.x, e!!.y)
                if (child != null && mListener != null) {
                    mListener.onDoubleTap(child, recyclerView.getChildAdapterPosition(child))
                    return true
                }
                return super.onDoubleTap(e)
            }
        }
    )

    override fun onInterceptTouchEvent(recycler: RecyclerView, event: MotionEvent): Boolean {
        val childView = recycler.findChildViewUnder(event.x, event.y)
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(event)) {
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) { }
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) { }

}