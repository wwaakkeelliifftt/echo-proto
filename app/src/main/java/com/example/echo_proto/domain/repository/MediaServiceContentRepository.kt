package com.example.echo_proto.domain.repository

import androidx.lifecycle.ViewModel
import com.example.echo_proto.data.local.entity.EpisodeEntity
import com.example.echo_proto.domain.model.Episode
import com.example.echo_proto.ui.viewmodels.ViewModelState
import com.example.echo_proto.util.Resource
import kotlinx.coroutines.flow.Flow

interface MediaServiceContentRepository {

    suspend fun getQueueEpisodesList(): Flow<Resource<List<Episode>>>

    suspend fun getEpisodeById(id: Int): Flow<Resource<Episode>>

//    suspend fun loadEpisodesToPlayerListFromActualFragment(state: ViewModelState): Flow<Resource<List<Episode>>>

}
