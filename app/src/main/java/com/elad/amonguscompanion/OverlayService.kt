package com.elad.amonguscompanion

import android.app.AppOpsManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.abs

class OverlayService : Service() {
    private val gamePackage = "com.innersloth.spacemafia"
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var wm: WindowManager
    private var bubble: TextView? = null
    private var panel: LinearLayout? = null
    private var gameIsFront = false
    private var selectedTab = "Utility"
    private val prefs by lazy { getSharedPreferences("overlay_state", MODE_PRIVATE) }

    private val poll = object : Runnable {
        override fun run() {
            val nowFront = foregroundPackage() == gamePackage
            if (nowFront != gameIsFront) {
                gameIsFront = nowFront
                if (gameIsFront) {
                    showBubble()
                    if (prefs.getBoolean("panel_open", false)) showPanel()
                } else {
                    hideViewsOnly()
                }
            }
            handler.postDelayed(this, 700)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        handler.post(poll)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (gameIsFront) {
            showBubble()
            if (prefs.getBoolean("panel_open", false)) showPanel()
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val channelId = "lokiz_overlay_service"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(channelId, "Floating menu", NotificationManager.IMPORTANCE_LOW))
        }
        val openApp = PendingIntent.getActivity(this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, channelId)
            else @Suppress("DEPRECATION") Notification.Builder(this)
        val notification = builder.setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lokiz Mods is running")
            .setContentText("Floating menu is ready for Among Us")
            .setContentIntent(openApp).setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(42, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else startForeground(42, notification)
    }

    private fun foregroundPackage(): String? {
        val ops = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = ops.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName)
        if (mode != AppOpsManager.MODE_ALLOWED) return null
        val usm = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val events = usm.queryEvents(end - 5000, end)
        val event = UsageEvents.Event()
        var latest: String? = null
        var latestTime = 0L
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if ((event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) && event.timeStamp >= latestTime) {
                latestTime = event.timeStamp
                latest = event.packageName
            }
        }
        return latest
    }

    private fun params(width: Int, height: Int) = WindowManager.LayoutParams(
        width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START; x = 18; y = 180 }

    private fun showBubble() {
        if (!gameIsFront || bubble != null) return
        val view = TextView(this).apply {
            text = "LM"; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE); gravity = Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(194, 28, 54)); cornerRadius = 18f
                setStroke(2, Color.rgb(255, 92, 111))
            }
        }
        val lp = params(dp(52), dp(52))
        try { wm.addView(view, lp); bubble = view } catch (_: Exception) { return }
        view.setOnTouchListener(object : View.OnTouchListener {
            var downX=0f; var downY=0f; var sx=0; var sy=0; var dragged=false
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { downX=e.rawX; downY=e.rawY; sx=lp.x; sy=lp.y; dragged=false; return true }
                    MotionEvent.ACTION_MOVE -> {
                        val dx=(e.rawX-downX).toInt(); val dy=(e.rawY-downY).toInt()
                        if (abs(dx)+abs(dy)>8) dragged=true
                        lp.x=sx+dx; lp.y=sy+dy
                        try { wm.updateViewLayout(v,lp) } catch (_: Exception) {}
                        return true
                    }
                    MotionEvent.ACTION_UP -> { if (!dragged) togglePanel(); return true }
                }
                return false
            }
        })
    }

    private fun togglePanel() {
        if (panel != null) { removePanel(); return }
        if (gameIsFront) showPanel()
    }

    private fun showPanel() {
        if (!gameIsFront || panel != null) return
        val root = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL
            background=android.graphics.drawable.GradientDrawable().apply {
                setColor(0xDD101116.toInt()); cornerRadius=dp(10).toFloat()
                setStroke(dp(1),0xFF74303D.toInt())
            }
            setPadding(dp(8),dp(8),dp(8),dp(8))
        }
        val header=LinearLayout(this).apply {
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF28151D.toInt()); setPadding(dp(8),dp(7),dp(8),dp(7))
        }
        header.addView(label("LOKIZ MODS",15,Color.WHITE,true),LinearLayout.LayoutParams(0,dp(38),1f))
        header.addView(label("×",20,0xFFFF657A.toInt(),true).apply {
            gravity=Gravity.CENTER; setOnClickListener { removePanel() }
        },LinearLayout.LayoutParams(dp(34),dp(38)))
        root.addView(header)
        val body=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
        val nav=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; setBackgroundColor(0xCC171820.toInt())
            setPadding(dp(3),dp(8),dp(3),dp(8))
        }
        listOf("Dashboard","Players","Utility","Account","Display","Misc").forEach { tab ->
            nav.addView(label(tab,11,if(tab==selectedTab) 0xFFFF536D.toInt() else 0xFFB6B7C2.toInt(),tab==selectedTab).apply {
                gravity=Gravity.CENTER; setPadding(dp(4),dp(13),dp(4),dp(13))
                setOnClickListener { selectedTab=tab; refreshPanel() }
            },LinearLayout.LayoutParams(dp(82),dp(46)))
        }
        body.addView(nav)
        val content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(6),dp(6),dp(4)); tag="content"; addView(tabContent()) }
        body.addView(ScrollView(this).apply { isFillViewport=true; addView(content) },LinearLayout.LayoutParams(0,dp(350),1f))
        root.addView(body); root.tag="root"
        try {
            wm.addView(root,params(dp(340),dp(430)).apply { x=dp(64); y=dp(92) })
            panel=root
            prefs.edit().putBoolean("panel_open",true).apply()
        } catch (_: Exception) { panel=null }
    }

    private fun refreshPanel() {
        val content=panel?.findViewWithTag<LinearLayout>("content") ?: return
        content.removeAllViews(); content.addView(tabContent())
    }

    private fun tabContent(): View {
        val page=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        page.addView(label(selectedTab.uppercase(),21,0xFFFF536D.toInt(),true).apply { setPadding(0,dp(5),0,dp(16)) })
        when(selectedTab) {
            "Utility" -> {
                page.addView(section("QUICK ACTIONS"))
                page.addView(info("Game integration is not connected in this build. These switches are visual placeholders, not active cheats."))
                listOf("Always Chat","Reveal Roles","Medbay Scan").forEach { page.addView(featureRow(it)) }
                page.addView(section("MOVEMENT"))
                page.addView(info("Private-lobby mod integration requires a compatible in-game plugin interface."))
            }
            "Players" -> { page.addView(section("PLAYER DATA")); page.addView(info("Live player names and roles are not available to this overlay. No roles are guessed or fabricated.")) }
            "Dashboard" -> { page.addView(info("The menu appears while Among Us is in the foreground, including the lobby and active matches. It stays open while you remain in the game.")) }
            else -> page.addView(info("$selectedTab settings will appear here when implemented."))
        }
        return page
    }

    private fun featureRow(name:String):View = LinearLayout(this).apply {
        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(8),dp(10),dp(8),dp(10))
        background=android.graphics.drawable.GradientDrawable().apply { setColor(0x442B2028); cornerRadius=dp(5).toFloat() }
        addView(label(name,14,Color.WHITE,false),LinearLayout.LayoutParams(0,dp(38),1f))
        addView(label("LOCKED",10,0xFFFF7587.toInt(),true).apply { gravity=Gravity.CENTER })
    }
    private fun section(text:String)=label(text,12,0xFFFF536D.toInt(),true).apply { setPadding(0,dp(8),0,dp(8)) }
    private fun info(text:String)=label(text,11,0xFFB8B8C2.toInt(),false).apply { setPadding(0,dp(6),0,dp(12)) }
    private fun label(text:String,size:Int,color:Int,bold:Boolean)=TextView(this).apply {
        this.text=text; textSize=size.toFloat(); setTextColor(color); if(bold) typeface=Typeface.DEFAULT_BOLD
    }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()

    private fun removePanel() {
        try { panel?.let { wm.removeView(it) } } catch (_:Exception) {}
        panel=null
        prefs.edit().putBoolean("panel_open",false).apply()
    }
    private fun hideViewsOnly() {
        try { panel?.let { wm.removeView(it) } } catch (_:Exception) {}
        try { bubble?.let { wm.removeView(it) } } catch (_:Exception) {}
        panel=null; bubble=null
    }
    override fun onDestroy() {
        handler.removeCallbacks(poll)
        hideViewsOnly()
        super.onDestroy()
    }
    override fun onBind(intent:Intent?):IBinder?=null
}
