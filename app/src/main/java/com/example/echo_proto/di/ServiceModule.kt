package com.example.echo_proto.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): ExoPlayer {
        return ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
        }
    }

    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(@ApplicationContext context: Context): DefaultDataSource.Factory {
        return DefaultDataSource.Factory(context)
    }

//    @ServiceScoped
//    @Provides
//    fun provideRareDateSourceFactory(@ApplicationContext context: Context) =
//        DefaultDataSourceFactory(context, Util.getUserAgent(context, "app name"))


}