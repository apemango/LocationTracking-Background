package com.phone.tracker.ui.componet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CircularProgressBar(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 10f,
    backgroundColor: Color = Color.Transparent,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            while (true) { // Infinite loop for continuous progress
                for (i in 0..100) {
                    delay(30) // Adjust delay for speed of progress
                    progress = i / 100f // Increment progress
                }
            }
        } else {
            progress = 0f // Reset progress when not loading
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Draw background circle
            drawCircle(color = backgroundColor, radius = size.minDimension / 2)

            // Draw progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}