package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GithubIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF24292F)) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.25f, h * 0.05f, w * 0.05f, h * 0.25f, w * 0.05f, h * 0.5f)
            cubicTo(w * 0.05f, h * 0.7f, w * 0.18f, h * 0.87f, w * 0.36f, h * 0.93f)
            cubicTo(w * 0.38f, h * 0.94f, w * 0.39f, h * 0.92f, w * 0.39f, h * 0.9f)
            cubicTo(w * 0.39f, h * 0.83f, w * 0.39f, h * 0.75f, w * 0.39f, h * 0.65f)
            cubicTo(w * 0.26f, h * 0.68f, w * 0.24f, h * 0.59f, w * 0.24f, h * 0.59f)
            cubicTo(w * 0.22f, h * 0.54f, w * 0.19f, h * 0.52f, w * 0.19f, h * 0.52f)
            cubicTo(w * 0.15f, h * 0.49f, w * 0.19f, h * 0.49f, w * 0.19f, h * 0.49f)
            cubicTo(w * 0.24f, h * 0.50f, w * 0.26f, h * 0.55f, w * 0.26f, h * 0.55f)
            cubicTo(w * 0.30f, h * 0.62f, w * 0.37f, h * 0.60f, w * 0.40f, h * 0.58f)
            cubicTo(w * 0.41f, h * 0.55f, w * 0.42f, h * 0.52f, w * 0.43f, h * 0.50f)
            cubicTo(w * 0.33f, h * 0.49f, w * 0.22f, h * 0.45f, w * 0.22f, h * 0.27f)
            cubicTo(w * 0.22f, h * 0.22f, w * 0.24f, h * 0.18f, w * 0.27f, h * 0.14f)
            cubicTo(w * 0.26f, h * 0.13f, w * 0.25f, h * 0.08f, w * 0.27f, h * 0.02f)
            cubicTo(w * 0.27f, h * 0.02f, w * 0.31f, h * 0.01f, w * 0.40f, h * 0.07f)
            cubicTo(w * 0.44f, h * 0.06f, w * 0.47f, h * 0.05f, w * 0.50f, h * 0.05f)
            cubicTo(w * 0.53f, h * 0.05f, w * 0.57f, h * 0.06f, w * 0.60f, h * 0.07f)
            cubicTo(w * 0.69f, h * 0.01f, w * 0.73f, h * 0.02f, w * 0.73f, h * 0.02f)
            cubicTo(w * 0.75f, h * 0.08f, w * 0.74f, h * 0.13f, w * 0.73f, h * 0.14f)
            cubicTo(w * 0.76f, h * 0.18f, w * 0.78f, h * 0.22f, w * 0.78f, h * 0.27f)
            cubicTo(w * 0.78f, h * 0.45f, w * 0.67f, h * 0.49f, w * 0.57f, h * 0.50f)
            cubicTo(w * 0.58f, h * 0.53f, w * 0.60f, h * 0.57f, w * 0.60f, h * 0.63f)
            cubicTo(w * 0.60f, h * 0.73f, w * 0.60f, h * 0.81f, w * 0.60f, h * 0.84f)
            cubicTo(w * 0.60f, h * 0.86f, w * 0.62f, h * 0.89f, w * 0.64f, h * 0.88f)
            cubicTo(w * 0.82f, h * 0.82f, w * 0.95f, h * 0.65f, w * 0.95f, h * 0.50f)
            cubicTo(w * 0.95f, h * 0.25f, w * 0.75f, h * 0.05f, w * 0.50f, h * 0.05f)
            close()
        }
        drawPath(path, brush = SolidColor(tint), style = Fill)
    }
}

@Composable
fun FacebookIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF1877F2)) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        // Rounded circle or background
        drawCircle(color = tint, radius = w * 0.48f)
        // 'f' symbol in white
        val path = Path().apply {
            moveTo(w * 0.58f, h * 0.85f)
            lineTo(w * 0.48f, h * 0.85f)
            lineTo(w * 0.48f, h * 0.52f)
            lineTo(w * 0.41f, h * 0.52f)
            lineTo(w * 0.41f, h * 0.42f)
            lineTo(w * 0.48f, h * 0.42f)
            lineTo(w * 0.48f, h * 0.35f)
            cubicTo(w * 0.48f, h * 0.27f, w * 0.53f, h * 0.21f, w * 0.63f, h * 0.21f)
            lineTo(w * 0.69f, h * 0.21f)
            lineTo(w * 0.69f, h * 0.30f)
            lineTo(w * 0.63f, h * 0.30f)
            cubicTo(w * 0.59f, h * 0.30f, w * 0.58f, h * 0.32f, w * 0.58f, h * 0.36f)
            lineTo(w * 0.58f, h * 0.42f)
            lineTo(w * 0.68f, h * 0.42f)
            lineTo(w * 0.66f, h * 0.52f)
            lineTo(w * 0.58f, h * 0.52f)
            close()
        }
        drawPath(path, brush = SolidColor(Color.White), style = Fill)
    }
}

@Composable
fun InstagramIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeW = w * 0.09f
        // Instagram camera body
        drawRoundRect(
            color = Color(0xFFE1306C),
            size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.84f),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.24f, h * 0.24f),
            style = Stroke(width = strokeW)
        )
        // Center lens
        drawCircle(
            color = Color(0xFFE1306C),
            radius = w * 0.22f,
            style = Stroke(width = strokeW)
        )
        // Flash dot
        drawCircle(
            color = Color(0xFFE1306C),
            radius = w * 0.05f,
            center = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.30f)
        )
    }
}

@Composable
fun XTwitterIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF0F1419)) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.15f)
            lineTo(w * 0.45f, h * 0.54f)
            lineTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.26f, h * 0.85f)
            lineTo(w * 0.50f, h * 0.59f)
            lineTo(w * 0.72f, h * 0.85f)
            lineTo(w * 0.88f, h * 0.85f)
            lineTo(w * 0.56f, h * 0.44f)
            lineTo(w * 0.84f, h * 0.15f)
            lineTo(w * 0.73f, h * 0.15f)
            lineTo(w * 0.51f, h * 0.39f)
            lineTo(w * 0.31f, h * 0.15f)
            close()
        }
        drawPath(path, brush = SolidColor(tint), style = Fill)
    }
}

@Composable
fun TwitchIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF9146FF)) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.12f, h * 0.10f)
            lineTo(w * 0.20f, h * 0.75f)
            lineTo(w * 0.40f, h * 0.75f)
            lineTo(w * 0.40f, h * 0.90f)
            lineTo(w * 0.55f, h * 0.75f)
            lineTo(w * 0.75f, h * 0.75f)
            lineTo(w * 0.88f, h * 0.60f)
            lineTo(w * 0.88f, h * 0.10f)
            close()
        }
        drawPath(path, brush = SolidColor(tint), style = Fill)
        // Two eyes
        drawRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.44f, h * 0.32f),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.20f)
        )
        drawRect(
            color = Color.White,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.32f),
            size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.20f)
        )
    }
}
