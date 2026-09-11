package com.sumaiya.voicecontrol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNeededPermissions()

        val prefs = getSharedPreferences("sumaiya_prefs", MODE_PRIVATE)
        val masterSwitch = findViewById<Switch>(R.id.masterSwitch)
        val isEnabled = prefs.getBoolean("is_voice_control_enabled", false)
        masterSwitch.isChecked = isEnabled

        // যদি আগে থেকেই ON থাকে, সরাসরি সার্ভিস চালু করে দাও (backup restore হলেও যেন কাজ করে)
        if (isEnabled) {
            Toast.makeText(this, "Starting Sumaiya service...", Toast.LENGTH_LONG).show()
            startService(Intent(this, VoiceControlService::class.java))
        }

        masterSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("is_voice_control_enabled", isChecked).apply()

            if (isChecked) {
                Toast.makeText(this, "Starting Sumaiya service...", Toast.LENGTH_LONG).show()
                startService(Intent(this, VoiceControlService::class.java))
            } else {
                stopService(Intent(this, VoiceControlService::class.java))
            }
        }
    }

    private fun requestNeededPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }
}
