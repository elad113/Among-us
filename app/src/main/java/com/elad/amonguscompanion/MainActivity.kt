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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 16, 20)
        window.navigationBarColor = Color.rgb(16, 16, 20)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 48, 28, 28)
            setBackgroundColor(Color.rgb(16, 16, 20))
        }
        page.addView(TextView(this).apply {
            text = "AMONG US"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, matchWrap())
        page.addView(TextView(this).apply {
            text = "COMPANION LAUNCHER  •  ANDROID"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 32)
        }, matchWrap())

        page.addView(actionButton("LAUNCH OFFICIAL AMONG US") { launchGame() }, matchWrap())
        page.addView(actionButton("ENABLE FLOATING MENU") { enableOverlay() }, matchWrap())
        page.addView(actionButton("OPEN MENU PREVIEW") { startOverlay() }, matchWrap())

        page.addView(TextView(this).apply {
            text = "Companion overlay\nThe floating panel provides companion-only controls and notes. Android does not allow this separate app to inject code into Among Us or change public-match game state."
            textSize = 13f
            setTextColor(Color.rgb(190, 190, 200))
            setPadding(4, 28, 4, 0)
        }, matchWrap())
        setContentView(page)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 14 }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(190, 38, 54))
        setOnClickListener { action() }
    }

    private fun launchGame() {
        val intent = packageManager.getLaunchIntentForPackage(gamePackage)
        if (intent == null) {
            Toast.makeText(this, "Install Among Us from Google Play first.", Toast.LENGTH_LONG).show()
        } else startActivity(intent)
    }

    private fun enableOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        } else startOverlay()
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Allow display over other apps first.", Toast.LENGTH_LONG).show()
            enableOverlay()
            return
        }
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Floating companion menu enabled.", Toast.LENGTH_SHORT).show()
    }
}
