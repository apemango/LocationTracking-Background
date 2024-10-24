package com.phone.tracker.data.api

import com.example.myapplication.ui.model.LocationTrackerResposeModel
import com.phone.tracker.data.api.model.AttendanceResposeModel
import com.phone.tracker.data.api.model.Character
import com.phone.tracker.data.api.model.CheckInResposeModel
import com.phone.tracker.data.api.model.CheckOutREsposeModel
import com.phone.tracker.data.api.model.LoginResposeModel
import com.phone.tracker.data.repository.MainRepository
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiEndPoints {
    @GET(ApiConstants.END_POINTS)
    suspend fun getCharacter() : List<Character>


    @Multipart
    @POST("/recib/wapi/locationTrack.php") // Replace with your actual endpoint
    suspend fun uploadLocation(
        @Part("checkInId") checkInId: Long,
        @Part("latitude") latitude: Double,
        @Part("location") location: String,
        @Part("longitude") longitude: Double,
        @Part("userId") userId: Long
    ): LocationTrackerResposeModel


    @Multipart
    @POST("recib/wapi/login.php") // Replace with your actual endpoint
    suspend fun loginApi(
        @Part("userName") username: Long,
        @Part("password") password: Long
    ): LoginResposeModel


    @Multipart
    @POST("recib/wapi/checkIn.php") // Replace with your actual endpoint
    suspend fun checkInApi(
        @Part("userId") username: Long,
        @Part("latitude") latitude: Double,
        @Part("longitude") longitude: Double,
        @Part("location") location: String,
    ): CheckInResposeModel

    @Multipart
    @POST("/recib/wapi/checkOut.php") // Replace with your actual endpoint
    suspend fun checkOutApi(
        @Part("userId") userId: Long,
        @Part("checkInId") checkInId: Long,
        @Part("latitude") latitude: Double,
        @Part("longitude") longitude: Double,
        @Part("location") location: String,
        @Part("distance") distance: String,
    ): CheckOutREsposeModel

    @Multipart
    @POST("/recib/wapi/empAttendanceList.php") // Replace with your actual endpoint
    suspend fun attendanceApi(
        @Part("userId") userId: Long,
    ): AttendanceResposeModel
}