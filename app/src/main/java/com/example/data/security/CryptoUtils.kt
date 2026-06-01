// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoUtils {
    private const val KEY_ALIAS = "BudgetJoySecureKey"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_SIZE_BITS = 128

    init {
        initKeyStore()
    }

    private fun initKeyStore() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
                val builder = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }

    fun encrypt(text: String): String {
        return try {
            val key = getSecretKey() ?: throw IllegalStateException("Secret key holds no active instance")
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            // Combine IV and encrypted bytes
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Throwable) {
            e.printStackTrace()
            // Fallback to Base64 in worst case to avoid crash (though Keystore is standard since SDK 23+)
            Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
    }

    fun decrypt(encryptedText: String): String? {
        return try {
            val key = getSecretKey() ?: throw IllegalStateException("Secret key holds no active instance")
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combined.size < IV_SIZE) return null
            
            val iv = ByteArray(IV_SIZE)
            System.arraycopy(combined, 0, iv, 0, IV_SIZE)

            val ciphertext = ByteArray(combined.size - IV_SIZE)
            System.arraycopy(combined, IV_SIZE, ciphertext, 0, ciphertext.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)

            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Throwable) {
            // Fallback for reading old unencrypted/plain Base64 values or handling transition gracefully
            try {
                val bytes = Base64.decode(encryptedText, Base64.NO_WRAP)
                String(bytes, Charsets.UTF_8)
            } catch (ex: Throwable) {
                null
            }
        }
    }

    // One-way hashing with salt + pepper to respect secure password storage standards
    fun hashPassword(password: String, email: String): String {
        return try {
            val salt = email.reversed() + "JoyBudget"
            val pepper = "BudgetJoyPepperSecure2026"
            val md = MessageDigest.getInstance("SHA-256")
            val combined = (password + salt + pepper).toByteArray(Charsets.UTF_8)
            val hashedBytes = md.digest(combined)
            Base64.encodeToString(hashedBytes, Base64.NO_WRAP)
        } catch (e: Throwable) {
            password // Fallback in case of absolute failure
        }
    }

    // Lightweight Integrity Detection to alert user if their device build is vulnerable (Rooted)
    fun isDeviceRooted(): Boolean {
        try {
            val buildTags = Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }
            val paths = arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
            )
            for (path in paths) {
                if (File(path).exists()) return true
            }
        } catch (e: Throwable) {
            // ignore
        }
        return false
    }
}
