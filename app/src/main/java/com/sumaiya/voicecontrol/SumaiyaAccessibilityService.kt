package com.sumaiya.voicecontrol

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class SumaiyaAccessibilityService : AccessibilityService() {

    companion object {
        var instance: SumaiyaAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun goBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }
}
