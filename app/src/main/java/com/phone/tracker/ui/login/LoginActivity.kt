package com.phone.tracker.ui.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phone.tracker.ui.home.MainViewModel
import com.phone.tracker.ui.theme.PotterComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PotterComposeTheme {
                val mainViewModel: MainViewModel = viewModel() // Get the viewModel
                val state by mainViewModel.loginState.collectAsState() // Collect state from the ViewModel
                LoginForm(state) {
                    mainViewModel.login(it.username, it.pwd)
                }
            }
        }
    }
}