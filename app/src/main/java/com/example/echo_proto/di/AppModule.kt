package com.example.echo_proto.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.echo_proto.data.local.FeedDao
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.data.remote.FeedApi
import com.example.echo_proto.data.repository.FeedRepositoryImpl
import com.example.echo_proto.data.repository.MediaServiceContentRepositoryImpl
import com.example.echo_proto.domain.repository.FeedRepository
import com.example.echo_proto.domain.repository.MediaServiceContentRepository
import com.example.echo_proto.exoplayer.MediaServiceConnection
import com.example.echo_proto.util.Constants
import com.prof.rssparser.Parser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun getXmlParser(app: Application): Parser {
        return Parser.Builder()
            .context(app.applicationContext)
            .cacheExpirationMillis(60L * 60L * 100L) // one hour
            .build()
    }

    @Provides
    @Singleton
    fun provideFeedApi(parser: Parser): FeedApi {
        return FeedApi(parser)
    }

    @Provides
    @Singleton
    fun provideFeedDatabase(app: Application): FeedDatabase {
        return Room.databaseBuilder(
            app,
            FeedDatabase::class.java,
            "feed_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFeedDatabaseDao(database: FeedDatabase): FeedDao = database.dao

    @Provides
    @Singleton
    fun getFeedRepository(db: FeedDatabase, api: FeedApi): FeedRepository {
        return FeedRepositoryImpl(db, api)
    }

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext app: Context): SharedPreferences {
        return app.getSharedPreferences(
            Constants.SHARED_PREFERENCES_INIT_KEY,
            Context.MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun provideMediaServiceConnection(@ApplicationContext app: Context): MediaServiceConnection {
        return MediaServiceConnection(app)
    }

    @Provides
    @Singleton
    fun provideMediaServiceRepository(database: FeedDatabase): MediaServiceContentRepository {
        return MediaServiceContentRepositoryImpl(database)
    }

}