// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.security

import android.util.Log
import com.example.BuildConfig

object SafeLog {
    fun d(tag: String, msg: String): Int {
        if (BuildConfig.DEBUG) {
            return Log.d(tag, msg)
        }
        return 0
    }
    
    fun d(tag: String, msg: String, tr: Throwable): Int {
        if (BuildConfig.DEBUG) {
            return Log.d(tag, msg, tr)
        }
        return 0
    }

    fun e(tag: String, msg: String): Int {
        if (BuildConfig.DEBUG) {
            return Log.e(tag, msg)
        }
        return 0
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        if (BuildConfig.DEBUG) {
            return Log.e(tag, msg, tr)
        }
        return 0
    }

    fun w(tag: String, msg: String): Int {
        if (BuildConfig.DEBUG) {
            return Log.w(tag, msg)
        }
        return 0
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        if (BuildConfig.DEBUG) {
            return Log.w(tag, msg, tr)
        }
        return 0
    }

    fun i(tag: String, msg: String): Int {
        if (BuildConfig.DEBUG) {
            return Log.i(tag, msg)
        }
        return 0
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        if (BuildConfig.DEBUG) {
            return Log.i(tag, msg, tr)
        }
        return 0
    }

    fun v(tag: String, msg: String): Int {
        if (BuildConfig.DEBUG) {
            return Log.v(tag, msg)
        }
        return 0
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        if (BuildConfig.DEBUG) {
            return Log.v(tag, msg, tr)
        }
        return 0
    }
}
