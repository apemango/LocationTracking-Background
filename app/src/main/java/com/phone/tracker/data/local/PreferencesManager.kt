package com.phone.tracker.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberPrefManager(): PreferencesManager {
    val context = LocalContext.current
    return remember { PreferencesManager(context) }
}

class PreferencesManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    // Public method to edit preferences
    fun editPreference(action: (SharedPreferences.Editor) -> Unit) {
        val editor = sharedPreferences.edit()
        action(editor)
        editor.apply()
    }

    fun saveString(key: String, value: String) {
        editPreference { editor ->
            editor.putString(key, value)
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    fun saveCheckIn(checkIn: String) {
        editPreference { editor ->
            editor.putString("CheckIn", checkIn)
        }
    }

    fun getCheckIn(): String = getString("CheckIn")

    val keyUserId = ""

    fun userIdSave(userId: String) {
        editPreference {
            it.putString(keyUserId, userId)
        }
    }

    fun userIdGet(): String = getString(keyUserId)

    private val keyLatitude = "Latitude"
    private val keyLongitude = "Longitude"

    fun saveLocation(latitude: Double, longitude: Double) {
        editPreference { editor ->
            editor.putFloat(keyLatitude, latitude.toFloat())
            editor.putFloat(keyLongitude, longitude.toFloat())
        }
    }

    fun getLatitude(defaultValue: Double = 0.0): Double {
        return sharedPreferences.getFloat(keyLatitude, defaultValue.toFloat()).toDouble()
    }

    fun getLongitude(defaultValue: Double = 0.0): Double {
        return sharedPreferences.getFloat(keyLongitude, defaultValue.toFloat()).toDouble()
    }
    fun trackingStatus(isOn: Boolean? = null): Boolean {
        if (isOn != null) {
            editPreference { it.putBoolean("tracking", isOn) }
        }
        return sharedPreferences.getBoolean("tracking", false)
    }


}