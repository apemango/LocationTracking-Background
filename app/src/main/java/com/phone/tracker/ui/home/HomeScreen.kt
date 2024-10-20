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

@Composable
fun HomeScreen(
    context: Context,
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Logo
        Image(
            painter = painterResource(id = R.drawable.ic_icon), // Replace with your logo resource
            contentDescription = "Profile Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(text = "John Doe", fontSize = 24.sp)

        // Phone Number
        Text(text = "+1234567890", fontSize = 18.sp)

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
