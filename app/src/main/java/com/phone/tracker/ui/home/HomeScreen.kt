package com.phone.tracker.ui.home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phone.tracker.R
import com.phone.tracker.data.api.model.CheckIn
import com.phone.tracker.data.local.PreferencesManager

@Composable
fun HomeScreen(
    preferencesManager: PreferencesManager, checkInDetails: List<CheckIn>,
    latitude: Double,
    longitude: Double,
    context: Context,
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    showHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            showHistory.invoke()
        }) {
            Text("History ")

        }
        // Profile Logo
        Image(
            painter = painterResource(id = R.drawable.ic_icon), // Replace with your logo resource
            contentDescription = "Profile Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))


        // User Name
        Text(
            text = "User Id " + preferencesManager.userIdGet(), fontSize = 24.sp
        )

        // Phone Number
        Text(
            text = "Check Id " + if (!checkInDetails.isNullOrEmpty()) {
                checkInDetails.first().checkInId
            } else {
                "Please retry "
            }, fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
        // Phone Number
        Text(text = "Location > : $latitude , $longitude", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // Check In / Check Out Button
        if (!isCheckedIn) {
            Button(onClick = {
                onCheckIn()
            }, modifier = Modifier.size(200.dp, 50.dp)) {
                Text("Check In")
            }
        } else {
            Button(onClick = {
                onCheckOut()
            }, modifier = Modifier.size(200.dp, 50.dp)) {
                Text("Check Out")
            }
        }
    }
}


