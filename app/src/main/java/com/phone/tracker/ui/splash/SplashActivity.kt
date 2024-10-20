package com.phone.tracker.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.phone.tracker.MainActivity
import com.phone.tracker.data.local.rememberPrefManager
import com.phone.tracker.ui.login.LoginActivity
import com.phone.tracker.ui.theme.PotterComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PotterComposeTheme {
                val pref = rememberPrefManager()
                val context = LocalContext.current // Get the current context
                val userId = pref.userIdGet()

                LaunchedEffect(Unit) {
                    val intent = if (!userId.isNullOrEmpty()) {
                        Intent(context, MainActivity::class.java)
                    } else {
                        Intent(context, LoginActivity::class.java)
                    }
                    context.startActivity(intent) // Use context to start the activity
                }

            }
        }
    }
}