package com.example.echo_proto.di

import android.app.Application
import androidx.room.Room
import com.example.echo_proto.data.local.FeedDatabase
import com.example.echo_proto.data.remote.FeedApi
import com.example.echo_proto.data.repository.FeedRepositoryImpl
import com.example.echo_proto.domain.repository.FeedRepository
import com.prof.rssparser.Parser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun getFeedRepository(db: FeedDatabase, api: FeedApi): FeedRepository {
        return FeedRepositoryImpl(db, api)
    }

}