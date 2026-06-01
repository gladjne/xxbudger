// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.security

import android.content.Context
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SupportFactory

object SecureDatabaseHelper {
    fun getOpenHelperFactory(context: Context): SupportSQLiteOpenHelper.Factory? {
        return try {
            net.sqlcipher.database.SQLiteDatabase.loadLibs(context)
            val passphraseBytes = SecureStorageManager.getDatabasePassphrase(context).toByteArray(Charsets.UTF_8)
            SupportFactory(passphraseBytes)
        } catch (t: Throwable) {
            android.util.Log.e("SECURE_DB", "Safe check: Core native SQLCipher library failed to load or initialize (e.g. UnsatisfiedLinkError on this device's ABI). Falling back: ${t.message}", t)
            null
        }
    }
}
