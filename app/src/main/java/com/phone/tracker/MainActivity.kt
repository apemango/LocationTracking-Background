package com.phone.tracker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.phone.tracker.data.local.PreferencesManager
import com.phone.tracker.recevier.LocationService
import com.phone.tracker.ui.AttendanceiHistory
import com.phone.tracker.ui.home.HomeScreen
import com.phone.tracker.ui.home.MainViewModel
import com.phone.tracker.ui.theme.PotterComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var locationReceiver: BroadcastReceiver

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                checkGpsAndStartService()
            } else {
                showPermissionDeniedMessage()
            }
        }

        // Register the broadcast receiver
        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "LOCATION_UPDATE") {
                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)
                    Log.d(
                        "MainActivity",
                        "Received Location: Latitude $latitude, Longitude $longitude"
                    )
                    mainViewModel.updateLocation(latitude, longitude)
                }
            }
        }

        IntentFilter(IntentFilter("LOCATION_UPDATE")).also {
            registerReceiver(locationReceiver, it)
        }

        setContent {
            PotterComposeTheme {
                val checkInState by mainViewModel.checkInState.collectAsState() // Collect state from the ViewModel
                val checkOutState by mainViewModel.checkOutState.collectAsState() // Collect state from the ViewModel

                val updateLocation by mainViewModel.location.collectAsState()
                val trackingState by mainViewModel.tracking.collectAsState()

                var isCheckInOutState = rememberSaveable() { mutableStateOf(false) }

                var checkInId = remember { mutableStateOf("") }
                var userId = remember { mutableStateOf(preferencesManager.userIdGet()) }
                var isReadyCheckInOut by rememberSaveable { mutableStateOf(mainViewModel.tracking.value) }

                val context = this
                LaunchedEffect(updateLocation, checkInState, checkOutState, trackingState) {

                    isCheckInOutState.value = trackingState

//                    Log.e("Checking details --> ", "onCreate: "+checkInState.checkIn.first().checkInId )

                    if (!checkInState.checkIn.isNullOrEmpty()) {
                        checkInId.value = checkInState.checkIn.first().checkInId
                        userId.value = checkInState.checkIn.first().checkInId
                        preferencesManager.saveCheckIn(checkInState.checkIn.first().checkInId)
                    }

                    Log.e("TAG", "onCreate:  state of api "+checkOutState.status )
                    if (checkOutState.status==1) {/*
                        preferencesManager.trackingStatus(false)
                        mainViewModel.trackingOnOff()
                        stopLocationService(context as Activity)*/
                    }

                    isReadyCheckInOut = updateLocation.latitude != 0.0 && updateLocation.longitude != 0.0
                }


                HomeScreen(preferencesManager,
                    checkInState.checkIn,
                    longitude = updateLocation.longitude, latitude = updateLocation.latitude,
                    context = this,
                    isCheckedIn = isCheckInOutState.value,
                    onCheckIn = {
                        checkLocationPermission()
                        Log.e("TAG", "onCreate: status live --- > " + isCheckInOutState.value)
                        if (isReadyCheckInOut && !isCheckInOutState.value) {
                            mainViewModel.checkIn(
                                preferencesManager.userIdGet().toLong(),
                                updateLocation.latitude,
                                updateLocation.longitude,
                                "Delhi"
                            )
                        } else {
                            Toast.makeText(
                                this,
                                "Please retry location not getting properly ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onCheckOut = {
                        if (isReadyCheckInOut) {
                            mainViewModel.checkOut(
                                preferencesManager.userIdGet().toLong(),
                                preferencesManager.getCheckIn().toLong(), updateLocation.latitude.toLong(),
                                updateLocation.longitude.toLong(), "NSB", "23"
                            )
                            stopLocationService(this)
                            preferencesManager.trackingStatus(false)
                            mainViewModel.trackingOnOff()

                        } else {
                            Toast.makeText(
                                this,
                                "Please retry location not getting properly ",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, showHistory = {
                        this.startActivity(Intent(this, AttendanceiHistory::class.java))
                    }
                )
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkGpsAndStartService()
            }

            else -> {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    showRationaleDialog()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    private fun checkGpsAndStartService() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Prompt user to enable GPS
            showGpsEnableDialog()
        } else {
            // Start the location service if GPS is enabled
            startLocationService()
            mainViewModel.trackingOnOff()
//            isCheckedIn = true
        }
    }

    private fun showGpsEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable GPS")
            .setMessage("Please enable GPS to use location services.")
            .setPositiveButton("Settings") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRationaleDialog() {
        Toast.makeText(
            this,
            "Location access is required to provide location-based services.",
            Toast.LENGTH_LONG
        ).show()
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Location access denied.", Toast.LENGTH_LONG).show()
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopLocationService(context: Context) {
        val intent = Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
}

