package com.my.mapupassessment.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.my.mapupassessment.data.local.SessionDao
import com.my.mapupassessment.data.local.entities.SessionEntity
import com.my.mapupassessment.data.response.TollPriceResponse
import com.my.mapupassessment.repository.SessionLocationRepository
import com.my.mapupassessment.repository.SessionTollRepository
import com.my.mapupassessment.utils.MapUtils.decodePolylineToGeoPoints
import com.my.mapupassessment.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionLocationRepository: SessionLocationRepository,
    private val sessionTollRepository: SessionTollRepository,
) : ViewModel() {

    private val _sessionId = MutableLiveData<Long>()
    val sessionId: LiveData<Long> = _sessionId

    val sessionInfo: LiveData<SessionEntity?> =
        sessionLocationRepository.getAllSessions()
            .map { sessions ->
                sessions.find { it.id == sessionId.value }
            }
            .asLiveData()

    private val _sessionData = MutableLiveData<SessionEntity?>()
    val sessionData: LiveData<SessionEntity?> = _sessionData

    fun getSessionById(sessionId: Long) {
        viewModelScope.launch {
            _sessionData.postValue(sessionLocationRepository.getSessionDataById(sessionId))
        }
    }


//    ********************** PolyLine Data ***********************************

    private val _polyLineData = MutableLiveData<String>()
    val polyLineData: LiveData<String> = _polyLineData

    private val _latlngData = MutableLiveData<List<GeoPoint>>()
    val latlngData: LiveData<List<GeoPoint>> = _latlngData

    fun getPolyLineData(sessionId: Long){
        viewModelScope.launch {
            val encoded = sessionLocationRepository
                .getEncodedPolyline(sessionId)
                ?: return@launch

            val decodedPoints = withContext(Dispatchers.Default) {
                decodePolylineToGeoPoints(encoded)
            }

            _latlngData.postValue(decodedPoints)
            _polyLineData.postValue(encoded)
        }
    }
//    ********************** PolyLine Data ***********************************

    fun setSessionId(id: Long) {
        _sessionId.value = id
    }

//    ********************* Toll Price ResponseModel **************************************
    val tollPriceResponseLiveData: LiveData<NetworkResult<TollPriceResponse>>
        get() = sessionTollRepository.tollResponseLiveData


    fun fetchTollData(mapProvider: String, polyLine: String, vehicleType: String, currency: String){
        viewModelScope.launch {
            sessionTollRepository.fetchTollPrice(mapProvider = mapProvider, polyLine = polyLine, vehicleType = vehicleType, currency = currency)
        }
    }

    fun clearTollRes(){
        sessionTollRepository._tollResponseLiveData.postValue(NetworkResult.Empty())
    }

//    ********************* Toll Price ResponseModel **************************************

}