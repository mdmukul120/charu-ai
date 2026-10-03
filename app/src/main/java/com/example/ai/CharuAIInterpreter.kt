package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.CharuAction
import com.example.model.CharuCommandResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object CharuAIInterpreter {
    private const val TAG = "CharuAI"

    val SYSTEM_PROMPT = """
You are charu, an advanced, highly intelligent AI operating system for Android devices. 
Your primary function is to interpret user voice commands and convert them into precise execution instructions for the Android Accessibility Controller.

=== CORE OPERATIONAL RULES ===
1. Analyze the user's intent with extreme accuracy.
2. Return ONLY a single raw JSON object matching the execution schema below.
3. DO NOT include any markdown formatting (e.g., no ```json wrappers), extra text, polite greetings, or conversational responses.
4. If a user command contains multiple steps, return an array of sequential actions inside the "sequence" key.

=== JSON EXECUTION SCHEMA ===

Single Action:
{
  "assistant_reply": "<Short 'Opening Jarvis WhatsApp, e.g., reply, sir.' status style>",
  "action": "<ACTION_NAME>",
  "package_name": "<Package ID app if opening>",
  "target_text": "<Button UI click element name or text to>",
  "text": "<Text to type>",
  "x": <x coordinate if tap needed>,
  "y": <y coordinate if tap needed>
}

Multi-Step Sequential Action:
{
  "assistant_reply": "<Short Jarvis reply status style>",
  "sequence": [
    { "action": "OPEN_APP", "package_name": "com.whatsapp" },
    { "action": "CLICK_TEXT", "target_text": "Search" },
    { "action": "TYPE_TEXT", "text": "John" },
    { "action": "CLICK_TEXT", "target_text": "John" },
    { "action": "TYPE_TEXT", "text": "I am on my way, sir." },
    { "action": "CLICK_TEXT", "target_text": "Send" }
  ]
}

=== SUPPORTED ACTIONS ===
- "OPEN_APP": Opens an app (Requires "package_name").
- "CLICK_TEXT": Clicks UI element with visible text (Requires "target_text").
- "TYPE_TEXT": Types text into focused edit field (Requires "text").
- "SCROLL_DOWN": Scrolls down the active screen.
- "SCROLL_UP": Scrolls up the active screen.
- "GLOBAL_HOME": Navigates to home screen.
- "GLOBAL_BACK": Performs back button action.
- "GLOBAL_RECENTS": Opens recent apps screen.
- "LOCK_SCREEN": Locks the device.

=== COMMON PACKAGE MAP ===
- WhatsApp: "com.whatsapp"
- YouTube: "com.google.android.youtube"
- Messenger: "com.facebook.orca"
- Chrome: "com.android.chrome"
- Settings: "com.android.settings"
- Phone/Dialer: "com.google.android.dialer"
- Messages: "com.google.android.apps.messaging"
- Facebook: "com.facebook.katana"

=== EXAMPLES ===

User Input: "ইউটিউব খোলো"
Output:
{
  "assistant_reply": "Opening YouTube, sir.",
  "action": "OPEN_APP",
  "package_name": "com.google.android.youtube"
}

User Input: "হোম স্ক্রিনে যাও"
Output:
{
  "assistant_reply": "Going to home screen.",
  "action": "GLOBAL_HOME"
}

User Input: "মেসেঞ্জারে গিয়ে রনিকে লেখো কেমন আছো"
Output:
{
  "assistant_reply": "Sending message to Roni, sir.",
  "sequence": [
    { "action": "OPEN_APP", "package_name": "com.facebook.orca" },
    { "action": "CLICK_TEXT", "target_text": "Search" },
    { "action": "TYPE_TEXT", "text": "রনি" },
    { "action": "CLICK_TEXT", "target_text": "রনি" },
    { "action": "TYPE_TEXT", "text": "কেমন আছো" },
    { "action": "CLICK_TEXT", "target_text": "Send" }
  ]
}

Analyze the following user command and strictly produce the JSON response:
""".trimIndent()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val responseAdapter = moshi.adapter(CharuCommandResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun interpretCommand(
        userInput: String,
        installedPackages: Map<String, String> = emptyMap()
    ): Pair<CharuCommandResponse, String> = withContext(Dispatchers.IO) {
        val cleanInput = userInput.trim()

        // 1. Try Gemini API if API key is provided and not default placeholder
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotBlank() &&
                !apiKey.contains("MY_GEMINI_API_KEY", ignoreCase = true) &&
                apiKey.length > 10

        if (hasValidKey) {
            try {
                val (response, rawJson) = callGeminiApi(cleanInput, apiKey)
                return@withContext Pair(response, rawJson)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API call failed, falling back to local neural pattern matcher", e)
            }
        }

        // 2. Intelligent Local Neural Rule Engine (Bengali + English high-speed parser)
        val localResponse = matchLocalPattern(cleanInput, installedPackages)
        val localJson = responseAdapter.indent("  ").toJson(localResponse)
        Pair(localResponse, localJson)
    }

    private fun callGeminiApi(userInput: String, apiKey: String): Pair<CharuCommandResponse, String> {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "User command: $userInput")
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", SYSTEM_PROMPT)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            throw RuntimeException("Gemini HTTP ${response.code}: $errorBody")
        }

        val bodyStr = response.body?.string() ?: throw RuntimeException("Empty response from Gemini")
        val jsonRoot = JSONObject(bodyStr)
        val candidates = jsonRoot.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = parts?.optJSONObject(0)?.optString("text").orEmpty()

        val cleanJson = cleanMarkdownJson(rawText)
        val parsed = responseAdapter.fromJson(cleanJson)
            ?: throw RuntimeException("Failed to parse JSON into CharuCommandResponse: $cleanJson")

        return Pair(parsed, cleanJson)
    }

    fun cleanMarkdownJson(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json").trim()
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```").trim()
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.removeSuffix("```").trim()
        }
        return trimmed.trim()
    }

    fun parseRawJson(rawJson: String): CharuCommandResponse? {
        return try {
            val cleaned = cleanMarkdownJson(rawJson)
            responseAdapter.fromJson(cleaned)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse raw JSON: $rawJson", e)
            null
        }
    }

    /**
     * Local intelligent pattern matcher for Bengali and English voice & text commands.
     * Ensures offline availability, lightning-fast execution, and seamless UX.
     */
    fun matchLocalPattern(
        input: String,
        installedPackages: Map<String, String> = emptyMap()
    ): CharuCommandResponse {
        val lower = input.lowercase().trim()

        // Home
        if (lower.contains("হোম স্ক্রিনে") || lower.contains("হোমে যাও") || lower.contains("home screen") || lower == "go home" || lower == "home") {
            return CharuCommandResponse(
                assistantReply = "Going to home screen.",
                action = "GLOBAL_HOME"
            )
        }

        // Back
        if (lower.contains("পিছনে যাও") || lower.contains("ব্যাক") || lower == "go back" || lower == "back") {
            return CharuCommandResponse(
                assistantReply = "Going back, sir.",
                action = "GLOBAL_BACK"
            )
        }

        // Recent apps
        if (lower.contains("রিসেন্ট") || lower.contains("সাম্প্রতিক") || lower.contains("recent apps") || lower == "recents") {
            return CharuCommandResponse(
                assistantReply = "Opening recent apps screen.",
                action = "GLOBAL_RECENTS"
            )
        }

        // Lock screen
        if (lower.contains("লক করো") || lower.contains("স্ক্রিন লক") || lower.contains("lock screen") || lower == "lock phone") {
            return CharuCommandResponse(
                assistantReply = "Locking the device, sir.",
                action = "LOCK_SCREEN"
            )
        }

        // Scroll Down
        if (lower.contains("নিচে স্ক্রোল") || lower.contains("নিচে নামো") || lower.contains("scroll down")) {
            return CharuCommandResponse(
                assistantReply = "Scrolling down, sir.",
                action = "SCROLL_DOWN"
            )
        }

        // Scroll Up
        if (lower.contains("উপরে স্ক্রোল") || lower.contains("উপরে ওঠো") || lower.contains("scroll up")) {
            return CharuCommandResponse(
                assistantReply = "Scrolling up, sir.",
                action = "SCROLL_UP"
            )
        }

        // Messenger message pattern: "মেসেঞ্জারে গিয়ে রনিকে লেখো কেমন আছো" or "send message on messenger to X saying Y"
        if (lower.contains("মেসেঞ্জার") || lower.contains("messenger")) {
            val msgMatch = extractRecipientAndMessage(input, listOf("মেসেঞ্জারে গিয়ে", "মেসেঞ্জারে", "on messenger to", "messenger to"))
            if (msgMatch != null) {
                val (recipient, text) = msgMatch
                return CharuCommandResponse(
                    assistantReply = "Sending message to $recipient, sir.",
                    sequence = listOf(
                        CharuAction(action = "OPEN_APP", packageName = "com.facebook.orca"),
                        CharuAction(action = "CLICK_TEXT", targetText = "Search"),
                        CharuAction(action = "TYPE_TEXT", text = recipient),
                        CharuAction(action = "CLICK_TEXT", targetText = recipient),
                        CharuAction(action = "TYPE_TEXT", text = text),
                        CharuAction(action = "CLICK_TEXT", targetText = "Send")
                    )
                )
            } else {
                return CharuCommandResponse(
                    assistantReply = "Opening Messenger, sir.",
                    action = "OPEN_APP",
                    packageName = "com.facebook.orca"
                )
            }
        }

        // WhatsApp message pattern
        if (lower.contains("হোয়াটসঅ্যাপ") || lower.contains("whatsapp")) {
            val msgMatch = extractRecipientAndMessage(input, listOf("হোয়াটসঅ্যাপে গিয়ে", "হোয়াটসঅ্যাপে", "on whatsapp to", "whatsapp to"))
            if (msgMatch != null) {
                val (recipient, text) = msgMatch
                return CharuCommandResponse(
                    assistantReply = "Sending WhatsApp message to $recipient, sir.",
                    sequence = listOf(
                        CharuAction(action = "OPEN_APP", packageName = "com.whatsapp"),
                        CharuAction(action = "CLICK_TEXT", targetText = "Search"),
                        CharuAction(action = "TYPE_TEXT", text = recipient),
                        CharuAction(action = "CLICK_TEXT", targetText = recipient),
                        CharuAction(action = "TYPE_TEXT", text = text),
                        CharuAction(action = "CLICK_TEXT", targetText = "Send")
                    )
                )
            } else {
                return CharuCommandResponse(
                    assistantReply = "Opening WhatsApp, sir.",
                    action = "OPEN_APP",
                    packageName = "com.whatsapp"
                )
            }
        }

        // YouTube
        if (lower.contains("ইউটিউব") || lower.contains("youtube")) {
            val query = extractSearchQuery(input, listOf("ইউটিউবে সার্চ করো", "ইউটিউবে দেখো", "search youtube for", "youtube search"))
            if (query != null && query.isNotBlank()) {
                return CharuCommandResponse(
                    assistantReply = "Searching YouTube for $query, sir.",
                    sequence = listOf(
                        CharuAction(action = "OPEN_APP", packageName = "com.google.android.youtube"),
                        CharuAction(action = "CLICK_TEXT", targetText = "Search"),
                        CharuAction(action = "TYPE_TEXT", text = query)
                    )
                )
            }
            return CharuCommandResponse(
                assistantReply = "Opening YouTube, sir.",
                action = "OPEN_APP",
                packageName = "com.google.android.youtube"
            )
        }

        // Chrome
        if (lower.contains("ক্রোম") || lower.contains("chrome") || lower.contains("ব্রাউজার") || lower.contains("browser")) {
            return CharuCommandResponse(
                assistantReply = "Opening Google Chrome, sir.",
                action = "OPEN_APP",
                packageName = "com.android.chrome"
            )
        }

        // Settings
        if (lower.contains("সেটিংস") || lower.contains("settings")) {
            return CharuCommandResponse(
                assistantReply = "Opening Settings, sir.",
                action = "OPEN_APP",
                packageName = "com.android.settings"
            )
        }

        // Phone / Dialer
        if (lower.contains("ফোন") || lower.contains("ডায়ালার") || lower.contains("phone") || lower.contains("dialer") || lower.contains("call")) {
            return CharuCommandResponse(
                assistantReply = "Opening Phone, sir.",
                action = "OPEN_APP",
                packageName = "com.google.android.dialer"
            )
        }

        // Messages
        if (lower.contains("মেসেজ") || lower.contains("messages") || lower.contains("sms")) {
            return CharuCommandResponse(
                assistantReply = "Opening Messages, sir.",
                action = "OPEN_APP",
                packageName = "com.google.android.apps.messaging"
            )
        }

        // Facebook
        if (lower.contains("ফেসবুক") || lower.contains("facebook")) {
            return CharuCommandResponse(
                assistantReply = "Opening Facebook, sir.",
                action = "OPEN_APP",
                packageName = "com.facebook.katana"
            )
        }

        // Generic Open App matcher
        val appOpenCandidate = extractAppToOpen(input)
        if (appOpenCandidate != null) {
            val matchedPackage = installedPackages.entries.firstOrNull {
                it.key.contains(appOpenCandidate, ignoreCase = true) || appOpenCandidate.contains(it.key, ignoreCase = true)
            }?.value

            if (matchedPackage != null) {
                return CharuCommandResponse(
                    assistantReply = "Opening $appOpenCandidate, sir.",
                    action = "OPEN_APP",
                    packageName = matchedPackage
                )
            }
        }

        // Fallback single action
        return CharuCommandResponse(
            assistantReply = "I am processing your directive, sir: \"$input\"",
            action = "OPEN_APP",
            packageName = "com.android.settings"
        )
    }

    private fun extractRecipientAndMessage(input: String, prefixes: List<String>): Pair<String, String>? {
        var textAfterPrefix = input
        for (prefix in prefixes) {
            val idx = textAfterPrefix.indexOf(prefix, ignoreCase = true)
            if (idx != -1) {
                textAfterPrefix = textAfterPrefix.substring(idx + prefix.length).trim()
                break
            }
        }

        val banglaRegex = Regex("(?:গিয়ে\\s+)?([^\\s]+)কে\\s+(?:লেখো|বলো|পাঠাও|জানাও)\\s+(.+)", RegexOption.IGNORE_CASE)
        val matchB = banglaRegex.find(textAfterPrefix) ?: banglaRegex.find(input)
        if (matchB != null) {
            val recipient = matchB.groupValues[1].trim()
            val text = matchB.groupValues[2].trim()
            return Pair(recipient, text)
        }

        val englishRegex = Regex("(?:to\\s+)?([\\w\\s]+?)\\s+(?:saying|write|text|send)\\s+(.+)", RegexOption.IGNORE_CASE)
        val matchE = englishRegex.find(textAfterPrefix) ?: englishRegex.find(input)
        if (matchE != null) {
            val recipient = matchE.groupValues[1].trim()
            val text = matchE.groupValues[2].trim()
            return Pair(recipient, text)
        }

        return null
    }

    private fun extractSearchQuery(input: String, prefixes: List<String>): String? {
        val lower = input.lowercase()
        for (prefix in prefixes) {
            val idx = lower.indexOf(prefix.lowercase())
            if (idx != -1) {
                return input.substring(idx + prefix.length).trim()
            }
        }
        return null
    }

    private fun extractAppToOpen(input: String): String? {
        val lower = input.lowercase()
        val openWords = listOf("open ", "launch ", "start ", "খোলো", "ওপেন করো")
        for (w in openWords) {
            if (lower.contains(w)) {
                return input.replace(w, "", ignoreCase = true).trim()
            }
        }
        return null
    }
}
