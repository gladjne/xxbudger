// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.security.SafeLog as Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream

object DownloadHelper {
    private const val tag = "DownloadHelper"

    /**
     * Saves a temporary cache file into the device's public Downloads directory under "BudgetJoy".
     * Returns the Uri of the saved file or null if unsuccessful, along with the display path.
     */
    fun saveFileToDownloads(
        context: Context,
        srcFile: File,
        displayName: String,
        mimeType: String
    ): Pair<Uri?, String> {
        val resolver = context.contentResolver
        val subDirectory = "BudgetJoy"
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$subDirectory"
        
        Log.d(tag, "Saving $displayName ($mimeType) to Downloads/$subDirectory")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                
                // Query to clean up/overwrite any existing file with the same name if possible,
                // or MediaStore will automatically append (1), (2), etc.
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(srcFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d(tag, "Saved successfully via MediaStore: $uri")
                    return Pair(uri, "Téléchargements/$subDirectory/$displayName")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error saving file via MediaStore (Android Q+)", e)
            }
        } else {
            // Legacy fallbacks for older Android
            try {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val joyDir = File(downloadsDir, subDirectory).apply { mkdirs() }
                val destFile = File(joyDir, displayName)
                
                FileInputStream(srcFile).use { inputStream ->
                    destFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                // Insert into system's MediaStore to make it visible to download managers
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.TITLE, displayName)
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.DATA, destFile.absolutePath)
                }
                
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                Log.d(tag, "Saved successfully via file system: ${destFile.absolutePath}")
                return Pair(uri ?: Uri.fromFile(destFile), "Téléchargements/$subDirectory/$displayName")
            } catch (e: Exception) {
                Log.e(tag, "Error saving file legacy way", e)
            }
        }
        
        return Pair(null, "Téléchargements/$displayName")
    }

    /**
     * Opens the physical file using FileProvider for robust viewing access across apps.
     */
    fun openFile(context: Context, file: File, mimeType: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, "Ouvrir le fichier avec").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(tag, "Failed to open file", e)
        }
    }
}
