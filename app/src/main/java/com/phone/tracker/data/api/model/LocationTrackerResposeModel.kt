package com.example.myapplication.ui.model

data class LocationTrackerResposeModel(
    val locationTrack: List<LocationTrack>
)
data class LocationTrack(
    val status: Int,
    val userId: String
)