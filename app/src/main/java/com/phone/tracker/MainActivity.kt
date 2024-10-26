package com.phone.tracker

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.phone.tracker.data.local.PreferencesManager
import com.phone.tracker.data.local.rememberPrefManager
import com.phone.tracker.location.LocationService
import com.phone.tracker.recevier.LocationServiceHello
import com.phone.tracker.ui.AttendanceiHistory
import com.phone.tracker.ui.componet.CircularProgressBar
import com.phone.tracker.ui.home.HomeScreen
import com.phone.tracker.ui.home.MainViewModel
import com.phone.tracker.ui.login.LoginActivity
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

    override fun onStart() {
        super.onStart()
//        startLocationService()
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

                val loading by mainViewModel.isLoading.collectAsState()

                var isLoading = remember { mutableStateOf(false) }
                isLoading.value.CircularProgressBar(onDismissRequest = { isLoading.value = false })

                LaunchedEffect(checkInState, checkOutState, loading) {
                    isLoading.value = loading
                    if (checkInState.checkIn.isNotEmpty()) {
                        Log.e("check", "NETWORK CALL onCreate: checkInState " + checkInState)
                        startLocationService()
                    }

                    if (checkOutState.checkOut.isNotEmpty()) {
                        Log.e("check", "Checkout state ::: " + checkOutState.toString())
                        stopLocationService()
                    }
                }

                var pref = rememberPrefManager()

                HomeScreen(updateLocation, checkInState, trackingState, onCheckIn = {
                    checkLocationPermission()
                    checkGpsAndStartService()

                    mainViewModel.checkIn(
                        pref.userIdGet().toLong(),
                        updateLocation.latitude,
                        updateLocation.longitude,
                        "UP"
                    )

                }, onCheckOut = {

                    checkLocationPermission()
                    if (checkGpsAndStartService()) {
                        if (updateLocation.longitude != 0.0 && updateLocation.latitude != 0.0) {
                            mainViewModel.checkOut(
                                pref.userIdGet().toLong(),
                                pref.getCheckIn().toLong(),
                                updateLocation.latitude.toString(),
                                updateLocation.longitude.toString(),
                                "Up",
                                "10"
                            )
                        } else {
                            Toast.makeText(
                                this,
                                "please retry not detecting your location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, history = {
                    startActivity(Intent(this, AttendanceiHistory::class.java))
                }, logout = {
                    preferencesManager.clear()
                    startActivity(Intent(this, LoginActivity::class.java))
                }, detectLocation = {
                    checkLocationPermission()
                    if(checkGpsAndStartService()){
                        val context = this
                        Intent(context, LocationService::class.java).apply {
                            action = LocationService.ACTION_SERVICE_START
                            context.startService(this)
                        }
                    }
                })
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

    private fun checkGpsAndStartService(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Prompt user to enable GPS
            showGpsEnableDialog()
            return false
        } else {
            // Start the location service if GPS is enabled
            startLocationService()
            return true
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
        val context = this
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_SERVICE_START
            context.startService(this)
        }
        mainViewModel.trackingOnOff(true)
    }

    private fun stopLocationService() {
        val context = this
        Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_SERVICE_STOP
            context.startService(this)
        }

        mainViewModel.trackingOnOff(false)
    }

}

