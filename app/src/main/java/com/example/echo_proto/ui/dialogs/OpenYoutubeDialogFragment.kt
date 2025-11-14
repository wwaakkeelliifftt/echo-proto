package com.example.echo_proto.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.example.echo_proto.R
import timber.log.Timber

class OpenYoutubeDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val youtubeUrl = requireArguments().getString(ARG_YOUTUBE_URL)
        if (!isYoutubeLink(youtubeUrl)) {
            Toast.makeText(requireContext(), R.string.youtube_open_error, Toast.LENGTH_SHORT).show()
            dismissAllowingStateLoss()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.youtube_open_title)
            .setMessage(youtubeUrl ?: getString(R.string.youtube_open_error))
            .setPositiveButton(R.string.youtube_open_positive) { _, _ ->
                youtubeUrl?.let { openYoutubeIntent(it) }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun openYoutubeIntent(youtubeUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, youtubeUrl.toUri()).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при открытии интента на ютуб")
            Toast.makeText(requireContext(), R.string.youtube_open_error, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_YOUTUBE_URL = "arg_youtube_url"
        const val TAG = "OpenYoutubeDialogFragment"

        fun newInstance(url: String?): OpenYoutubeDialogFragment {
            return OpenYoutubeDialogFragment().apply {
                arguments = bundleOf(ARG_YOUTUBE_URL to url)
            }
        }

        @JvmStatic
        fun isYoutubeLink(url: String?): Boolean {
            if (url.isNullOrBlank()) return false
            return url.contains("youtube.com", ignoreCase = true) || url.contains("youtu.be", ignoreCase = true)
        }
    }
}

