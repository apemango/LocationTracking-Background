package com.phone.tracker.data.api.model

import com.google.gson.annotations.SerializedName

data class CheckOutREsposeModel(
    @SerializedName("checkOut" ) var checkOut : ArrayList<CheckOut> = arrayListOf()
)
data class CheckOut (

    @SerializedName("userId"       ) var userId       : String? = null,
    @SerializedName("checkOutDate" ) var checkOutDate : String? = null,
    @SerializedName("checkOutTime" ) var checkOutTime : String? = null,
    @SerializedName("status"       ) var status       : Int?    = null

)