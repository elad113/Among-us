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
            text = "AMONG US • FLOATING MENU"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 30)
        }, matchWrap())

        page.addView(actionButton("ENABLE MENU & LAUNCH AMONG US") { launchWithMenu() }, matchWrap())
        page.addView(actionButton("OPEN OVERLAY SETTINGS") { requestOverlayPermission() }, matchWrap())
        page.addView(TextView(this).apply {
            text = "No Usage Access permission is needed. The floating panel stays on screen until you close it from the panel. This is an overlay shell; game-data features still need a compatible in-game plugin and are not active yet."
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

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } else {
            Toast.makeText(this, "Overlay permission is already enabled.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchWithMenu() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            Toast.makeText(this, "Allow display over other apps, then tap Launch again.", Toast.LENGTH_LONG).show()
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
