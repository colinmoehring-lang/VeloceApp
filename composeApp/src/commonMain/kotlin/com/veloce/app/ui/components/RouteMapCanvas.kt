package com.veloce.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veloce.app.ui.theme.CardBg
import com.veloce.app.ui.theme.CardBorder
import com.veloce.app.ui.theme.ElectricLime
import com.veloce.app.ui.theme.NeonCyan
import com.veloce.app.ui.theme.NeonOrange

@Composable
fun RouteMapCanvas(
    coordinates: List<Pair<Double, Double>>,
    selectedIndex: Int = -1,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (coordinates.isEmpty()) return@Canvas

            val minLat = coordinates.minOf { it.first }
            val maxLat = coordinates.maxOf { it.first }
            val minLng = coordinates.minOf { it.second }
            val maxLng = coordinates.maxOf { it.second }

            val latRange = (maxLat - minLat).coerceAtLeast(0.0001)
            val lngRange = (maxLng - minLng).coerceAtLeast(0.0001)

            val width = size.width
            val height = size.height

            val points = coordinates.map { (lat, lng) ->
                val x = ((lng - minLng) / lngRange * width).toFloat()
                val y = (height - ((lat - minLat) / latRange * height)).toFloat()
                Offset(x, y)
            }

            // Draw Route Path
            val path = Path()
            path.moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }

            // Shadow / Glow
            drawPath(
                path = path,
                color = NeonCyan.copy(alpha = 0.3f),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Main Route Line
            drawPath(
                path = path,
                color = NeonCyan,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Start / End Markers
            drawCircle(color = ElectricLime, radius = 6.dp.toPx(), center = points.first())
            drawCircle(color = NeonOrange, radius = 6.dp.toPx(), center = points.last())

            // Highlight scrubbed point if selected
            if (selectedIndex in points.indices) {
                val scrubPt = points[selectedIndex]
                drawCircle(color = Color.White, radius = 10.dp.toPx(), center = scrubPt)
                drawCircle(color = NeonCyan, radius = 6.dp.toPx(), center = scrubPt)
            }
        }

        // Overlay Label
        Text(
            text = "GPS Route Map",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
        )
    }
}
