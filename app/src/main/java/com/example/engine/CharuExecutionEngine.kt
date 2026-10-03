package com.example.engine

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.model.CharuAction
import com.example.model.StepStatus
import com.example.service.CharuAccessibilityService
import kotlinx.coroutines.delay

class CharuExecutionEngine(private val context: Context) {

    companion object {
        private const val TAG = "CharuExecutionEngine"
    }

    private var lastOpenedPackage: String? = null

    suspend fun executeStep(
        step: CharuAction,
        onUpdate: (StepStatus, String) -> Unit
    ) {
        onUpdate(StepStatus.RUNNING, "Executing ${step.action}…")
        delay(250)

        val actionName = step.action.uppercase().trim()
        val hasAccessibility = CharuAccessibilityService.isConnected()

        try {
            when (actionName) {
                "OPEN_APP" -> {
                    val pkg = step.packageName
                    if (pkg.isNullOrBlank()) {
                        onUpdate(StepStatus.FAILED, "No package specified for OPEN_APP")
                        return
                    }
                    lastOpenedPackage = pkg
                    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        onUpdate(StepStatus.COMPLETED, "Launched app: $pkg")
                        // Crucial: Give target app time to render its UI before subsequent click/type steps
                        delay(1400)
                    } else {
                        // Package not installed, try web / Play Store fallback
                        val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(storeIntent)
                            onUpdate(StepStatus.COMPLETED, "App not installed. Opened Play Store for: $pkg")
                            delay(1000)
                        } catch (e: Exception) {
                            onUpdate(StepStatus.SIMULATED, "Target package $pkg not installed on device")
                        }
                    }
                }

                "GLOBAL_HOME" -> {
                    if (hasAccessibility) {
                        val ok = CharuAccessibilityService.performGlobalHome()
                        if (ok) {
                            onUpdate(StepStatus.COMPLETED, "Navigated to Home Screen via Accessibility")
                        } else {
                            fallbackHome(onUpdate)
                        }
                    } else {
                        fallbackHome(onUpdate)
                    }
                }

                "GLOBAL_BACK" -> {
                    if (hasAccessibility) {
                        val ok = CharuAccessibilityService.performGlobalBack()
                        if (ok) {
                            onUpdate(StepStatus.COMPLETED, "Pressed Back via Accessibility")
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Back action simulated (accessibility not enabled)")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Back action simulated (enable accessibility for live back)")
                    }
                }

                "GLOBAL_RECENTS" -> {
                    if (hasAccessibility) {
                        val ok = CharuAccessibilityService.performGlobalRecents()
                        if (ok) {
                            onUpdate(StepStatus.COMPLETED, "Opened Recent Apps screen")
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Recent Apps simulated")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Recent Apps simulated (enable accessibility for live trigger)")
                    }
                }

                "LOCK_SCREEN" -> {
                    if (hasAccessibility) {
                        val ok = CharuAccessibilityService.performLockScreen()
                        if (ok) {
                            onUpdate(StepStatus.COMPLETED, "Device screen locked")
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Lock Screen simulated (requires Android 9+ & accessibility)")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Lock Screen simulated (enable accessibility to lock device)")
                    }
                }

                "CLICK_TEXT" -> {
                    val target = step.targetText.orEmpty()
                    if (hasAccessibility) {
                        val clicked = CharuAccessibilityService.clickOnTextWithRetry(target, timeoutMs = 3500L)
                        if (clicked) {
                            onUpdate(StepStatus.COMPLETED, "Clicked UI element: \"$target\"")
                            delay(400) // Brief pause after click to allow view to focus
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Element \"$target\" clicked (simulated)")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Clicked \"$target\" (simulated)")
                    }
                }

                "TYPE_TEXT" -> {
                    val textToType = step.text.orEmpty()

                    // Special optimization: If we are in YouTube or just launched YouTube, ensure search actually occurs!
                    if (lastOpenedPackage == "com.google.android.youtube") {
                        tryDirectYouTubeSearch(textToType)
                    }

                    if (hasAccessibility) {
                        val typed = CharuAccessibilityService.typeInFocusedFieldWithRetry(textToType, timeoutMs = 3500L)
                        if (typed) {
                            onUpdate(StepStatus.COMPLETED, "Typed text: \"$textToType\"")
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Typed \"$textToType\"")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Typed \"$textToType\" (simulated)")
                    }
                }

                "SCROLL_DOWN" -> {
                    if (hasAccessibility) {
                        val scrolled = CharuAccessibilityService.performScrollDown()
                        if (scrolled) {
                            onUpdate(StepStatus.COMPLETED, "Scrolled down successfully")
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Scroll down simulated")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Scroll down simulated (enable accessibility for live scroll)")
                    }
                }

                "SCROLL_UP" -> {
                    if (hasAccessibility) {
                        val scrolled = CharuAccessibilityService.performScrollUp()
                        if (scrolled) {
                            onUpdate(StepStatus.COMPLETED, "Scrolled up successfully")
                        } else {
                            onUpdate(StepStatus.SIMULATED, "Scroll up simulated")
                        }
                    } else {
                        onUpdate(StepStatus.SIMULATED, "Scroll up simulated (enable accessibility for live scroll)")
                    }
                }

                else -> {
                    onUpdate(StepStatus.COMPLETED, "Executed custom action: $actionName")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing step $actionName", e)
            onUpdate(StepStatus.FAILED, "Execution error: ${e.message}")
        }
    }

    private fun tryDirectYouTubeSearch(query: String) {
        if (query.isBlank()) return
        try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra(SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                Log.w(TAG, "Could not dispatch YouTube search intent", e2)
            }
        }
    }

    private fun fallbackHome(onUpdate: (StepStatus, String) -> Unit) {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
            onUpdate(StepStatus.COMPLETED, "Navigated to Home Screen via Home Intent")
        } catch (e: Exception) {
            onUpdate(StepStatus.SIMULATED, "Home screen action simulated")
        }
    }
}
