package com.example.echo_proto.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.echo_proto.R
import com.example.echo_proto.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FeedFilterListDialogFragment(
    context: Context,
    private val filterSet: Set<String>
) : DialogFragment() {

    private var inputFilterEditText = EditText(context)
    private val setOfFilter = TextView(context).apply {
        text = "Вы добавили:\n filterSet"
    }
    private var listenerAdd: ((String) -> Unit)? = null

    fun setListener(update: (String) -> Unit) {
        listenerAdd = update
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Добавить новый фильтр")
            .setMessage(Constants.FILTER_DIALOG_DESCRIPTION)
            .setMessage("Ваши фильтры $filterSet")
            .setView(inputFilterEditText)
            .setPositiveButton("Добавить") { _, _ ->
                val text = inputFilterEditText.text.toString()
                listenerAdd?.invoke(text)
                Toast.makeText(requireContext(), "add: $text", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Завершить", null)
            .create()

    }

    companion object {
        fun newInstance() {
            // todo : return this FeedFilterListDialogFragment with parameters
        }
    }

}