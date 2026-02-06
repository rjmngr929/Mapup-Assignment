package com.my.mapupassessment.session

import android.content.Intent
import android.util.Log
import com.my.mapupassessment.data.local.SessionDao
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.repository.SessionLocationRepository
import com.my.mapupassessment.service.ForegroundLocationService
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sessionLocationRepository: SessionLocationRepository,
) {
    private val KEY_ACTIVE_SESSION = "active_session"

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    suspend fun startNewSession(): Long {
        val id = sessionLocationRepository.insertSession(
            SessionEntity(startTime = System.currentTimeMillis())
        )
        sharedPrefManager.putLong(KEY_ACTIVE_SESSION, id)
        sharedPrefManager.setTracking(true)

        return id
    }

    fun getActiveSessionId(): Long? {
        val id = sharedPrefManager.getLong(KEY_ACTIVE_SESSION, -1)
        return if (id == -1L) null else id
    }

    suspend fun stopSession() {
        Log.d("TAG", "stopSession: stop session called success")
        val id = getActiveSessionId() ?: return
        val endTime = System.currentTimeMillis()
        sessionLocationRepository.updateSession(id, endTime,  duration = endTime - sessionLocationRepository.getStartTime(id))
        sharedPrefManager.remove(KEY_ACTIVE_SESSION)
        sharedPrefManager.setTracking(false)
    }
}
