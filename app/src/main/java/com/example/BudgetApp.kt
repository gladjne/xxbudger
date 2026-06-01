package com.example

import android.app.Application
import android.util.Log

class BudgetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("CRASH", "FATAL: ${throwable.stackTraceToString()}")
        }
    }
}
