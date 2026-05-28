package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.ui.localization.AppLanguageSupported

object LimitNotificationHelper {
    private const val TAG = "LimitNotificationHelper"
    private const val CHANNEL_ID = "budget_joy_limits"

    fun showLimitNotification(
        context: Context,
        category: String,
        amount: Double,
        limit: Double,
        percentage: Int,
        lang: AppLanguageSupported
    ) {
        val sharedPrefs = context.getSharedPreferences("budget_joy_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        if (!notificationsEnabled) {
            Log.d(TAG, "Notifications are disabled. Skipping notification.")
            return
        }

        val channelName = when (lang) {
            AppLanguageSupported.ENGLISH -> "Budget Spending Limits"
            AppLanguageSupported.DEUTSCH -> "Budget-Ausgabelimits"
            AppLanguageSupported.ESPANOL -> "Límites de Gastos"
            AppLanguageSupported.ITALIANO -> "Limiti di Spesa"
            AppLanguageSupported.PORTUGUES -> "Limites de Despesas"
            else -> "Limites de Dépenses"
        }

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = "Alerts about spending thresholds reached or exceeded"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Build localized title and content messages
        val (title, body) = when (lang) {
            AppLanguageSupported.ENGLISH -> {
                val titleStr = when {
                    percentage >= 100 -> "⚠️ Critical Limit Exceeded!"
                    percentage >= 95 -> "⚠️ Limit Reached"
                    else -> "🔔 Approaching Limit"
                }
                val bodyStr = "Your spending in $category has reached $percentage% (${com.example.presentation.ui.UiUtils.formatCurrency(amount)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit)})."
                titleStr to bodyStr
            }
            AppLanguageSupported.DEUTSCH -> {
                val titleStr = when {
                    percentage >= 100 -> "⚠️ Kritisches Limit überschritten!"
                    percentage >= 95 -> "⚠️ Limit erreicht"
                    else -> "🔔 Limit nähert sich"
                }
                val bodyStr = "Deine Ausgaben in der Kategorie $category haben $percentage% erreicht (${com.example.presentation.ui.UiUtils.formatCurrency(amount)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit)})."
                titleStr to bodyStr
            }
            AppLanguageSupported.ESPANOL -> {
                val titleStr = when {
                    percentage >= 100 -> "⚠️ ¡Límite crítico superado!"
                    percentage >= 95 -> "⚠️ Límite alcanzado"
                    else -> "🔔 Límite cercano"
                }
                val bodyStr = "Tus gastos en la categoría $category han alcanzado el $percentage% (${com.example.presentation.ui.UiUtils.formatCurrency(amount)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit)})."
                titleStr to bodyStr
            }
            AppLanguageSupported.ITALIANO -> {
                val titleStr = when {
                    percentage >= 100 -> "⚠️ Limite critico superato!"
                    percentage >= 95 -> "⚠️ Limite raggiunto"
                    else -> "🔔 Limite vicino"
                }
                val bodyStr = "Le tue spese nella categoria $category hanno raggiunto il $percentage% (${com.example.presentation.ui.UiUtils.formatCurrency(amount)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit)})."
                titleStr to bodyStr
            }
            AppLanguageSupported.PORTUGUES -> {
                val titleStr = when {
                    percentage >= 100 -> "⚠️ Limite crítico excedido!"
                    percentage >= 95 -> "⚠️ Limite atingido"
                    else -> "🔔 Limite próximo"
                }
                val bodyStr = "Suas despesas na categoria $category atingiram $percentage% (${com.example.presentation.ui.UiUtils.formatCurrency(amount)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit)})."
                titleStr to bodyStr
            }
            else -> { // FRANCAIS or fallback
                val titleStr = when {
                    percentage >= 100 -> "⚠️ Limite de dépenses dépassée !"
                    percentage >= 95 -> "⚠️ Seuil de dépenses atteint (100%)"
                    else -> "🔔 Seuil de dépenses approché (80%)"
                }
                val bodyStr = "Tes dépenses pour la catégorie \"$category\" ont atteint $percentage% (${com.example.presentation.ui.UiUtils.formatCurrency(amount)} de ${com.example.presentation.ui.UiUtils.formatCurrency(limit)})."
                titleStr to bodyStr
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            category.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(category.hashCode(), builder.build())
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to dispatch limits notification safely", e)
        }
    }
}
