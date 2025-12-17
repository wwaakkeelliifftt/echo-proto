package com.example.echo_proto.ui.common

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes

class ActionModeHelper(
    @MenuRes private val menuResId: Int,
    private val onActionItemClicked: (itemId: Int) -> Unit,
    private val onDestroyActionMode: () -> Unit
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(menuResId, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        item?.itemId?.let { onActionItemClicked(it) }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        onDestroyActionMode()
    }
}
