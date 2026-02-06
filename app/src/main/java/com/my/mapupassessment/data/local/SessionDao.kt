package com.my.mapupassessment.data.local

import androidx.room.*
import com.my.mapupassessment.data.local.entities.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity) : Long

    @Query("UPDATE sessions SET distance = distance + :distance WHERE id = :sessionId")
    suspend fun addDistance(sessionId: Long, distance: Double)

    @Query("SELECT * FROM sessions")
     fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT startTime FROM sessions WHERE id = :sessionId")
    suspend fun getStartTime(sessionId: Long): Long

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    suspend fun getSessionDataById(sessionId: Long): SessionEntity

    @Query("""
        UPDATE sessions 
        SET endTime = :endTime, duration = :duration 
        WHERE id = :sessionId
    """)
    suspend fun update(sessionId: Long, endTime: Long, duration: Long)

    @Query("DELETE FROM sessions")
    suspend fun nukeSessionTable()
}