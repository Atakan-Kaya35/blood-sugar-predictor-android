package com.ataka.bspapp.util

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import android.util.Log

object AppStateMonitor : DefaultLifecycleObserver {

    private lateinit var prefs: SharedPreferences
    private const val KEY_FOREGROUND = "is_foreground"

    // Callback when app comes to foreground
    private var onForegroundCallback: (() -> Unit)? = null

    fun initialize(application: Application) {
        prefs = application.getSharedPreferences("bsp_prefs", Context.MODE_PRIVATE)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        val wasInBackground = !isForeground()
        prefs.edit().putBoolean(KEY_FOREGROUND, true).apply()
        Log.d("BSP", "🌞 App is now in FOREGROUND")

        // Trigger callback if we were in background before
        if (wasInBackground) {
            onForegroundCallback?.invoke()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        prefs.edit().putBoolean(KEY_FOREGROUND, false).apply()
        Log.d("BSP", "🌚 App is now in BACKGROUND")
    }

    fun isForeground(): Boolean = prefs.getBoolean(KEY_FOREGROUND, true)

    fun setOnForegroundCallback(callback: () -> Unit) {
        onForegroundCallback = callback
    }
}
