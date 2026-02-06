package com.my.mapupassessment.di

import android.content.Context
import androidx.room.Room
import com.my.mapupassessment.data.local.AppDatabase
import com.my.mapupassessment.data.local.PolylineDao
import com.my.mapupassessment.data.local.SessionDao
import com.my.mapupassessment.repository.SessionLocationRepository
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "mapup_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSessionLocationRepository(
        sessionDao: SessionDao,
        polylineDao: PolylineDao,

    ): SessionLocationRepository {
        return SessionLocationRepository(sessionDao = sessionDao, polylineDao = polylineDao)
    }

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()


    @Provides
    fun providePolylineDaoDao(db: AppDatabase): PolylineDao = db.polylineDao()
}