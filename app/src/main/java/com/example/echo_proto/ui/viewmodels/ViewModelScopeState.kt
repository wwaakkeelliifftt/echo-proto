package com.example.echo_proto.ui.viewmodels


interface ViewModelScopeState {
    val scopeState: ViewModelState
}

sealed class ViewModelState(val filter: String? = null) {
    object Queue: ViewModelState()
    object Feed: ViewModelState()
    object FeedPersonal: ViewModelState()
    class Channel(channelName: String) : ViewModelState(filter = channelName)
    object Downloads: ViewModelState()
    object Favorites: ViewModelState()
}