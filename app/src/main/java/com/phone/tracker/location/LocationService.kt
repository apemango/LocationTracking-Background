package com.phone.tracker.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.phone.tracker.R
import com.phone.tracker.data.api.ApiEndPoints
import com.phone.tracker.data.local.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service(), LocationUpdatesCallBack {
    private val TAG = LocationService::class.java.simpleName

    private lateinit var gpsLocationClient: GPSLocationClient
    private var notification: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null

    @Inject
    lateinit var apiService: ApiEndPoints

    @Inject
    lateinit var preferencesManager: PreferencesManager



    override fun onCreate() {
        super.onCreate()
        gpsLocationClient = GPSLocationClient()
        gpsLocationClient.setLocationUpdatesCallBack(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SERVICE_START -> startService()
            ACTION_SERVICE_STOP -> stopService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object {
        const val ACTION_SERVICE_START = "ACTION_START"
        const val ACTION_SERVICE_STOP = "ACTION_STOP"
    }


    private fun startService() {
        gpsLocationClient.getLocationUpdates(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Searching...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        startForeground(1, notification?.build())
    }

    private fun stopService() {
        gpsLocationClient.setLocationUpdatesCallBack(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun locationException(message: String) {
        Log.d(TAG, message)
    }

    override fun onLocationUpdate(location: Location) {
        val updatedNotification = notification?.setContentText(
            "Location: (${location.latitude}, ${location.longitude})"
        )

        val intent = Intent("LOCATION_UPDATE")
        intent.putExtra("latitude", location.latitude)
        intent.putExtra("longitude", location.longitude)
        sendBroadcast(intent)

        notificationManager?.notify(1, updatedNotification?.build())

        uploadLocationToServer(location.latitude,location.longitude)
    }

    private fun uploadLocationToServer(latitude: Double, longitude: Double) {
        val checkInId = preferencesManager.getCheckIn()
        val userId = preferencesManager.userIdGet()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!checkInId.isNullOrBlank() && !userId.isNullOrBlank()) {
                    val response = apiService.uploadLocation(
                        checkInId = checkInId.toLong(),
                        latitude,
                        "NSP",
                        longitude,
                        userId.toLong()
                    )
                } else {
                    Log.e(
                        "FATAL ERROR ",
                        "uploadLocationToServer: checkInId $checkInId userId$userId"
                    )
                }
            } catch (e: Exception) {
                Log.e("LocationServiceHello", "Error uploading location", e)
            }
        }
    }

}