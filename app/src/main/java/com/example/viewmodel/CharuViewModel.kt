package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.CharuAIInterpreter
import com.example.engine.CharuExecutionEngine
import com.example.model.CharuAction
import com.example.model.CharuCommandResponse
import com.example.model.CommandHistoryItem
import com.example.model.ExecutionStep
import com.example.model.InstalledAppInfo
import com.example.model.StepStatus
import com.example.service.CharuAccessibilityService
import com.example.voice.CharuVoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharuViewModel(application: Application) : AndroidViewModel(application) {

    private val executionEngine = CharuExecutionEngine(application.applicationContext)
    val voiceManager = CharuVoiceManager(application.applicationContext)

    val isServiceConnected: StateFlow<Boolean> = CharuAccessibilityService.isServiceConnected
    val isListening: StateFlow<Boolean> = voiceManager.isListening
    val audioRms: StateFlow<Float> = voiceManager.audioRms
    val isContinuousListening: StateFlow<Boolean> = voiceManager.isContinuousMode
    val isFloatingBubbleActive: StateFlow<Boolean> = com.example.service.CharuFloatingBubbleService.isBubbleActive

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _latestResponse = MutableStateFlow<CharuCommandResponse?>(null)
    val latestResponse: StateFlow<CharuCommandResponse?> = _latestResponse.asStateFlow()

    private val _latestRawJson = MutableStateFlow("")
    val latestRawJson: StateFlow<String> = _latestRawJson.asStateFlow()

    private val _executionSteps = MutableStateFlow<List<ExecutionStep>>(emptyList())
    val executionSteps: StateFlow<List<ExecutionStep>> = _executionSteps.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<CommandHistoryItem>>(emptyList())
    val commandHistory: StateFlow<List<CommandHistoryItem>> = _commandHistory.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _customJsonInput = MutableStateFlow(
        """{
  "assistant_reply": "Opening WhatsApp, sir.",
  "action": "OPEN_APP",
  "package_name": "com.whatsapp"
}"""
    )
    val customJsonInput: StateFlow<String> = _customJsonInput.asStateFlow()

    private val _systemStatusText = MutableStateFlow("CHARU OS READY // STANDING BY")
    val systemStatusText: StateFlow<String> = _systemStatusText.asStateFlow()

    init {
        loadInstalledApps()
        seedInitialHistory()
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun updateCurrentInput(text: String) {
        _currentInput.value = text
    }

    fun updateCustomJsonInput(text: String) {
        _customJsonInput.value = text
    }

    fun toggleVoiceOutput() {
        voiceManager.isVoiceOutputEnabled = !voiceManager.isVoiceOutputEnabled
    }

    fun isVoiceOutputEnabled(): Boolean = voiceManager.isVoiceOutputEnabled

    fun loadInstalledApps() {
        viewModelScope.launch {
            val pm = getApplication<Application>().packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val list = resolveInfos.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(pm).toString()
                val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledAppInfo(appName = label, packageName = pkgName, isSystemApp = isSystem)
            }.distinctBy { it.packageName }.sortedBy { it.appName }

            _installedApps.value = list
        }
    }

    fun submitCommand(inputCommand: String = _currentInput.value) {
        val cleanInput = inputCommand.trim()
        if (cleanInput.isBlank()) return

        _currentInput.value = ""
        _isProcessing.value = true
        _systemStatusText.value = "INTERPRETING DIRECTIVE: \"$cleanInput\"…"

        viewModelScope.launch {
            try {
                val appMap = _installedApps.value.associate { it.appName.lowercase() to it.packageName }
                val (response, rawJson) = CharuAIInterpreter.interpretCommand(cleanInput, appMap)

                _latestResponse.value = response
                _latestRawJson.value = rawJson
                _systemStatusText.value = response.assistantReply

                // Voice feedback
                voiceManager.speak(response.assistantReply)

                // Build execution steps
                val actions = response.getExecutionSteps()
                val steps = actions.mapIndexed { idx, act ->
                    ExecutionStep(index = idx + 1, action = act, status = StepStatus.PENDING)
                }
                _executionSteps.value = steps

                // Run sequential execution
                var anyFailed = false
                val updatedSteps = steps.toMutableList()

                for (i in updatedSteps.indices) {
                    val currentStep = updatedSteps[i]
                    executionEngine.executeStep(currentStep.action) { status, logMsg ->
                        updatedSteps[i] = currentStep.copy(status = status, message = logMsg)
                        _executionSteps.value = updatedSteps.toList()
                        if (status == StepStatus.FAILED) anyFailed = true
                    }
                }

                // Add to history
                val historyItem = CommandHistoryItem(
                    command = cleanInput,
                    assistantReply = response.assistantReply,
                    rawJson = rawJson,
                    stepCount = actions.size,
                    isSuccess = !anyFailed
                )
                _commandHistory.value = listOf(historyItem) + _commandHistory.value
                _systemStatusText.value = "EXECUTION COMPLETE // READY FOR NEXT DIRECTIVE"
            } catch (e: Exception) {
                _systemStatusText.value = "EXECUTION FAILED: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun executeDirectAction(action: CharuAction) {
        val response = CharuCommandResponse(
            assistantReply = "Executing ${action.summary()}.",
            action = action.action,
            packageName = action.packageName,
            targetText = action.targetText,
            text = action.text,
            x = action.x,
            y = action.y
        )
        _latestResponse.value = response
        _latestRawJson.value = """{
  "assistant_reply": "${response.assistantReply}",
  "action": "${action.action}"${if (action.packageName != null) ",\n  \"package_name\": \"${action.packageName}\"" else ""}${if (action.targetText != null) ",\n  \"target_text\": \"${action.targetText}\"" else ""}${if (action.text != null) ",\n  \"text\": \"${action.text}\"" else ""}
}"""

        val step = ExecutionStep(1, action, StepStatus.PENDING)
        _executionSteps.value = listOf(step)
        _isProcessing.value = true
        _systemStatusText.value = response.assistantReply
        voiceManager.speak(response.assistantReply)

        viewModelScope.launch {
            executionEngine.executeStep(action) { status, msg ->
                _executionSteps.value = listOf(step.copy(status = status, message = msg))
            }
            _isProcessing.value = false
        }
    }

    fun executeRawJsonDirectly(json: String = _customJsonInput.value) {
        val parsed = CharuAIInterpreter.parseRawJson(json)
        if (parsed == null) {
            Toast.makeText(getApplication(), "Invalid Charu JSON Schema", Toast.LENGTH_SHORT).show()
            return
        }

        _latestResponse.value = parsed
        _latestRawJson.value = json
        _isProcessing.value = true
        _systemStatusText.value = parsed.assistantReply
        voiceManager.speak(parsed.assistantReply)

        val actions = parsed.getExecutionSteps()
        val steps = actions.mapIndexed { idx, act ->
            ExecutionStep(index = idx + 1, action = act, status = StepStatus.PENDING)
        }
        _executionSteps.value = steps

        viewModelScope.launch {
            val updated = steps.toMutableList()
            for (i in updated.indices) {
                val current = updated[i]
                executionEngine.executeStep(current.action) { status, msg ->
                    updated[i] = current.copy(status = status, message = msg)
                    _executionSteps.value = updated.toList()
                }
            }
            _isProcessing.value = false
            _systemStatusText.value = "CUSTOM SEQUENCE EXECUTED"
        }
    }

    fun startVoiceInput(onPermissionNeeded: () -> Unit) {
        if (voiceManager.isListening.value) {
            voiceManager.stopListening()
            return
        }

        voiceManager.startListening(
            onResult = { recognizedText ->
                submitCommand(recognizedText)
            },
            onError = { errMsg ->
                _systemStatusText.value = "VOICE INPUT: $errMsg"
                if (errMsg.contains("permission", ignoreCase = true)) {
                    onPermissionNeeded()
                }
            }
        )
    }

    fun toggleContinuousListening(onPermissionNeeded: () -> Unit) {
        val willEnable = !voiceManager.isContinuousMode.value
        voiceManager.setContinuousListening(willEnable)
        if (willEnable) {
            voiceManager.startListening(
                onResult = { recognizedText -> submitCommand(recognizedText) },
                onError = { errMsg ->
                    _systemStatusText.value = "VOICE INPUT: $errMsg"
                    if (errMsg.contains("permission", ignoreCase = true)) {
                        onPermissionNeeded()
                    }
                }
            )
            _systemStatusText.value = "CONTINUOUS LISTENING ACTIVE // ALWAYS LISTENING"
        } else {
            _systemStatusText.value = "CONTINUOUS LISTENING DISABLED"
        }
    }

    fun toggleFloatingBubble(context: Context, onPermissionNeeded: () -> Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(context)) {
                onPermissionNeeded()
                return
            }
        }
        if (com.example.service.CharuFloatingBubbleService.isBubbleActive.value) {
            com.example.service.CharuFloatingBubbleService.stop(context)
            Toast.makeText(context, "Floating Bubble Stopped", Toast.LENGTH_SHORT).show()
        } else {
            com.example.service.CharuFloatingBubbleService.start(context)
            Toast.makeText(context, "Floating Bubble Active on Screen", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyJsonToClipboard(context: Context, json: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Charu JSON Schema", json)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied Charu Execution Schema", Toast.LENGTH_SHORT).show()
    }

    fun clearHistory() {
        _commandHistory.value = emptyList()
    }

    private fun seedInitialHistory() {
        _commandHistory.value = listOf(
            CommandHistoryItem(
                command = "ইউটিউব খোলো",
                assistantReply = "Opening YouTube, sir.",
                rawJson = """{
  "assistant_reply": "Opening YouTube, sir.",
  "action": "OPEN_APP",
  "package_name": "com.google.android.youtube"
}""",
                stepCount = 1,
                timestamp = System.currentTimeMillis() - 3600000,
                isSuccess = true
            ),
            CommandHistoryItem(
                command = "মেসেঞ্জারে গিয়ে রনিকে লেখো কেমন আছো",
                assistantReply = "Sending message to Roni, sir.",
                rawJson = """{
  "assistant_reply": "Sending message to Roni, sir.",
  "sequence": [
    { "action": "OPEN_APP", "package_name": "com.facebook.orca" },
    { "action": "CLICK_TEXT", "target_text": "Search" },
    { "action": "TYPE_TEXT", "text": "রনি" },
    { "action": "CLICK_TEXT", "target_text": "রনি" },
    { "action": "TYPE_TEXT", "text": "কেমন আছো" },
    { "action": "CLICK_TEXT", "target_text": "Send" }
  ]
}""",
                stepCount = 6,
                timestamp = System.currentTimeMillis() - 7200000,
                isSuccess = true
            ),
            CommandHistoryItem(
                command = "হোম স্ক্রিনে যাও",
                assistantReply = "Going to home screen.",
                rawJson = """{
  "assistant_reply": "Going to home screen.",
  "action": "GLOBAL_HOME"
}""",
                stepCount = 1,
                timestamp = System.currentTimeMillis() - 10800000,
                isSuccess = true
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
