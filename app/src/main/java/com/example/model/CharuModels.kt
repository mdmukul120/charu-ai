package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CharuAction(
    @param:Json(name = "action") val action: String = "",
    @param:Json(name = "package_name") val packageName: String? = null,
    @param:Json(name = "target_text") val targetText: String? = null,
    @param:Json(name = "text") val text: String? = null,
    @param:Json(name = "x") val x: Float? = null,
    @param:Json(name = "y") val y: Float? = null
) {
    fun summary(): String {
        return when (action.uppercase()) {
            "OPEN_APP" -> "Open App: ${packageName ?: "unknown"}"
            "CLICK_TEXT" -> "Click Text: \"${targetText ?: ""}\""
            "TYPE_TEXT" -> "Type: \"${text ?: ""}\""
            "SCROLL_DOWN" -> "Scroll Down Screen"
            "SCROLL_UP" -> "Scroll Up Screen"
            "GLOBAL_HOME" -> "Go to Home Screen"
            "GLOBAL_BACK" -> "Press Back"
            "GLOBAL_RECENTS" -> "Open Recent Apps"
            "LOCK_SCREEN" -> "Lock Device Screen"
            else -> "$action ${packageName ?: targetText ?: text ?: ""}".trim()
        }
    }
}

@JsonClass(generateAdapter = true)
data class CharuCommandResponse(
    @param:Json(name = "assistant_reply") val assistantReply: String = "Directive processed, sir.",
    @param:Json(name = "action") val action: String? = null,
    @param:Json(name = "package_name") val packageName: String? = null,
    @param:Json(name = "target_text") val targetText: String? = null,
    @param:Json(name = "text") val text: String? = null,
    @param:Json(name = "x") val x: Float? = null,
    @param:Json(name = "y") val y: Float? = null,
    @param:Json(name = "sequence") val sequence: List<CharuAction>? = null
) {
    fun getExecutionSteps(): List<CharuAction> {
        if (!sequence.isNullOrEmpty()) {
            return sequence
        }
        if (!action.isNullOrBlank()) {
            return listOf(
                CharuAction(
                    action = action,
                    packageName = packageName,
                    targetText = targetText,
                    text = text,
                    x = x,
                    y = y
                )
            )
        }
        return emptyList()
    }
}

enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    SIMULATED
}

data class ExecutionStep(
    val index: Int,
    val action: CharuAction,
    val status: StepStatus = StepStatus.PENDING,
    val message: String = ""
)

data class CommandHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val command: String,
    val assistantReply: String,
    val rawJson: String,
    val stepCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean = false
)
