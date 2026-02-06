package com.my.mapupassessment.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.my.mapupassessment.data.local.entities.SessionPolylineEntity

@Dao
interface PolylineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPolyline(entity: SessionPolylineEntity)

    @Query("SELECT encodedPolyline FROM session_polylines WHERE sessionId = :sessionId")
    suspend fun getPolyline(sessionId: Long): String?

    @Query("DELETE FROM session_polylines WHERE sessionId = :sessionId")
    suspend fun deleteSessionPolyline(sessionId: Long)

    @Query("DELETE FROM session_polylines")
    suspend fun nukeSessionPolylineTable()
}