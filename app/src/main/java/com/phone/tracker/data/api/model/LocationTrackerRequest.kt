package com.example.myapplication.ui.model

data class LocationTrackerRequest(
    val checkInId: String,
    val latitude: Double,
    val location: String,
    val longitude: Double,
    val userId: String
)