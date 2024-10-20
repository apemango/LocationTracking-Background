package com.phone.tracker.data.api.model

data class CheckInResposeModel(
    val checkIn: List<CheckIn> = emptyList()
)
data class CheckIn(
    val checkInDate: String,
    val checkInId: String,
    val checkInTime: String,
    val status: Int,
    val userId: String
)