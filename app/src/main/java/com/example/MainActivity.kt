package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.TransactionRepositoryImpl
import com.example.presentation.ui.MainScreen
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.presentation.viewmodel.BudgetViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize authentication layer
        val authRepository = com.example.data.repository.FirebaseAuthRepository(applicationContext)

        // 2. Initialize local persistence layer
        val database = AppDatabase.getDatabase(applicationContext)
        val transactionDao = database.transactionDao()
        val savingsGoalDao = database.savingsGoalDao()
        val debtDao = database.debtDao()
        val categoryLimitDao = database.categoryLimitDao()
        val repository = TransactionRepositoryImpl(transactionDao, savingsGoalDao, debtDao, categoryLimitDao, authRepository, applicationContext)

        // 3. Instantiate ViewModel with our custom factory
        val factory = BudgetViewModelFactory(repository, applicationContext)
        val viewModel = ViewModelProvider(this, factory)[BudgetViewModel::class.java]

        // 4. Auto-schedule daily reminders if enabled (defaults to true)
        val sharedPrefs = getSharedPreferences("budget_joy_prefs", android.content.Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
        com.example.data.notification.DailyNotificationWorker.scheduleOrCancel(applicationContext, notificationsEnabled)

        val authViewModelFactory = com.example.presentation.viewmodel.AuthViewModelFactory(authRepository)
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[com.example.presentation.viewmodel.AuthViewModel::class.java]

        setContent {
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val strings = com.example.ui.localization.getStringsForLanguage(currentLanguage)

            MyApplicationTheme {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.example.ui.localization.LocalAppStrings provides strings
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
