package com.example.echo_proto.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.echo_proto.R
import com.example.echo_proto.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EmptyDatabaseDialogFragment: DialogFragment() {

    private var listenerUpdate: (() -> Unit)? = null

    fun setListener(update: () -> Unit) {
        listenerUpdate = update
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("<-- такая вот иконка")
            .setMessage(Constants.DATABASE_EMPTY_MESSAGE)
            .setIcon(R.drawable.ic_refresh)
            .setPositiveButton("Супер, так и сделаю!") { _, _ ->
                listenerUpdate?.invoke()
            }
            .create()
    }

}