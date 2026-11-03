package com.elad.amonguscompanion

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val gamePackage = "com.innersloth.spacemafia"
    private val bg = Color.rgb(17, 18, 24)
    private val red = Color.rgb(218, 42, 69)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 42, 28, 28)
            setBackgroundColor(bg)
        }
        page.addView(TextView(this).apply {
            text = "LOKIZ MODS"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }, matchWrap())
        page.addView(TextView(this).apply {
            text = "AMONG US • ANDROID LOADER"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 30)
        }, matchWrap())

        page.addView(actionButton("LAUNCH AMONG US") { launchWithMenu() }, matchWrap())
        page.addView(actionButton("SET UP GAME-ONLY MENU") { setupPermissions() }, matchWrap())
        page.addView(TextView(this).apply {
            text = "The Lokiz panel is shown only while Among Us is the foreground app. This build is an overlay shell; game-data features need a compatible in-game mod interface and are not active yet."
            textSize = 13f
            setTextColor(Color.rgb(190, 190, 202))
            setPadding(4, 24, 4, 0)
        }, matchWrap())
        setContentView(page)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 14 }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 15f
        setTextColor(Color.WHITE)
        setBackgroundColor(red)
        setOnClickListener { action() }
    }

    private fun setupPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        if (!hasUsageAccess()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Toast.makeText(this, "Enable Usage Access for Lokiz Mods, then return here.", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(this, "Permissions ready. Tap Launch Among Us.", Toast.LENGTH_SHORT).show()
    }

    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(),
            packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun launchWithMenu() {
        if (!Settings.canDrawOverlays(this) || !hasUsageAccess()) {
            setupPermissions()
            return
        }
        val launch = packageManager.getLaunchIntentForPackage(gamePackage)
        if (launch == null) {
            Toast.makeText(this, "Install the official Among Us app from Google Play first.", Toast.LENGTH_LONG).show()
            return
        }
        startService(Intent(this, OverlayService::class.java))
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launch)
    }
}
