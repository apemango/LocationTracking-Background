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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    init {
        _tracking.value = preferencesManager.trackingStatus()
        _isLoading.value = false
        preferencesManager.userIdGet()
    }

    fun trackingOnOff(state: Boolean? = null) {
        val currentStatus = preferencesManager.trackingStatus()
        if (state != null) {
            _tracking.value = preferencesManager.trackingStatus(state)
        } else {
            _tracking.value = preferencesManager.trackingStatus(currentStatus)
        }
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
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = mainRepository.checkInApi(userId, latitude, longitude, location)
                _checkInState.value = response
                preferencesManager.saveCheckIn(response.checkIn.first().checkInId)
                _isLoading.value = false
            } catch (e: Exception) {
                Log.d("TAG", "login: error " + e.message)
            }
        }
    }

    private val _checkOutState = MutableStateFlow(CheckOutREsposeModel())
    val checkOutState: StateFlow<CheckOutREsposeModel> get() = _checkOutState

    fun checkOut(
        userId: Long,
        checkInId: Long,
        latitude: String,
        longitude: String,
        location: String,
        distance: String,
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = mainRepository.checkoutApi(
                    userId,
                    checkInId,
                    latitude.toDouble(),
                    longitude.toDouble(),
                    location,
                    distance
                )
                _checkOutState.value = response
                _isLoading.value = false
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
        Log.e("SYNC DATA", "updateLocation   ---- > : " + longitude + " " + longitude)
    }

}

data class LocationData(val latitude: Double = 0.0, val longitude: Double = 0.0)

