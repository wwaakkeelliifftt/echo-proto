package com.example.echo_proto.ui.view

import android.view.MenuInflater
import androidx.appcompat.widget.Toolbar

/**
 * привязка и наполнение тулбара в каждом фрагменте
 * */
interface ToolbarConfigurator {
    fun configureToolbar(toolbar: Toolbar, menuInflater: MenuInflater)
}