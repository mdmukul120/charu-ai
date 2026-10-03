package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun CharuHeader(
    isAccessibilityOnline: Boolean,
    isVoiceOutputEnabled: Boolean,
    onToggleVoice: () -> Unit,
    isContinuousListening: Boolean = false,
    onToggleContinuous: () -> Unit = {},
    isFloatingBubbleActive: Boolean = false,
    onToggleBubble: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = CyberSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isAccessibilityOnline) CyberEmerald else CyberGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CHARU OS // CORE",
                        color = CyberCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Continuous Listening toggle button
                    IconButton(
                        onClick = onToggleContinuous,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_continuous_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Toggle Always Listening",
                            tint = if (isContinuousListening) CyberGold else CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Floating bubble overlay toggle button
                    IconButton(
                        onClick = onToggleBubble,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_floating_bubble_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Toggle Screen Bubble",
                            tint = if (isFloatingBubbleActive) CyberCyan else CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Voice reply speech toggle
                    IconButton(
                        onClick = onToggleVoice,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("toggle_voice_button")
                    ) {
                        Icon(
                            imageVector = if (isVoiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "Toggle Voice Reply",
                            tint = if (isVoiceOutputEnabled) CyberCyan else CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPill(
                    label = "CONTROLLER",
                    value = if (isAccessibilityOnline) "ONLINE" else "INTENT",
                    color = if (isAccessibilityOnline) CyberEmerald else CyberGold
                )
                StatusPill(
                    label = "ALWAYS-MIC",
                    value = if (isContinuousListening) "ACTIVE" else "OFF",
                    color = if (isContinuousListening) CyberGold else CyberTextSecondary
                )
                StatusPill(
                    label = "BUBBLE",
                    value = if (isFloatingBubbleActive) "ON SCREEN" else "OFF",
                    color = if (isFloatingBubbleActive) CyberCyan else CyberTextSecondary
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyberDark)
            .border(width = 0.8.dp, color = color.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label: ",
                color = CyberTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CharuArcOrb(
    isListening: Boolean,
    isProcessing: Boolean,
    audioRms: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 2500 else if (isProcessing) 1500 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 3500 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rev_rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 400 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val effectiveRms = if (isListening) (0.2f + audioRms * 0.8f) else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(160.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("arc_orb_trigger")
    ) {
        // Outer Canvas for holographic rings
        Canvas(modifier = Modifier.size(150.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 2

            // Background subtle glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CyberCyan.copy(alpha = if (isListening) 0.35f else 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                )
            )

            // Outer dashed ring
            drawCircle(
                color = CyberCyan.copy(alpha = 0.35f),
                radius = baseRadius - 6.dp.toPx(),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), rotation)
                )
            )

            // Inner reverse dashed ring
            drawCircle(
                color = if (isListening) CyberGold.copy(alpha = 0.7f) else CyberPurple.copy(alpha = 0.4f),
                radius = baseRadius - 16.dp.toPx(),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f), reverseRotation)
                )
            )

            // Dynamic Audio Wave ring
            if (isListening && effectiveRms > 0.05f) {
                drawCircle(
                    color = CyberCyan.copy(alpha = 0.6f * effectiveRms),
                    radius = (baseRadius - 26.dp.toPx()) * (1f + effectiveRms * 0.25f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Inner Reactor Core Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isListening) CyberGold.copy(alpha = 0.4f) else CyberCyan.copy(alpha = 0.35f),
                            CyberDark
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = if (isListening) CyberGold else if (isProcessing) CyberPurple else CyberCyan,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = when {
                    isListening -> Icons.Default.GraphicEq
                    isProcessing -> Icons.Default.GraphicEq
                    else -> Icons.Default.Mic
                },
                contentDescription = "Voice Command Trigger",
                tint = if (isListening) CyberGold else if (isProcessing) CyberPurple else CyberCyan,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
