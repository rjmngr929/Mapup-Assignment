package com.my.mapupassessment.repository


import com.my.mapupassessment.data.local.PolylineDao
import com.my.mapupassessment.data.local.SessionDao
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.data.local.entities.SessionPolylineEntity
import com.my.mapupassessment.utils.MapUtils.decodePolylineToGeoPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionLocationRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val polylineDao: PolylineDao
) {

//    **************************** Session Data Set **************************************************************
    suspend fun insertSession(sessionData: SessionEntity) : Long = sessionDao.insert(session = sessionData)

    suspend fun updateSession(sessionId: Long, endTime: Long, duration: Long ): Unit = sessionDao.update(sessionId = sessionId, endTime = endTime, duration = duration)

    suspend fun addDistance(sessionId: Long, distance: Double): Unit = sessionDao.addDistance(sessionId = sessionId, distance = distance)

    suspend fun getStartTime(sessionId: Long): Long = sessionDao.getStartTime(sessionId = sessionId)

    suspend fun getSessionDataById(sessionId: Long): SessionEntity = sessionDao.getSessionDataById(sessionId = sessionId)

    suspend fun nukeSessionTable(): Unit = sessionDao.nukeSessionTable()

    fun getAllSessions(): Flow<List<SessionEntity>> = sessionDao.getAllSessions()
//    **************************** Session Data Set **************************************************************


//    ******************************* PolyLine Data Set **************************************************
    suspend fun saveEncodedPath(
        sessionId: Long,
        encodedPolyline: String
    ) {
        polylineDao.upsertPolyline(
            SessionPolylineEntity(
                sessionId = sessionId,
                encodedPolyline = encodedPolyline,
                updatedAt = System.currentTimeMillis()
            )
        )
    }


    suspend fun getEncodedPolyline(sessionId: Long): String? =
        withContext(Dispatchers.IO) {
            polylineDao.getPolyline(sessionId)
        }

    suspend fun nukeSessionPolyline(): Unit = polylineDao.nukeSessionPolylineTable()

//    ******************************* PolyLine Data Set **************************************************


}






