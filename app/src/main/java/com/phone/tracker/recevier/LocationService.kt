package com.phone.tracker.recevier

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.phone.tracker.data.local.PreferencesManager
//import com.example.myapplication.data.remote.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.phone.tracker.R
import com.phone.tracker.data.api.ApiEndPoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var apiService: ApiEndPoints

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var timer: Timer? = null // Change from lateinit to nullable


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 2000 // Update every 2 seconds
            fastestInterval = 1000 // Fastest update every second
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // High accuracy
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d(
                        "LocationService",
                        "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                    )
                    updateNotification(location.latitude, location.longitude)

                    preferencesManager.saveLocation(location.latitude, location.longitude)

                    Log.d(
                        "SAVE",
                        "onLocationResult: code ----> " + preferencesManager.getLongitude()
                    )

                    if (preferencesManager.getLongitude() != 0.0 && !preferencesManager.userIdGet()
                            .isNullOrEmpty()
                    ) {
                        //save on server
                        uploadLocationToServer(location.latitude, location.longitude)
                    }
                }
            }
        }
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
                    updateNotificationApiResponse(response.locationTrack.first().toString())
                } else {
                    Log.e(
                        "FATAL ERROR ",
                        "uploadLocationToServer: checkInId $checkInId userId$userId"
                    )
                }
            } catch (e: Exception) {
                Log.e("LocationService", "Error uploading location", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Check for stop action
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(1, createNotification("Fetching location..."))
        startLocationUpdates()
        fetchLastKnownLocation()

        // Start a timer for forced updates
        timer = Timer() // Initialize timer here
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (hasLocationPermissions()) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {

                            // Send broadcast with location data
                            val intent = Intent("LOCATION_UPDATE")
                            intent.putExtra("latitude", location.latitude)
                            intent.putExtra("longitude", location.longitude)
                            sendBroadcast(intent)

                            preferencesManager.saveLocation(location.latitude, location.longitude)
                            Log.d(
                                "LocationService",
                                "Forced Update - Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                            )
                            updateNotification(location.latitude, location.longitude)
                        }
                    }
                }
            }
        }, 0, 2000)

        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (hasLocationPermissions()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.e("LocationService", "Permissions not granted")
        }
    }

    private fun fetchLastKnownLocation() {
        if (hasLocationPermissions()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(
                        "LocationService",
                        "Last Known Latitude: ${location.latitude}, Longitude: ${location.longitude}"
                    )
                    updateNotification(location.latitude, location.longitude)
                } else {
                    Log.e("LocationService", "Last known location is null")
                }
            }.addOnFailureListener { e ->
                Log.e("LocationService", "Failed to get last known location", e)
            }
        } else {
            Log.e("LocationService", "Permissions not granted")
        }
    }

    private fun hasLocationPermissions(): Boolean =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun updateNotification(latitude: Double, longitude: Double) {
        val notificationText = "Tracking Location\nLatitude: $latitude\nLongitude: $longitude"
        val notification = createNotification(notificationText)

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun updateNotificationApiResponse(notification: String) {
        val notificationText = "-> Saved Status \n $notification"
        val notification = createNotification(notificationText)

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)

    }

    private fun createNotification(contentText: String): Notification {
        val channelId = "location_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Create an Intent to stop the service with FLAG_IMMUTABLE for compatibility.
        val stopIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tracking Location")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.ic_stop, "Stop Service", stopPendingIntent) // Adding stop action
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        timer?.cancel() // Cancel the timer when service is destroyed
    }

    private fun stopLocationUpdates() {
        preferencesManager.saveLocation(0.0, 0.0)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP_SERVICE = "com.example.app.ACTION_STOP_SERVICE"
    }
}