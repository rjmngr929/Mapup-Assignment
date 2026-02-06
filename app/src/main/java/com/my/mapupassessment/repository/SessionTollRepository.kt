package com.my.mapupassessment.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.my.mapupassessment.Helper.NetworkHelper
import com.my.mapupassessment.api.TollAPI
import com.my.mapupassessment.data.TollPriceRequest
import com.my.mapupassessment.data.Units
import com.my.mapupassessment.data.Vehicle
import com.my.mapupassessment.data.response.TollPriceResponse
import com.my.mapupassessment.di.exception.NetworkExceptionHandler
import com.my.mapupassessment.utils.NetworkResult
import org.json.JSONObject
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionTollRepository @Inject constructor(private val tollApi: TollAPI, private val networkHelper: NetworkHelper, private val exceptionHandler: NetworkExceptionHandler) {

//    ***************** API Toll Price Manage *******************************************************
    val _tollResponseLiveData = MutableLiveData<NetworkResult<TollPriceResponse>>()
    val tollResponseLiveData: LiveData<NetworkResult<TollPriceResponse>>
        get() = _tollResponseLiveData

    suspend fun fetchTollPrice(mapProvider: String, polyLine: String, vehicleType: String, currency: String) {
        _tollResponseLiveData.postValue(NetworkResult.Loading())
        if(networkHelper.isNetworkConnected()) {
            try {
                val response = tollApi.fetchTollPrice(
                    TollPriceRequest(
                        mapProvider = mapProvider,
                        polyline = polyLine,
                        vehicle = Vehicle(
                            type = vehicleType
                        ),
                        units = Units(
                            currency = currency
                        )
                    )
                )
                if (response.isSuccessful) {
                    handleAddBrandResponse(response)
                } else {
                    try {
                        val jsonString = response.errorBody()?.toString()
                        Log.d("TAG", "fetchTollPrice: error create by ${jsonString}")
                        if(!jsonString.isNullOrEmpty()){
                            val msg = JSONObject(jsonString).getString("message")
                            Log.d("TAG", "fetchTollPrice: error create by ${msg}")
                            _tollResponseLiveData.postValue( NetworkResult.Error(message = msg))
                        }else{
                            _tollResponseLiveData.postValue( NetworkResult.Error(message = "Something went wrong, Please try again later."))
                        }

                    }catch (e: Exception){
                        Log.d("TAG", "fetchTollPrice: data => upper ${e}")
                        _tollResponseLiveData.postValue(
                            NetworkResult.Error(response.errorBody()?.string()
                                ?.let { JSONObject(it) }?.getString("message")))
                    }
                }

            } catch (e: Exception) {
                Log.d("TAG", "fetchTollPrice: error is $e")
                _tollResponseLiveData.postValue(
                    NetworkResult.Error(
                        exceptionHandler.handleException(
                            e
                        )
                    )
                )
            }
        }else{
            _tollResponseLiveData.postValue(
                NetworkResult.Error("No internet connection" )
            )
        }
    }

    private fun handleAddBrandResponse(response: Response<TollPriceResponse>) {
        if (response.isSuccessful && response.body() != null) {
            _tollResponseLiveData.postValue(NetworkResult.Success(response.body()!!))
        }
        else if(response.errorBody()!=null){
            val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
            _tollResponseLiveData.postValue(NetworkResult.Error(errorObj.getString("message")))
        }
        else{
            _tollResponseLiveData.postValue(NetworkResult.Error("Something Went Wrong"))
        }
    }
//    ***************** API Toll Price Manage *******************************************************

}