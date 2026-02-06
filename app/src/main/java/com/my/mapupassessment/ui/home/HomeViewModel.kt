package com.my.mapupassessment.ui.home

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.my.mapupassessment.data.local.SessionDao
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.repository.SessionLocationRepository
import com.my.mapupassessment.service.ForegroundLocationService
import com.my.mapupassessment.service.TrackingServiceController
import com.my.mapupassessment.session.SessionManager
import com.my.mapupassessment.utils.Constants
import com.my.mapupassessment.utils.prefs.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val sharedPrefManager: SharedPrefManager,
    private val sessionLocationDataRepository: SessionLocationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isTracking: StateFlow<Boolean> =
        sharedPrefManager.trackingFlow


    fun toggleTracking() {
        viewModelScope.launch {
//            Log.d("TAG", "toggleTracking: service start stop status => ${_isTracking.value}")
            if (isTracking.value == true) {
                sessionManager.stopSession()
                TrackingServiceController.stop(context)

            } else {
                sessionManager.startNewSession()
                TrackingServiceController.start(context)
                resumeTimerIfNeeded()

            }
        }
    }


//    *************************

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> = _elapsedTime

    private var timerJob: Job? = null

    fun resumeTimerIfNeeded() {
        val sessionId = sessionManager.getActiveSessionId() ?: return

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = sessionLocationDataRepository.getStartTime(sessionId)

            while (isActive) {
                _elapsedTime.value = System.currentTimeMillis() - startTime
                delay(1000)
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _elapsedTime.value = 0L
    }

    fun clearDataBase(){
        viewModelScope.launch (Dispatchers.IO){
            sessionLocationDataRepository.nukeSessionTable()
            sessionLocationDataRepository.nukeSessionPolyline()
        }
    }
}