package com.phone.tracker.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phone.tracker.data.api.model.AttendanceResposeModel
import com.phone.tracker.data.api.model.CheckInResposeModel
import com.phone.tracker.data.api.model.CheckOutREsposeModel
import com.phone.tracker.data.api.model.LoginResposeModel
import com.phone.tracker.data.local.PreferencesManager
import com.phone.tracker.data.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {


    private val _loginState = MutableStateFlow(LoginResposeModel())
    val loginState: StateFlow<LoginResposeModel> get() = _loginState

    private val _tracking = MutableStateFlow(false)
    val tracking: StateFlow<Boolean> get() = _tracking

    init {
        _tracking.value = preferencesManager.trackingStatus()
        preferencesManager.userIdGet()
    }

    fun trackingOnOff(){
        val currentStatus = preferencesManager.trackingStatus()
        _tracking.value = preferencesManager.trackingStatus(!currentStatus)
    }


    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = mainRepository.loginApi(username, password)
                _loginState.value = response
            } catch (e: Exception) {
                Log.d("TAG", "login: error" + e.message)
            }
        }
    }

    private val _checkInState = MutableStateFlow(CheckInResposeModel())
    val checkInState: StateFlow<CheckInResposeModel> get() = _checkInState

    fun checkIn(
        userId: Long,
        latitude: Double,
        longitude: Double,
        location: String
    ) {
        viewModelScope.launch {
            try {
                val response = mainRepository.checkInApi(userId, latitude, longitude, location)
                _checkInState.value = response
                preferencesManager.saveCheckIn(response.checkIn.first().checkInId)
            } catch (e: Exception) {
                Log.d("TAG", "login: error" + e.message)
            }
        }
    }

    private val _checkOutState = MutableStateFlow(CheckOutREsposeModel())
    val checkOutState: StateFlow<CheckOutREsposeModel> get() = _checkOutState

    fun checkOut(
        userId: Long,
        checkInId: Long,
        latitude: Long,
        longitude: Long,
        location: String,
        distance: String,
    ) {
        viewModelScope.launch {
            try {
                val response = mainRepository.checkoutApi(
                    userId,
                    checkInId,
                    latitude,
                    longitude,
                    location,
                    distance
                )
                _checkOutState.value = response
            } catch (e: Exception) {
                Log.d("TAG", "login: error checkout api error" + e.message)
            }
        }
    }

    private val _attendanceState = MutableStateFlow(AttendanceResposeModel())
    val attendanceState: StateFlow<AttendanceResposeModel> get() = _attendanceState

    fun attendance(
        userId: String
    ) {
        viewModelScope.launch {
            try {
                val response = mainRepository.attendanceApi(userId)
                _attendanceState.value = response
            } catch (e: Exception) {
                Log.d("TAG", "login: error" + e.message)
            }
        }
    }


    private val _location = MutableStateFlow(LocationData())
    val location: StateFlow<LocationData> get() = _location

    fun updateLocation(latitude: Double, longitude: Double) {
        _location.value = LocationData(latitude, longitude)
    }

}

data class LocationData(val latitude: Double = 0.0, val longitude: Double = 0.0)

