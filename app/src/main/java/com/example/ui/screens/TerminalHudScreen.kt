package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CharuCommandResponse
import com.example.model.ExecutionStep
import com.example.model.StepStatus
import com.example.ui.components.CharuArcOrb
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerminalHudScreen(
    currentInput: String,
    onInputChange: (String) -> Unit,
    onSubmitCommand: (String) -> Unit,
    isListening: Boolean,
    isProcessing: Boolean,
    audioRms: Float,
    onOrbClick: () -> Unit,
    isContinuousListening: Boolean = false,
    onToggleContinuous: () -> Unit = {},
    isFloatingBubbleActive: Boolean = false,
    onToggleBubble: () -> Unit = {},
    statusText: String,
    latestResponse: CharuCommandResponse?,
    latestRawJson: String,
    executionSteps: List<ExecutionStep>,
    onCopyJson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val suggestions = listOf(
        "ইউটিউব খোলো",
        "হোম স্ক্রিনে যাও",
        "মেসেঞ্জারে গিয়ে রনিকে লেখো কেমন আছো",
        "Open WhatsApp",
        "Open Chrome",
        "Scroll Down",
        "Lock Screen"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Interactive Arc Reactor Core
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CharuArcOrb(
                        isListening = isListening,
                        isProcessing = isProcessing,
                        audioRms = audioRms,
                        onClick = onOrbClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isListening) "LISTENING FOR VOICE DIRECTIVE…"
                        else if (isProcessing) "PROCESSING DIRECTIVE THROUGH NEURAL CORE…"
                        else if (isContinuousListening) "ALWAYS-LISTENING ACTIVE // SPEAK FREELY"
                        else "TAP ORB TO SPEAK // CHARU OS READY",
                        color = if (isListening) CyberGold else if (isProcessing) CyberPurple else if (isContinuousListening) CyberGold else CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = statusText,
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Dedicated OS Controls: Continuous Listening & Floating Screen Bubble
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "CHARU OS BACKGROUND & VOICE MODES",
                        color = CyberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Continuous Listening Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Always Listening (সর্বক্ষণ শুনুন)",
                                color = CyberTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Charu continuously listens until you manually turn it off",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = isContinuousListening,
                            onCheckedChange = { onToggleContinuous() },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = CyberGold,
                                checkedTrackColor = CyberGold.copy(alpha = 0.4f),
                                uncheckedThumbColor = CyberTextSecondary,
                                uncheckedTrackColor = CyberDark
                            ),
                            modifier = Modifier.testTag("continuous_listening_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.8.dp)
                            .background(CyberSurfaceBorder)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Floating Screen Bubble Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Screen Floating Bubble (স্ক্রিন বাবল)",
                                color = CyberTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Floats over YouTube, Messenger, WhatsApp when backgrounded",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = isFloatingBubbleActive,
                            onCheckedChange = { onToggleBubble() },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                                uncheckedThumbColor = CyberTextSecondary,
                                uncheckedTrackColor = CyberDark
                            ),
                            modifier = Modifier.testTag("floating_bubble_switch")
                        )
                    }
                }
            }
        }

        // Voice / Text Command Input Bar
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "VOICE OR TEXT DIRECTIVE",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentInput,
                            onValueChange = onInputChange,
                            placeholder = {
                                Text(
                                    text = "Say or type: e.g. ইউটিউব খোলো, Open WhatsApp…",
                                    color = CyberTextSecondary.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (currentInput.isNotBlank()) onSubmitCommand(currentInput)
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedContainerColor = CyberDark,
                                unfocusedContainerColor = CyberDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("command_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (currentInput.isNotBlank()) onSubmitCommand(currentInput)
                            },
                            enabled = currentInput.isNotBlank() && !isProcessing,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentInput.isNotBlank()) CyberCyan else CyberSurfaceBorder)
                                .testTag("send_command_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Submit Directive",
                                tint = if (currentInput.isNotBlank()) CyberDark else CyberTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "QUICK DIRECTIVES:",
                        color = CyberTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        suggestions.forEach { chipText ->
                            Surface(
                                color = CyberDark,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(0.8.dp, CyberCyan.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .testTag("suggestion_chip_$chipText")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .clickable {
                                            onSubmitCommand(chipText)
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = chipText,
                                        color = CyberTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Execution Pipeline
        if (latestResponse != null || executionSteps.isNotEmpty()) {
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CHARU EXECUTION PIPELINE",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = CyberCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        latestResponse?.let { resp ->
                            Spacer(modifier = Modifier.height(10.dp))
                            // Assistant Jarvis Status Reply
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberDark)
                                    .border(width = 1.dp, color = CyberGold.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "ASSISTANT STATUS REPLY",
                                        color = CyberGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${resp.assistantReply}\"",
                                        color = CyberTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Steps list
                        if (executionSteps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "SEQUENTIAL INSTRUCTION STEPS (${executionSteps.size})",
                                color = CyberTextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            executionSteps.forEach { step ->
                                StepItemCard(step = step)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // Raw JSON Schema Output Box
        if (latestRawJson.isNotBlank()) {
            item {
                Surface(
                    color = CyberDark,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CHARU JSON EXECUTION SCHEMA",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onCopyJson(latestRawJson) },
                                    modifier = Modifier.size(32.dp).testTag("copy_json_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy JSON",
                                        tint = CyberTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        latestResponse?.let { resp ->
                                            val firstAction = resp.getExecutionSteps().firstOrNull()
                                            if (firstAction != null) {
                                                // Re-trigger execution
                                                onSubmitCommand(resp.assistantReply)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).testTag("re_run_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Re-Run",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSurfaceCard)
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = latestRawJson,
                                    color = CyberEmerald,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StepItemCard(step: ExecutionStep) {
    val statusColor = when (step.status) {
        StepStatus.PENDING -> CyberTextSecondary
        StepStatus.RUNNING -> CyberGold
        StepStatus.COMPLETED -> CyberEmerald
        StepStatus.SIMULATED -> CyberCyan
        StepStatus.FAILED -> CyberRed
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CyberSurfaceCard)
            .border(width = 0.8.dp, color = statusColor.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step index badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f))
                    .border(width = 1.dp, color = statusColor, shape = CircleShape)
            ) {
                Text(
                    text = "${step.index}",
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = step.action.action,
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (step.status) {
                            StepStatus.PENDING -> "PENDING"
                            StepStatus.RUNNING -> "RUNNING…"
                            StepStatus.COMPLETED -> "SUCCESS"
                            StepStatus.SIMULATED -> "SIMULATED"
                            StepStatus.FAILED -> "FAILED"
                        },
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = step.action.summary(),
                    color = CyberTextPrimary,
                    fontSize = 11.sp
                )

                if (step.message.isNotBlank()) {
                    Text(
                        text = "• ${step.message}",
                        color = statusColor.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            when (step.status) {
                StepStatus.RUNNING -> {
                    CircularProgressIndicator(
                        color = CyberGold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                }
                StepStatus.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                StepStatus.SIMULATED -> {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Simulated",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                StepStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = CyberRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                StepStatus.PENDING -> {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Pending",
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
