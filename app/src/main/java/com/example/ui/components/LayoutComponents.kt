package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Bespoke "Professional Polish" Material Design 3 Lavender colors
val CarbonBackground = Color(0xFFF7F2FA)     // Light Lavender workspace background
val CharcoalSurface = Color(0xFFFEF7FF)      // Pristine card surface background
val LightSurface = Color(0xFFECE6F0)         // Darker tinted background for input & containers
val PrimaryTeal = Color(0xFF6750A4)          // Professional MD3 Purple Accent
val ActiveBlue = Color(0xFF21005D)           // High Contrast Deep Indigo
val HighFrictionOrange = Color(0xFFB3261E)   // Standard MD3 Error Red
val SoftCyan = Color(0xFFEADDFF)             // Soft Lavender highlighted selection indicator
val TextLight = Color(0xFF1D1B20)            // High contrast text/numbers
val TextMuted = Color(0xFF49454F)            // Muted secondary gray-purple

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp), // MD3 extra large responsive rounded corners
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)), // Standard MD3 outline border
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (borderBrush != null) {
                        Modifier.drawBehind {
                            // Subtle luxury indicator on high friction cards
                            drawRect(
                                brush = borderBrush,
                                size = size.copy(height = 6.dp.toPx())
                            )
                        }
                    } else Modifier
                )
                .padding(18.dp),
            content = content
        )
    }
}

@Composable
fun CustomAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initial = if (name.isNotEmpty()) name.first().uppercase() else "B"
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(SoftCyan),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = ActiveBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedProcessingLoader(
    modifier: Modifier = Modifier,
    tipText: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val angle by infiniteTransition.animateFloat(
        initialValue =  0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .drawBehind {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Color.Transparent, PrimaryTeal, SoftCyan)),
                        startAngle = angle,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                    )
                }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Analyzing with Coach Intelligence...",
            color = PrimaryTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tipText,
            color = TextMuted,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
