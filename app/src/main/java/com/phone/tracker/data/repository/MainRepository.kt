package com.phone.tracker.data.repository

import android.util.Log
import com.phone.tracker.data.api.ApiEndPoints
import com.phone.tracker.data.api.model.AttendanceResposeModel
import com.phone.tracker.data.api.model.CheckInResposeModel
import com.phone.tracker.data.api.model.CheckOutREsposeModel
import com.phone.tracker.data.api.model.LoginResposeModel
import retrofit2.HttpException
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val apiEndPoints: ApiEndPoints
) {

    suspend fun loginApi(username: String, password: String): LoginResposeModel {
        return try {
            apiEndPoints.loginApi(username.toLong(),password.toLong())
        } catch (e: HttpException) {
            // Handle login error
            Log.e("MainRepository", "Login failed: ${e.message()}")
            throw e // Rethrow if you want to handle it higher up
        }
    }

    suspend fun checkInApi(
        userId: Long,
        latitude: Double,
        longitude: Double,
        location: String
    ): CheckInResposeModel {
        return try {
            apiEndPoints.checkInApi(userId, latitude, longitude, location)
        } catch (e: HttpException) {
            // Handle login error
            Log.e("MainRepository", "Login failed: ${e.message()}")
            throw e // Rethrow if you want to handle it higher up
        }
    }


    suspend fun checkoutApi(
        userId: Long,
        checkInId: Long,
        latitude: Long,
        longitude: Long,
        location: String,
        distance: String,
    ): CheckOutREsposeModel {
        return try {
            apiEndPoints.checkOutApi(userId,
                checkInId, latitude, longitude, location, distance)
        } catch (e: HttpException) {
            // Handle login error
            Log.e("MainRepository Checkout api --- >", "Login failed: ${e.message()}")
            throw e // Rethrow if you want to handle it higher up
        }
    }

    suspend fun attendanceApi(
        userId: String
    ): AttendanceResposeModel {
        return try {
            apiEndPoints.attendanceApi(userId.toLong())
        } catch (e: HttpException) {
            // Handle login error
            Log.e("MainRepository", "Login failed: ${e.message()}")
            throw e // Rethrow if you want to handle it higher up
        }
    }




}