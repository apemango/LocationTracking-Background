package com.phone.tracker.data.api.model

data class AttendanceResposeModel(
    val AttendanceList: List<Attendance> = emptyList()
)
data class Attendance(
    val checkIn: String,
    val checkOut: String,
    val distance: String,
    val duration: String
)