package com.sumaiya.voicecontrol

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.provider.Settings

class CommandExecutor(private val context: Context) {

    fun execute(command: String) {
        val cmd = command.lowercase().trim()

        when {
            (cmd.contains("light") || cmd.contains("torch") || cmd.contains("flash")) && cmd.contains("off") ->
                toggleFlashlight(false)
            (cmd.contains("light") || cmd.contains("torch") || cmd.contains("flash")) ->
                toggleFlashlight(true)

            cmd.endsWith(" back") || cmd.endsWith(" close") || cmd == "back" -> {
                SumaiyaAccessibilityService.instance?.goBack()
            }
            cmd.endsWith(" home") -> {
                SumaiyaAccessibilityService.instance?.goHome()
            }

            cmd.contains("wifi") -> openPanel(Settings.Panel.ACTION_WIFI)
            cmd.contains("data") -> openPanel(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)

            cmd.endsWith(" on") -> {
                val appName = cmd.removeSuffix(" on").trim()
                openApp(appName)
            }
            cmd.startsWith("open ") -> {
                val appName = cmd.substringAfter("open ").trim()
                openApp(appName)
            }
        }
    }

    private fun toggleFlashlight(turnOn: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, turnOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openPanel(action: String) {
        val intent = Intent(action)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    private fun openApp(appName: String) {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)
        for (appInfo in packages) {
            val label = pm.getApplicationLabel(appInfo).toString().lowercase()
            if (label.contains(appName)) {
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                launchIntent?.let {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }
                break
            }
        }
    }
}
