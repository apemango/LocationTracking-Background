package com.phone.tracker.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phone.tracker.R
import com.phone.tracker.data.api.model.CheckInResposeModel
import com.phone.tracker.data.local.rememberPrefManager

@Composable
fun HomeScreen(
    updateLocation: LocationData,
    checkInState: CheckInResposeModel,
    status: Boolean,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    history: () -> Unit,
    logout: () -> Unit, detectLocation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Location - > $updateLocation")
        Row {
            Spacer(Modifier.weight(1f))
            Button(onClick = { history.invoke() }) {
                Text("History")
            }
            Spacer(modifier = Modifier.size(10.dp))
            Button(onClick = { logout.invoke() }) {
                Text("Logout")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "Check In : \n" + if (!checkInState.checkIn.isNullOrEmpty()) {
                checkInState.checkIn.toString()
            } else {
                ""
            }
        )
        Spacer(modifier = Modifier.weight(1f))

        var preferencesManager = rememberPrefManager()

        Text(if (status) " On" else "Off", fontSize = 30.sp)

        // Create a Box to hold the image and apply background color
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .background(
                    if (status) Color.Green else Color.Red,
                    shape = RoundedCornerShape(8.dp)
                ) // Optional: Rounded corners
        ) {
            Image(
                painter = if (status)
                    painterResource(R.drawable.baseline_location_searching_24)
                else
                    painterResource(R.drawable.baseline_location_off_24),
                contentDescription = "Profile Logo",
                modifier = Modifier.fillMaxSize() // Fill the box
            )
        }


        Spacer(modifier = Modifier.height(16.dp))


        // User Name
        Text(
            text = "User Id " + preferencesManager.userIdGet(), fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row {
            Log.e("TAG", "HomeScreen: ##### --->  "+updateLocation.longitude + " ==== "+updateLocation.latitude )
            if (updateLocation.longitude != 0.0 && updateLocation.latitude != 0.0){
                if (status) {
                    Button(
                        onClick = {
                            onCheckOut()
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .size(200.dp, 50.dp)
                    ) {
                        Text("Check Out")
                    }
                } else {
                    Button(
                        onClick = {
                            onCheckIn()
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .size(200.dp, 50.dp)
                    ) {
                        Text("Check In")
                    }
                }
            }else{
                Button(
                    onClick = {
                        detectLocation()
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .size(200.dp, 50.dp)
                ) {
                    Text("Detect Location")
                }

            }

        }

    }
}


