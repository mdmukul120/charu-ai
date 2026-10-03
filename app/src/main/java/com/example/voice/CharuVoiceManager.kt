package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class CharuVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "CharuVoiceManager"
        private const val UTTERANCE_ID = "CharuUtteranceId"
    }

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(false)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    var isVoiceOutputEnabled: Boolean = true

    private var currentOnResult: ((String) -> Unit)? = null
    private var currentOnError: ((String) -> Unit)? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale.US
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(1.02f)
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    // If continuous mode is on, resume listening automatically after speaking
                    if (_isContinuousMode.value) {
                        mainHandler.postDelayed({
                            restartListeningIfActive()
                        }, 400)
                    }
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    if (_isContinuousMode.value) {
                        mainHandler.postDelayed({
                            restartListeningIfActive()
                        }, 400)
                    }
                }
            })
            Log.i(TAG, "TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS init error: $status")
        }
    }

    fun setContinuousListening(enabled: Boolean) {
        _isContinuousMode.value = enabled
        if (enabled && !_isListening.value && !_isSpeaking.value) {
            startListeningInternal()
        } else if (!enabled) {
            stopListening()
        }
    }

    fun toggleContinuousListening() {
        setContinuousListening(!_isContinuousMode.value)
    }

    fun speak(text: String) {
        if (!isVoiceOutputEnabled || !isTtsReady || text.isBlank()) return
        try {
            // Temporarily pause recognizer while speaking so it doesn't transcribe its own voice
            if (_isListening.value) {
                speechRecognizer?.stopListening()
                _isListening.value = false
            }
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error in TTS speak", e)
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        currentOnResult = onResult
        currentOnError = onError
        startListeningInternal()
    }

    private fun startListeningInternal() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            currentOnError?.invoke("Speech recognition not available on this device")
            return
        }

        stopSpeaking()
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying previous recognizer", e)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _audioRms.value = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _audioRms.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _audioRms.value = 0f

                    // In continuous mode, auto-recover on timeout / no-match
                    if (_isContinuousMode.value) {
                        mainHandler.postDelayed({
                            restartListeningIfActive()
                        }, 500)
                    } else {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            else -> "Speech recognition error ($error)"
                        }
                        currentOnError?.invoke(msg)
                    }
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val topText = matches?.firstOrNull()

                    if (!topText.isNullOrBlank()) {
                        currentOnResult?.invoke(topText)
                    }

                    // In continuous mode, continue listening after execution
                    if (_isContinuousMode.value && !_isSpeaking.value) {
                        mainHandler.postDelayed({
                            restartListeningIfActive()
                        }, 700)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            currentOnError?.invoke("Could not start speech listening: ${e.message}")
        }
    }

    private fun restartListeningIfActive() {
        if (_isContinuousMode.value && !_isSpeaking.value && !_isListening.value) {
            startListeningInternal()
        }
    }

    fun stopListening() {
        _isListening.value = false
        _audioRms.value = 0f
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down CharuVoiceManager", e)
        }
    }
}
