package com.sumaiya.voicecontrol

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.provider.Settings

class CommandExecutor(private val context: Context) {

    // পরিচিত অ্যাপের নাম, আর Vosk যেভাবে ভুল শুনতে পারে তার সম্ভাব্য ভার্সনগুলো
    private val appAliases = mapOf(
        "youtube" to listOf("youtube", "you tube", "you to go", "utube", "u tube", "you too"),
        "facebook" to listOf("facebook", "face book", "facebookk", "face buck"),
        "whatsapp" to listOf("whatsapp", "whats up", "what's app", "whats app", "watsap"),
        "messenger" to listOf("messenger", "message in jar", "message"),
        "instagram" to listOf("instagram", "insta gram", "insta"),
        "telegram" to listOf("telegram", "tele gram"),
        "chrome" to listOf("chrome", "crumb"),
        "camera" to listOf("camera", "cam era"),
        "gmail" to listOf("gmail", "g mail", "email")
    )

    fun execute(command: String) {
        val cmd = command.lowercase().trim()

        when {
            (cmd.contains("light") || cmd.contains("torch") || cmd.contains("flash")) && cmd.contains("off") ->
                toggleFlashlight(false)
            (cmd.contains("light") || cmd.contains("torch") || cmd.contains("flash")) ->
                toggleFlashlight(true)

            cmd.contains("back") || cmd.contains("close") -> {
                SumaiyaAccessibilityService.instance?.goBack()
            }
            cmd.contains("home") -> {
                SumaiyaAccessibilityService.instance?.goHome()
            }

            cmd.contains("wifi") -> openPanel(Settings.Panel.ACTION_WIFI)
            cmd.contains("data") -> openPanel(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)

            else -> tryOpenAnyApp(cmd)
        }
    }

    private fun tryOpenAnyApp(cmd: String) {
        // আগে পরিচিত অ্যাপের অ্যালিয়াস লিস্টে খোঁজা
        for ((realName, aliases) in appAliases) {
            if (aliases.any { cmd.contains(it) }) {
                openApp(realName)
                return
            }
        }
        // না পেলে, "open X" বা "X on" প্যাটার্ন থেকে নাম বের করে খোঁজা
        val appName = when {
            cmd.startsWith("open ") -> cmd.substringAfter("open ").trim()
            cmd.endsWith(" on") -> cmd.removeSuffix(" on").trim()
            else -> cmd
        }
        openApp(appName)
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
