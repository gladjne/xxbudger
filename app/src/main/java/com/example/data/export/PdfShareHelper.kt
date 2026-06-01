// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.security.SafeLog as Log
import androidx.core.content.FileProvider
import java.io.File

object PdfShareHelper {
    private const val tag = "PdfShareHelper"

    /**
     * Shares any file with a specific mime type
     */
    fun shareFile(context: Context, file: File, mimeType: String, subject: String, text: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Exporter et partager via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(tag, "Failed to build share intent", e)
        }
    }

    /**
     * Shares a local PDF file using Android system's built-in sharing sheet (email, WhatsApp, Drive, etc.)
     */
    fun sharePdf(context: Context, pdfFile: File) {
        shareFile(
            context = context,
            file = pdfFile,
            mimeType = "application/pdf",
            subject = "Rapport Budget Joy",
            text = "Voici mon rapport mensuel de budget généré par Budget Joy ! 🎯"
        )
    }
}

