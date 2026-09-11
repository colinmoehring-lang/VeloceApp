package com.veloce.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.ui.theme.ElectricLime
import com.veloce.app.ui.theme.NeonCyan
import com.veloce.app.ui.theme.NeonOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeed: Double,
    maxSpeed: Double = 250.0,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(currentSpeed.toFloat())

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = 240f
            val startAngle = 150f
            val strokeWidth = 14.dp.toPx()

            // Background Arc
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            val progressSweep = ((animatedSpeed / maxSpeed.toFloat()) * sweepAngle).coerceIn(0f, sweepAngle)
            drawArc(
                color = NeonCyan,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedSpeed.toInt()}",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "KM/H",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }
    }
}

@Composable
fun LeanAngleGauge(
    rollAngle: Double,
    modifier: Modifier = Modifier
) {
    val animatedRoll by animateFloatAsState(rollAngle.toFloat())

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 - 10.dp.toPx()

            // Reference Circle
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius,
                style = Stroke(width = 4.dp.toPx())
            )

            // Lean Needle
            val radians = (animatedRoll - 90) * (PI / 180)
            val endX = center.x + radius * cos(radians).toFloat()
            val endY = center.y + radius * sin(radians).toFloat()

            drawLine(
                color = ElectricLime,
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${kotlin.math.abs(animatedRoll.toInt())}°",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricLime
            )
            Text(
                text = if (animatedRoll < 0) "LINKS" else "RECHTS",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun GForceGauge(
    gForce: Double,
    modifier: Modifier = Modifier
) {
    val animatedG by animateFloatAsState(gForce.toFloat())

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.1f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val sweep = ((animatedG / 3.0f) * 270f).coerceIn(0f, 270f)
            drawArc(
                color = NeonOrange,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedG * 100).toInt() / 100.0}G",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NeonOrange
            )
            Text(
                text = "G-FORCE",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
