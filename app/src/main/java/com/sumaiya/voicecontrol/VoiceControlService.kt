package com.sumaiya.voicecontrol

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import org.json.JSONObject

class VoiceControlService : Service(), RecognitionListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private val channelId = "sumaiya_voice_channel"
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        try {
            startForegroundWithNotification("Sumaiya", "Starting...")
            showToast("Sumaiya service started")
            initModel()
        } catch (e: Exception) {
            showToast("CRASH in onCreate: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun initModel() {
        updateNotification("Loading voice model...")
        StorageService.unpack(this, "model", "model",
            { loadedModel ->
                model = loadedModel
                updateNotification("Model loaded. Listening...")
                showToast("Model loaded, listening")
                startListening()
            },
            { exception ->
                updateNotification("Model ERROR: ${exception.message}")
                showToast("Model error: ${exception.message}")
            }
        )
    }

    private fun startListening() {
        model?.let {
            try {
                val recognizer = Recognizer(it, 16000.0f)
                speechService = SpeechService(recognizer, 16000.0f)
                speechService?.startListening(this)
                updateNotification("Listening now...")
            } catch (e: Exception) {
                updateNotification("Listen ERROR: ${e.message}")
                showToast("Listen error: ${e.message}")
            }
        }
    }

    override fun onResult(hypothesis: String?) {
        hypothesis?.let {
            val text = JSONObject(it).optString("text", "")
            updateNotification("Heard: \"$text\"")
            if (text.isNotEmpty()) {
                showToast("Heard: $text")
                CommandExecutor(applicationContext).execute(text)
            }
        }
    }

    override fun onPartialResult(hypothesis: String?) {
        hypothesis?.let {
            val text = JSONObject(it).optString("partial", "")
            if (text.isNotEmpty()) {
                updateNotification("Hearing: \"$text\"...")
            }
        }
    }

    override fun onFinalResult(hypothesis: String?) {}
    override fun onError(exception: Exception?) {
        updateNotification("Error: ${exception?.message}")
        showToast("Recognition error: ${exception?.message}")
    }
    override fun onTimeout() {
        updateNotification("Timeout, restarting...")
        mainHandler.postDelayed({ startListening() }, 500)
    }

    private fun showToast(msg: String) {
        mainHandler.post {
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun startForegroundWithNotification(title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Sumaiya Voice Control",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sumaiya")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    override fun onDestroy() {
        speechService?.stop()
        speechService?.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
