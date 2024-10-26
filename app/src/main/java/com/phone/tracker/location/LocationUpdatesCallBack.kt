package com.phone.tracker.location

import android.location.Location

interface LocationUpdatesCallBack {
    fun onLocationUpdate(location: Location)
    fun locationException(message: String)
}