package com.example.echo_proto.ui.adapters

import android.animation.ArgbEvaluator
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.echo_proto.R
import com.example.echo_proto.databinding.ItemEpisodeHeaderV2Binding

/**
 * ItemDecoration for sticky headers in RecyclerView with Nocturne Gold animation
 */
class StickyHeaderDecoration(
    private val adapter: EpisodeFeedAdapterV2
) : RecyclerView.ItemDecoration() {

    private var currentHeader: Pair<Int, View>? = null
    private var lastStickyPosition: Int = -1
    private var animationStartTime: Long = 0
    private val argbEvaluator = ArgbEvaluator()

    // Animation constants
    private val FLASH_DURATION = 300L
    private val HOLD_DURATION = 3000L
    private val FADE_DURATION = 3000L
    private val TOTAL_DURATION = FLASH_DURATION + HOLD_DURATION + FADE_DURATION

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) return

        val headerPosition = getHeaderPositionForPosition(topChildPosition)
        if (headerPosition == -1) return

        // Check if header just became sticky
        if (headerPosition != lastStickyPosition) {
            lastStickyPosition = headerPosition
            animationStartTime = System.currentTimeMillis()
        }

        val headerView = getHeaderView(parent, headerPosition)
        fixLayoutSize(parent, headerView)

        // Apply animated color
        applyAnimatedColor(parent, headerView)

        val contactPoint = headerView.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPosition)

        if (childInContact != null && adapter.getItemViewType(parent.getChildAdapterPosition(childInContact)) == EpisodeFeedAdapterV2.TYPE_DATE_HEADER) {
            moveHeader(c, headerView, childInContact)
        } else {
            drawHeader(c, headerView)
        }

        // Request next frame if animation is running
        if (System.currentTimeMillis() - animationStartTime < TOTAL_DURATION) {
            parent.postInvalidateOnAnimation()
        }
    }

    private fun applyAnimatedColor(parent: RecyclerView, headerView: View) {
        val binding = ItemEpisodeHeaderV2Binding.bind(headerView)
        val context = parent.context
        
        val normalColor = ContextCompat.getColor(context, R.color.colorTextSecondary)
        val goldColor = ContextCompat.getColor(context, R.color.colorPrimary) // Nocturne Gold
        val outlineColor = ContextCompat.getColor(context, R.color.colorOutlineVariant)

        val elapsed = System.currentTimeMillis() - animationStartTime
        
        val currentColor = when {
            elapsed < FLASH_DURATION -> {
                // 0 to 300ms: Flash to Gold
                val fraction = elapsed.toFloat() / FLASH_DURATION
                argbEvaluator.evaluate(fraction, normalColor, goldColor) as Int
            }
            elapsed < FLASH_DURATION + HOLD_DURATION -> {
                // 300ms to 3300ms: Hold Gold
                goldColor
            }
            elapsed < TOTAL_DURATION -> {
                // 3300ms to 6300ms: Fade back to Normal
                val fraction = (elapsed - FLASH_DURATION - HOLD_DURATION).toFloat() / FADE_DURATION
                argbEvaluator.evaluate(fraction, goldColor, normalColor) as Int
            }
            else -> normalColor
        }

        // Apply color to views
        binding.tvDateHeader.setTextColor(currentColor)
        binding.tvEpisodeCount.setTextColor(currentColor)
        
        // Divider color (if it's gold, it should also fade)
        val currentDividerColor = if (currentColor == normalColor) outlineColor else currentColor
        binding.viewDivider.setBackgroundColor(currentDividerColor)
    }

    private fun getHeaderPositionForPosition(position: Int): Int {
        var pos = position
        while (pos >= 0) {
            if (adapter.getItemViewType(pos) == EpisodeFeedAdapterV2.TYPE_DATE_HEADER) {
                return pos
            }
            pos--
        }
        return -1
    }

    private fun getHeaderView(parent: RecyclerView, position: Int): View {
        val lastHeader = currentHeader
        if (lastHeader != null && lastHeader.first == position) {
            return lastHeader.second
        }

        val headerView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_episode_header_v2,
            parent,
            false
        )
        
        val item = adapter.actualList[position] as EpisodeFeedAdapterV2.FeedItem.DateHeader
        val binding = ItemEpisodeHeaderV2Binding.bind(headerView)
        
        binding.tvDateHeader.text = item.date
        binding.tvEpisodeCount.text = "${item.episodeCount} EPISODES"
        headerView.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.gray_1000))

        currentHeader = position to headerView
        return headerView
    }

    private fun drawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0f, 0f)
        header.draw(c)
        c.restore()
    }

    private fun moveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int, currentHeaderPos: Int): View? {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.bottom > contactPoint && child.top <= contactPoint) {
                return child
            }
        }
        return null
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width
        )
        val childHeight = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams.height
        )

        view.measure(childWidth, childHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}
