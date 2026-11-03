package com.elad.amonguscompanion

import android.app.Activity
import android.app.AppOpsManager
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
    private val bg = Color.rgb(17,18,24)
    private val red = Color.rgb(218,42,69)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor=bg; window.navigationBarColor=bg
        val page=LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER_HORIZONTAL
            setPadding(28,42,28,28); setBackgroundColor(bg)
        }
        page.addView(TextView(this).apply {
            text="LOKIZ MODS"; textSize=30f; setTextColor(Color.WHITE)
            gravity=Gravity.CENTER; typeface=android.graphics.Typeface.DEFAULT_BOLD
        },matchWrap())
        page.addView(TextView(this).apply {
            text="AMONG US • FLOATING MENU"; textSize=12f; setTextColor(Color.LTGRAY)
            gravity=Gravity.CENTER; setPadding(0,6,0,30)
        },matchWrap())
        page.addView(actionButton("ENABLE BOTH PERMISSIONS & LAUNCH") { launchWithMenu() },matchWrap())
        page.addView(actionButton("OPEN OVERLAY SETTINGS") { requestOverlayPermission() },matchWrap())
        page.addView(actionButton("OPEN USAGE ACCESS SETTINGS") { requestUsageAccess() },matchWrap())
        page.addView(TextView(this).apply {
            text="Two permissions are required: Display over other apps and Usage Access. Usage Access lets the app show the floating menu only while Among Us is in the foreground."
            textSize=13f; setTextColor(Color.rgb(190,190,202)); setPadding(4,24,4,0)
        },matchWrap())
        setContentView(page)
    }

    private fun matchWrap()=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin=14 }
    private fun actionButton(label:String,action:()->Unit)=Button(this).apply {
        text=label; isAllCaps=false; textSize=15f; setTextColor(Color.WHITE)
        setBackgroundColor(red); setOnClickListener { action() }
    }
    private fun hasUsageAccess():Boolean {
        val ops=getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return ops.checkOpNoThrow("android:get_usage_stats",android.os.Process.myUid(),packageName)==AppOpsManager.MODE_ALLOWED
    }
    private fun requestOverlayPermission() {
        if(!Settings.canDrawOverlays(this)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")))
        else Toast.makeText(this,"Overlay permission is already enabled.",Toast.LENGTH_SHORT).show()
    }
    private fun requestUsageAccess() {
        if(!hasUsageAccess()) startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        else Toast.makeText(this,"Usage Access is already enabled.",Toast.LENGTH_SHORT).show()
    }
    private fun launchWithMenu() {
        if(!Settings.canDrawOverlays(this)) { requestOverlayPermission(); Toast.makeText(this,"Allow display over other apps, then tap Launch again.",Toast.LENGTH_LONG).show(); return }
        if(!hasUsageAccess()) { requestUsageAccess(); Toast.makeText(this,"Enable Usage Access, then tap Launch again.",Toast.LENGTH_LONG).show(); return }
        val launch=packageManager.getLaunchIntentForPackage(gamePackage)
        if(launch==null) { Toast.makeText(this,"Install the official Among Us app first.",Toast.LENGTH_LONG).show(); return }
        val serviceIntent=Intent(this,OverlayService::class.java)
        if(android.os.Build.VERSION.SDK_INT>=26) startForegroundService(serviceIntent) else startService(serviceIntent)
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(launch)
    }
}