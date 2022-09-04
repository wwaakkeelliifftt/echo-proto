package com.example.echo_proto.data.repository

import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.data.local.entity.EpisodeEntity
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.domain.repository.MediaServiceContentRepository
import com.example.echo_proto.ui.viewmodels.ViewModelState
import com.example.echo_proto.util.Constants
import com.example.echo_proto.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MediaServiceContentRepositoryImpl @Inject constructor(
    private val db: FeedDatabase
): MediaServiceContentRepository {

//    override suspend fun loadEpisodesToPlayerListFromActualFragment(
//        state: ViewModelState
//    ): Flow<Resource<List<Episode>>> = flow {
//        emit(Resource.Loading(data = emptyList()))
//        when (state) {
//            is ViewModelState.Queue -> {}
//            is ViewModelState.Feed -> {}
//            is ViewModelState.FeedPersonal -> {}
//            is ViewModelState.Downloads -> {}
//            is ViewModelState.Favorites -> {}
//            else -> { state.filter?.let { filter -> getChannelFeed(filter) } }
//        }
//    }
//
//    private fun getChannelFeed(filter: String) {
//
//    }

    override suspend fun getEpisodeById(id: Int): Flow<Resource<Episode>> = flow {
        emit(Resource.Loading())
        try {
            val result = db.dao.getEpisodeById(id = id)
            emit(Resource.Success(data = result.toEpisode()))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message))
        }
    }

    override suspend fun getQueueEpisodesList() = flow {
        emit(Resource.Loading(data = emptyList()))
        try {
            val result = db.dao.getQueueFeed().map { it.toEpisode() }
            if (!result.isNullOrEmpty()) {
                emit(Resource.Success(data = result))
            }
        } catch (e: Exception) {
            emit(Resource.Error(data = emptyList(), message = Constants.DATABASE_ERROR_MESSAGE))
        }
    }
    //        .stateIn(CoroutineScope(Dispatchers.IO)) --- return a "hot" SharedFlow

}
