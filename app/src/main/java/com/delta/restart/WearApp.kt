package com.delta.restart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*

@Composable
fun WearApp(viewModel: MainViewModel, lastSessionDuration: String = "") {
    val isWalking by viewModel.isWalking.collectAsState()

    Scaffold(
        modifier = Modifier.padding(0.dp),
        timeText = { },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(0.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Yellow Circle at the Top
            SmokingIconWithTitle()

            Spacer(modifier = Modifier.height(16.dp))

            // Start/Stop Button with Dynamic Color and Larger Size
            Button(
                onClick = {
                    log("User tapped isWalking button")
                    viewModel.setWalkingStatus(!isWalking)
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f) // Make the button larger
                    .height(80.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isWalking) Color(0xFFEE675C) else Color(0xFF5BB974), // Red when active, yellow when inactive
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = if (isWalking) "Stop" else "Start",
                    style = MaterialTheme.typography.button
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last Session Information
            Text(
                text = lastSessionDuration,
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SmokingIconWithTitle() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 0.dp)
    ) {
        // Circular background with the icon inside
        Box(
            modifier = Modifier
                .size(36.dp) // Adjust size as needed
                .background(Color(0xFF5BB974), CircleShape), // Yellow background
            contentAlignment = Alignment.Center // Center the icon within the circle
        ) {
            Icon(
                painter = painterResource(id = R.drawable.smoking_solid), // Replace with your icon name
                contentDescription = "Smoking Icon",
                modifier = Modifier.size(24.dp), // Adjust icon size as needed
                tint = Color.Black // Icon color
            )
        }
    }
}