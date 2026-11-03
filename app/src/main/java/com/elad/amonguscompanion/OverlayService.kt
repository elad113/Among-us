package com.elad.amonguscompanion

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
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

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showBubble()
    }

    private fun overlayParams(width: Int, height: Int) = WindowManager.LayoutParams(
        width, height,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = 24; y = 220 }

    private fun showBubble() {
        val view = TextView(this).apply {
            text = "AU"
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(190, 38, 54))
        }
        bubble = view
        val lp = overlayParams(58, 58)
        wm.addView(view, lp)
        view.setOnTouchListener(object : View.OnTouchListener {
            var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var dragged = false
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
                        wm.updateViewLayout(v, lp)
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
        panel?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            panel = null
            return
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
            setBackgroundColor(Color.rgb(25, 25, 32))
        }
        box.addView(TextView(this).apply {
            text = "MOD MENU  •  COMPANION"
            textSize = 16f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE); setPadding(0, 0, 0, 12)
        })
        addToggle(box, "Custom HUD preview")
        addToggle(box, "Player notes")
        addToggle(box, "Match timer")
        addToggle(box, "Private-lobby checklist")
        box.addView(TextView(this).apply {
            text = "Companion-only controls. No game memory or match state is modified."
            textSize = 11f; setTextColor(Color.LTGRAY); setPadding(0, 12, 0, 0)
        })
        panel = box
        wm.addView(box, overlayParams(290, WindowManager.LayoutParams.WRAP_CONTENT).apply {
            x = 90; y = 220
        })
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

    override fun onDestroy() {
        try { bubble?.let { wm.removeView(it) } } catch (_: Exception) {}
        try { panel?.let { wm.removeView(it) } } catch (_: Exception) {}
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
