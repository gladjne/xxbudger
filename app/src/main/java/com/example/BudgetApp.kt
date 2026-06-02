package com.example

import android.app.Application
import android.util.Log

class BudgetApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH", "FATAL: ${throwable.stackTraceToString()}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
