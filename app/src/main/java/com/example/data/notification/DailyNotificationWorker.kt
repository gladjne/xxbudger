// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
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
import androidx.work.*
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.repository.ThemePreferencesRepository
import com.example.ui.localization.AppLanguageSupported
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DailyNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "DailyNotificationWorker"

    override suspend fun doWork(): Result {
        val sharedPrefs = com.example.data.security.SecureStorageManager.getEncryptedSharedPreferences(applicationContext)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)

        if (!notificationsEnabled) {
            Log.d(tag, "Notifications are disabled in settings. Skipping reminder check.")
            return Result.success()
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Only run check during 20h (8 PM) or 22h (10 PM)
        if (currentHour != 20 && currentHour != 22) {
            Log.d(tag, "Current hour is $currentHour (not 20 or 22). No action needed.")
            return Result.success()
        }

        // Generate date string to prevent duplicate notifications during the same hour
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val notifyKey = "last_notified_${todayStr}_$currentHour"
        val alreadyNotified = sharedPrefs.getBoolean(notifyKey, false)

        if (alreadyNotified) {
            Log.d(tag, "Already notified user for hour $currentHour today. Skipping.")
            return Result.success()
        }

        // Calculate start of today (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val todayTxCount = database.transactionDao().getTransactionsCountForToday(startOfToday)

            if (todayTxCount > 0) {
                Log.d(tag, "User has already registered $todayTxCount transactions today. No notification needed.")
                return Result.success()
            }

            // Retrieve selected language
            val selectedLang = try {
                val repo = ThemePreferencesRepository(applicationContext)
                repo.languageFlow.first()
            } catch (e: Exception) {
                AppLanguageSupported.FRANCAIS
            }

            // We need to nudge!
            val message = when (selectedLang) {
                AppLanguageSupported.ENGLISH -> {
                    if (currentHour == 20) "Remember to add today's expenses 💸"
                    else "Final reminder to log today's budget  ✅"
                }
                AppLanguageSupported.DEUTSCH -> {
                    if (currentHour == 20) "Denke daran, deine Ausgaben für heute einzutragen 💸"
                    else "Letzte Erinnerung, dein tägliches Budget einzutragen ✅"
                }
                AppLanguageSupported.ESPANOL -> {
                    if (currentHour == 20) "Recuerda añadir tus gastos de hoy 💸"
                    else "Último recordatorio para registrar tu presupuesto diario ✅"
                }
                AppLanguageSupported.ITALIANO -> {
                    if (currentHour == 20) "Ricordati di aggiungere le spese di oggi 💸"
                    else "Ultimo promemoria per registrare il tuo budget giornaliero ✅"
                }
                AppLanguageSupported.PORTUGUES -> {
                    if (currentHour == 20) "Lembre-se de adicionar suas despesas de hoje 💸"
                    else "Último lembrete para registrar seu orçamento de hoje ✅"
                }
                AppLanguageSupported.CHINESE -> {
                    if (currentHour == 20) "记得添加今天的支出 💸"
                    else "记录今天预算的最后提醒 ✅"
                }
                AppLanguageSupported.JAPANESE -> {
                    if (currentHour == 20) "今日の支出を記録しましょう 💸"
                    else "今日の予算を記録する最後の通知 ✅"
                }
                AppLanguageSupported.KOREAN -> {
                    if (currentHour == 20) "오늘의 지출을 추가하는 것을 잊지 마세요 💸"
                    else "오늘의 예산을 기록하는 마지막 리마인더 ✅"
                }
                AppLanguageSupported.RUSSIAN -> {
                    if (currentHour == 20) "Не забудьте добавить сегодняшние расходы 💸"
                    else "Последнее напоминание записать сегодняшний бюджет ✅"
                }
                AppLanguageSupported.ARABIC -> {
                    if (currentHour == 20) "تذكر إضافة مصاريف اليوم 💸"
                    else "التذكير الأخير لتسجيل ميزانية اليوم ✅"
                }
                else -> { // French / fallback
                    if (currentHour == 20) "Pense à ajouter tes dépenses du jour 💸"
                    else "Dernier rappel pour enregistrer ton budget du jour ✅"
                }
            }

            sendNotification(message, currentHour, selectedLang)

            // Mark as notified
            sharedPrefs.edit().putBoolean(notifyKey, true).apply()
            Log.d(tag, "Successfully nudge notified for hour $currentHour.")

        } catch (e: Exception) {
            Log.e(tag, "Error processing daily notification check", e)
            return Result.retry()
        }

        return Result.success()
    }

    private fun sendNotification(message: String, notificationId: Int, lang: AppLanguageSupported) {
        val channelId = "budget_joy_reminders"
        val channelName = when (lang) {
            AppLanguageSupported.ENGLISH -> "Budget Joy Reminders"
            AppLanguageSupported.DEUTSCH -> "Budget Joy Erinnerungen"
            AppLanguageSupported.ESPANOL -> "Recordatorios de Budget Joy"
            AppLanguageSupported.ITALIANO -> "Promemoria Budget Joy"
            AppLanguageSupported.PORTUGUES -> "Lembretes do Budget Joy"
            AppLanguageSupported.CHINESE -> "Budget Joy 提醒"
            AppLanguageSupported.JAPANESE -> "Budget Joy リマインダー"
            AppLanguageSupported.KOREAN -> "Budget Joy 알림"
            AppLanguageSupported.RUSSIAN -> "Напоминания Budget Joy"
            AppLanguageSupported.ARABIC -> "تذكيرات Budget Joy"
            else -> "Rappels Budget Joy"
        }

        createNotificationChannel(applicationContext, channelId, channelName, lang)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Budget Joy")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(notificationId, builder.build())
            }
        } catch (e: Throwable) {
            Log.e(tag, "Failed to dispatch notification reminder safely", e)
        }
    }

    private fun createNotificationChannel(context: Context, channelId: String, name: String, lang: AppLanguageSupported) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = when (lang) {
                AppLanguageSupported.ENGLISH -> "Daily reminders to record your budget"
                AppLanguageSupported.DEUTSCH -> "Tägliche Erinnerungen zur Budgeterfassung"
                AppLanguageSupported.ESPANOL -> "Recordatorios diarios para registrar el presupuesto"
                AppLanguageSupported.ITALIANO -> "Promemoria giornalieri per registrare il budget"
                AppLanguageSupported.PORTUGUES -> "Lembretes diários para registrar seu orçamento"
                AppLanguageSupported.CHINESE -> "每日预算记录提醒"
                AppLanguageSupported.JAPANESE -> "予算を記録するための毎日のリマインダー"
                AppLanguageSupported.KOREAN -> "예산을 기록하기 위한 일일 리마인더"
                AppLanguageSupported.RUSSIAN -> "Ежедневные напоминания для записи бюджета"
                AppLanguageSupported.ARABIC -> "تذكيرات يومية لتسجيل الميزانية"
                else -> "Rappels quotidiens pour enregistrer le budget"
            }
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val WORK_NAME = "DailyBudgetReminderWork"

        fun scheduleOrCancel(context: Context, enabled: Boolean) {
            try {
                val workManager = WorkManager.getInstance(context)
                if (!enabled) {
                    workManager.cancelUniqueWork(WORK_NAME)
                    Log.d("DailyNotificationWorker", "Daily Notification work cancelled.")
                    return
                }

                // Periodic request running every 30 minutes to capture exact times (20h, 22h)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(
                    30, TimeUnit.MINUTES
                )
                .setConstraints(constraints)
                .build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
                Log.d("DailyNotificationWorker", "Daily Notification work scheduled periodically (every 30 mins).")
            } catch (e: Throwable) {
                Log.w("DailyNotificationWorker", "Could not schedule work manager reminder safely", e)
            }
        }
    }
}
