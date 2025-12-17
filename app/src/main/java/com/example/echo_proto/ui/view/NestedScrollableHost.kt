package com.example.echo_proto.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs


class NestedScrollableHost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var initialX = 0f
    private var initialY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val parentViewPager: ViewPager2?
        get() {
            var parent = parent
            while (parent != null && parent !is ViewPager2) {
                parent = parent.parent
            }
            return parent as? ViewPager2
        }

    private val child: View?
        get() = if (childCount > 0) getChildAt(0) else null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        handleInterceptTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        handleInterceptTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun handleInterceptTouchEvent(ev: MotionEvent) {
        val parentViewPager = parentViewPager ?: return
        val child = child ?: return

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = ev.x
                initialY = ev.y
                parentViewPager.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - initialX
                val dy = ev.y - initialY
                val isViewPagerHorizontal = parentViewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL
                val scaledDx = abs(dx) * if (isViewPagerHorizontal) 1f else 0.35f
                val scaledDy = abs(dy) * if (isViewPagerHorizontal) 0.35f else 1f

                if (scaledDx > touchSlop || scaledDy > touchSlop) {
                    val isGesturePerpendicular = scaledDy > scaledDx
                    if (isViewPagerHorizontal == isGesturePerpendicular) {
                        parentViewPager.requestDisallowInterceptTouchEvent(false)
                    } else {
                        val direction = if (isViewPagerHorizontal) {
                            if (dx > 0) -1 else 1
                        } else {
                            if (dy > 0) -1 else 1
                        }

                        val canChildScroll = if (isViewPagerHorizontal) {
                            child.canScrollHorizontally(direction)
                        } else {
                            child.canScrollVertically(direction)
                        }

                        parentViewPager.requestDisallowInterceptTouchEvent(canChildScroll)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parentViewPager.requestDisallowInterceptTouchEvent(false)
            }
        }
    }
}