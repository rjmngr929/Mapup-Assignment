package com.my.mapupassessment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.data.local.entities.SessionPolylineEntity

@Database(
    entities = [SessionEntity::class, SessionPolylineEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun polylineDao(): PolylineDao
}