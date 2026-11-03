package com.elad.amonguscompanion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private val prefs by lazy { getSharedPreferences("overlay_state", MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        ensureBubble()
        if (prefs.getBoolean("panel_open", false)) showPanel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Re-attach the overlay if Android recreated the sticky service.
        ensureBubble()
        if (prefs.getBoolean("panel_open", false) && panel == null) showPanel()
        return START_STICKY
    }

    private fun startAsForeground() {
        val channelId = "lokiz_overlay_service"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Floating menu", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Keeps the Among Us companion menu available"
                }
            )
        }
        val openApp = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Among Us companion is running")
            .setContentText("Floating menu stays available over the game")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                42, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(42, notification)
        }
    }

    private fun overlayParams(width: Int, height: Int) = WindowManager.LayoutParams(
        width, height,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 220 }

    private fun ensureBubble() {
        if (bubble?.isAttachedToWindow == true) return
        val view = TextView(this).apply {
            text = "AU"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(190, 38, 54))
            elevation = 12f
            contentDescription = "Open Among Us companion menu"
        }
        val lp = overlayParams(58, 58)
        try {
            wm.addView(view, lp)
            bubble = view
        } catch (_: Exception) {
            bubble = null
            return
        }
        view.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f
            var downY = 0f
            var startX = 0
            var startY = 0
            var dragged = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX; downY = event.rawY
                        startX = lp.x; startY = lp.y; dragged = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - downX).toInt()
                        val dy = (event.rawY - downY).toInt()
                        if (abs(dx) + abs(dy) > 8) dragged = true
                        lp.x = startX + dx; lp.y = startY + dy
                        try { wm.updateViewLayout(v, lp) } catch (_: Exception) {}
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!dragged) togglePanel()
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun togglePanel() {
        if (panel != null) {
            removePanel()
            return
        }
        showPanel()
    }

    private fun showPanel() {
        if (panel != null) return
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            setBackgroundColor(Color.rgb(25, 25, 32))
            elevation = 16f
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        header.addView(TextView(this).apply {
            text = "MOD MENU  •  COMPANION"
            textSize = 15f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(0, WindowManager.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(TextView(this).apply {
            text = "×"; textSize = 24f; setTextColor(Color.WHITE)
            setPadding(12, 0, 4, 4)
            setOnClickListener { removePanel() }
        })
        box.addView(header)
        addToggle(box, "Custom HUD preview")
        addToggle(box, "Player notes")
        addToggle(box, "Match timer")
        addToggle(box, "Private-lobby checklist")
        box.addView(TextView(this).apply {
            text = "Companion-only controls. No game memory or match state is modified."
            textSize = 11f; setTextColor(Color.LTGRAY); setPadding(0, 12, 0, 0)
        })
        val lp = overlayParams(290, WindowManager.LayoutParams.WRAP_CONTENT).apply {
            x = 90; y = 220
        }
        try {
            wm.addView(box, lp)
            panel = box
            prefs.edit().putBoolean("panel_open", true).apply()
        } catch (_: Exception) {
            panel = null
        }
    }

    private fun removePanel() {
        val current = panel ?: return
        try { wm.removeView(current) } catch (_: Exception) {}
        panel = null
        prefs.edit().putBoolean("panel_open", false).apply()
    }

    private fun addToggle(parent: LinearLayout, label: String) {
        var enabled = false
        parent.addView(TextView(this).apply {
            text = "○  $label"
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(0, 10, 0, 10)
            setOnClickListener {
                enabled = !enabled
                text = (if (enabled) "●  " else "○  ") + label
                setTextColor(if (enabled) Color.rgb(255, 90, 105) else Color.WHITE)
            }
        })
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep the foreground service alive when the launcher task is swiped away.
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        try { bubble?.let { wm.removeView(it) } } catch (_: Exception) {}
        try { panel?.let { wm.removeView(it) } } catch (_: Exception) {}
        bubble = null
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
