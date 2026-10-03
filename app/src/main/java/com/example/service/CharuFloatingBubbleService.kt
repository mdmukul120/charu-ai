package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharuFloatingBubbleService : Service() {

    companion object {
        private const val CHANNEL_ID = "charu_floating_bubble_channel"
        private const val NOTIFICATION_ID = 2001

        private val _isBubbleActive = MutableStateFlow(false)
        val isBubbleActive: StateFlow<Boolean> = _isBubbleActive.asStateFlow()

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                val intent = Intent(context, CharuFloatingBubbleService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CharuFloatingBubbleService::class.java)
            context.stopService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            initFloatingBubble()
            _isBubbleActive.value = true
        } else {
            stopSelf()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initFloatingBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        // Programmatic cybernetic floating bubble view
        val density = resources.displayMetrics.density
        val bubbleSize = (64 * density).toInt()

        val rootLayout = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#0A0E17"))
                setStroke((2 * density).toInt(), Color.parseColor("#00E5FF"))
            }
            background = bg
            elevation = 16 * density
        }

        val iconView = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_btn_speak_now)
            setColorFilter(Color.parseColor("#00E5FF"))
            val padding = (16 * density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val layoutParams = FrameLayout.LayoutParams(bubbleSize, bubbleSize)
        rootLayout.addView(iconView, layoutParams)

        // Draggable & Click gesture handler
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoving = false

        rootLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isMoving = true
                    }
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    windowManager?.updateViewLayout(rootLayout, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoving) {
                        // Single tap on floating bubble: Bring Charu to foreground
                        val launchIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(launchIntent)
                    }
                    true
                }
                else -> false
            }
        }

        floatingView = rootLayout
        windowManager?.addView(floatingView, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Charu Floating Bubble Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Charu floating assistant active on screen"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Charu AI Floating Controller Active")
            .setContentText("Tap floating bubble on screen to interact with Charu OS")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        _isBubbleActive.value = false
        floatingView?.let {
            windowManager?.removeView(it)
        }
        floatingView = null
    }
}
