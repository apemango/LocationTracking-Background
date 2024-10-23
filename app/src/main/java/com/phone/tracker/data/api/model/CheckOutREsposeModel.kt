package com.phone.tracker.data.api.model

data class CheckOutREsposeModel(
    val checkOutDate: String ="",
    val checkOutTime: String ="",
    val status: Int =-1,
    val userId: String  =""
)