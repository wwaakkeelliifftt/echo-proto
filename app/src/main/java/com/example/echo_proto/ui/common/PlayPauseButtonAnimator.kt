package com.example.echo_proto.ui.common

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView

object PlayPauseButtonAnimator {
    private const val DEFAULT_DURATION = 444L
    private val interpolator = AccelerateDecelerateInterpolator()

    fun animate(
        activeView: ImageView,
        ghostView: ImageView,
        isPlaying: Boolean,
        duration: Long = DEFAULT_DURATION,
        iconResolver: (Boolean) -> Int
    ) {
        val outgoingDrawable = activeView.drawable?.constantState?.newDrawable()?.mutate()
        if (outgoingDrawable != null) {
            ghostView.apply {
                visibility = View.VISIBLE
                alpha = 1f
                rotation = 0f
                setImageDrawable(outgoingDrawable)
                animate().cancel()
                animate()
                    .rotation(90f)
                    .alpha(0f)
                    .setDuration(duration)
                    .setInterpolator(interpolator)
                    .withEndAction {
                        visibility = View.GONE
                        rotation = 0f
                    }
                    .start()
            }
        }

        activeView.apply {
            animate().cancel()
            setImageResource(iconResolver(isPlaying))
            alpha = 0f
            rotation = -90f
            animate()
                .rotation(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
        }
    }
}
