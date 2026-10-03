package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharuAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "CharuAccessService"

        private var instance: CharuAccessibilityService? = null

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        fun isConnected(): Boolean = _isServiceConnected.value

        fun getInstance(): CharuAccessibilityService? = instance

        fun performGlobalHome(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun performGlobalBack(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun performGlobalRecents(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false
        }

        fun performLockScreen(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) ?: false
            } else {
                false
            }
        }

        suspend fun clickOnTextWithRetry(targetText: String, timeoutMs: Long = 3500L): Boolean {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                val service = instance ?: return false
                val root = service.rootInActiveWindow
                if (root != null) {
                    val clicked = service.findAndClickText(root, targetText)
                    if (clicked) return true
                }
                delay(300)
            }
            return false
        }

        suspend fun typeInFocusedFieldWithRetry(text: String, timeoutMs: Long = 3500L): Boolean {
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                val service = instance ?: return false
                val root = service.rootInActiveWindow
                if (root != null) {
                    val typed = service.findAndTypeText(root, text)
                    if (typed) return true
                }
                delay(300)
            }
            return false
        }

        fun clickOnText(targetText: String): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            return service.findAndClickText(root, targetText)
        }

        fun typeInFocusedField(text: String): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            return service.findAndTypeText(root, text)
        }

        fun performScrollDown(): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            return service.scrollNode(root, forward = true)
        }

        fun performScrollUp(): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            return service.scrollNode(root, forward = false)
        }

        fun tapAt(x: Float, y: Float): Boolean {
            val service = instance ?: return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val path = Path().apply { moveTo(x, y) }
                val stroke = GestureDescription.StrokeDescription(path, 0, 100)
                val gesture = GestureDescription.Builder().addStroke(stroke).build()
                return service.dispatchGesture(gesture, null, null)
            }
            return false
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.i(TAG, "Charu Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Continuous accessibility event processing if needed
    }

    override fun onInterrupt() {
        Log.w(TAG, "Charu Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
        Log.i(TAG, "Charu Accessibility Service Destroyed")
    }

    private fun findAndClickText(rootNode: AccessibilityNodeInfo, targetText: String): Boolean {
        val cleanTarget = targetText.trim()
        if (cleanTarget.isBlank()) return false

        // 1. Recursive search matching text, contentDescription, or viewId
        val matchedNode = searchNodeDeep(rootNode, cleanTarget)
        if (matchedNode != null) {
            var current: AccessibilityNodeInfo? = matchedNode
            while (current != null) {
                if (current.isClickable) {
                    if (current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                }
                current = current.parent
            }
            // If parent not clickable, click matched node directly
            if (matchedNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
        }

        // 2. Standard fallback search
        val nodes = rootNode.findAccessibilityNodeInfosByText(cleanTarget)
        if (!nodes.isNullOrEmpty()) {
            for (node in nodes) {
                var current: AccessibilityNodeInfo? = node
                while (current != null) {
                    if (current.isClickable && current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                    current = current.parent
                }
                if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
            }
        }
        return false
    }

    private fun searchNodeDeep(node: AccessibilityNodeInfo, target: String): AccessibilityNodeInfo? {
        val targetLower = target.lowercase()
        val textLower = node.text?.toString()?.lowercase()
        val descLower = node.contentDescription?.toString()?.lowercase()
        val viewIdLower = node.viewIdResourceName?.lowercase()

        val isSearchIntent = targetLower == "search" || targetLower.contains("সার্চ") || targetLower.contains("অনুসন্ধান")
        val isSendIntent = targetLower == "send" || targetLower.contains("পাঠাও") || targetLower.contains("সেন্ড")

        // Content description match (e.g. "Search YouTube", "Search Messenger", "Search contacts")
        if (descLower != null && (descLower.contains(targetLower) || targetLower.contains(descLower))) {
            return node
        }

        // Text match
        if (textLower != null && (textLower.contains(targetLower) || targetLower.contains(textLower))) {
            return node
        }

        // Search button ID matches
        if (isSearchIntent && viewIdLower != null) {
            if (viewIdLower.contains("search") || viewIdLower.contains("menu_item_0") || viewIdLower.contains("btn_search")) {
                return node
            }
        }

        // Send button ID matches
        if (isSendIntent && viewIdLower != null) {
            if (viewIdLower.contains("send") || viewIdLower.contains("submit") || viewIdLower.contains("compose")) {
                return node
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = searchNodeDeep(child, target)
            if (found != null) return found
        }
        return null
    }

    private fun findAndTypeText(rootNode: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }

        // 1. Try focused input node
        val focused = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && (focused.isEditable || focused.isFocusable)) {
            focused.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            if (focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                return true
            }
        }

        // 2. Search for any editable field
        val editable = findFirstEditableNode(rootNode)
        if (editable != null) {
            editable.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            editable.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
                return true
            }
        }

        return false
    }

    private fun findFirstEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val className = node.className?.toString()?.lowercase() ?: ""
        if (node.isEditable || className.contains("edittext") || className.contains("searchview")) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFirstEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun scrollNode(node: AccessibilityNodeInfo, forward: Boolean): Boolean {
        if (node.isScrollable) {
            val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            if (node.performAction(action)) {
                return true
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (scrollNode(child, forward)) {
                return true
            }
        }
        return false
    }
}
