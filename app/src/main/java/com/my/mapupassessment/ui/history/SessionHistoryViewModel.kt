package com.my.mapupassessment.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.my.mapupassessment.data.local.SessionDao
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.repository.SessionLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val sessionLocationRepository: SessionLocationRepository
) : ViewModel() {

//    val allSessions: LiveData<List<SessionEntity>> = liveData {
//        emit(sessionLocationRepository.getAllSessions())
//    }
    val allSessions: LiveData<List<SessionEntity>> = liveData {
        sessionLocationRepository.getAllSessions().collect { sessions ->
            emit(sessions)
        }
    }

}