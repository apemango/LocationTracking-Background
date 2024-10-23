package com.phone.tracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.phone.tracker.data.api.model.AttendanceResposeModel
import com.phone.tracker.data.local.PreferencesManager
import com.phone.tracker.ui.home.MainViewModel
import com.phone.tracker.ui.theme.PotterComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AttendanceiHistory : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PotterComposeTheme {
                val attendanceState by mainViewModel.attendanceState.collectAsState() // Collect state from the ViewModel

                LaunchedEffect(Unit) {
                    mainViewModel.attendance(preferencesManager.userIdGet())
                }
                ShowHistory(attendanceState)
            }
        }
    }
}

@Composable
fun ShowHistory(attendanceState: AttendanceResposeModel) {
    LazyColumn {
        items(attendanceState.AttendanceList) {
            Column(modifier = Modifier.wrapContentSize()) {
                Row {
                    Text("Check In "+it.checkIn + " - >  checkout " + it.checkOut + "\nDistance " + it.distance + " duration " + it.duration)
                }
                Spacer(modifier = Modifier.fillMaxWidth().height(2.dp).background(color = Color.Black))
            }
        }
    }

}