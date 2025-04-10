package com.example.echo_proto.util

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.echo_proto.R
import com.google.android.material.snackbar.Snackbar

enum class SnackbarStyle { ERROR, INFO }

object SnackbarHelper {

    private val handler = Handler(Looper.getMainLooper())
    private val pendingRunnables = mutableMapOf<Snackbar, Runnable>()

    fun showError(
        rootView: View,
        anchorView: View,
        message: String,
        duration: Int = Snackbar.LENGTH_INDEFINITE,
        autoDismissDelay: Long = 10000L
    ): Snackbar {
        return createSnackbar(rootView, anchorView, message, duration).apply {
            setErrorStyle()
            setAutoDismiss(autoDismissDelay)
            show()
        }
    }

    fun showInfo(
        rootView: View,
        anchorView: View,
        message: String,
        duration: Int = Snackbar.LENGTH_INDEFINITE
    ): Snackbar {
        return createSnackbar(rootView, anchorView, message, duration).apply {
            setInfoStyle()
            show()
        }
    }

    fun showWithAction(
        rootView: View,
        anchorView: View,
        message: String,
        actionText: String,
        action: () -> Unit,
        style: SnackbarStyle = SnackbarStyle.INFO,
        autoDismissDelay: Long = 10000L
    ): Snackbar {
        return createSnackbar(rootView, anchorView, message, Snackbar.LENGTH_INDEFINITE).apply {
            when (style) {
                SnackbarStyle.ERROR -> setErrorStyle()
                SnackbarStyle.INFO -> setInfoStyle()
            }
            setAction(actionText) {
                action()
                dismiss()
            }
            setAutoDismiss(autoDismissDelay)
            show()
        }
    }

    private fun createSnackbar(
        rootView: View,
        snackbarAnchorView: View,
        message: String,
        duration: Int
    ): Snackbar {
//        val rootView = snackbarAnchorView.rootView.findViewById<View>(android.R.id.content)
        return Snackbar.make(rootView, message, duration).apply {
            anchorView =  snackbarAnchorView // if (snackbarAnchorView.isAttachedToWindow) snackbarAnchorView else null
            animationMode = Snackbar.ANIMATION_MODE_FADE
            view.minimumHeight = snackbarAnchorView.context.resources.getDimensionPixelSize(R.dimen.snackbar_height)
            setTextMaxLines(4)
        }
    }

    private fun Snackbar.setErrorStyle() {
        setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_error_background))
        setTextColor(ContextCompat.getColor(context, R.color.snackbar_error_text))
        setActionTextColor(ContextCompat.getColor(context, R.color.snackbar_action_text))
        setAction("Закрыть") { dismiss() }

        // close sb when tap out of Snackbar
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) dismiss()
            true
        }
    }

    private fun Snackbar.setInfoStyle() {
        setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_info_background))
        setTextColor(ContextCompat.getColor(context, R.color.snackbar_info_text))
        setActionTextColor(ContextCompat.getColor(context, R.color.snackbar_action_text))
        setAction("Ok") { dismiss() }

    }

    private fun Snackbar.setAutoDismiss(delayMillis: Long) {
        val runnable = Runnable { dismiss() }
        pendingRunnables[this] = runnable
        handler.postDelayed(runnable, delayMillis)

        addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                pendingRunnables.remove(transientBottomBar)?.let {
                    handler.removeCallbacks(it)
                }
            }
        })
    }

}