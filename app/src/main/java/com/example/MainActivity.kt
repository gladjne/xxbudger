// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp

class MainActivity : androidx.appcompat.app.AppCompatActivity() {
    private lateinit var viewModel: BudgetViewModel
    private var isBiometricPromptShowing = false

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded(this) {
            // Decoupled: when biometric lock is active, the composition shows LockScreen, 
            // which handles prompting automatically and safely when composed.
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized && viewModel.isAppLocked.value) {
            showBiometricPrompt()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    fun showBiometricPrompt() {
        if (isBiometricPromptShowing) return
        if (!lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            return
        }
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        val authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                             androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        val status = biometricManager.canAuthenticate(authenticators)
        if (status != androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            // Auto open if system features/security are not set up or configured on this device
            viewModel.unlockApp()
            return
        }

        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isBiometricPromptShowing = false
                }

                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isBiometricPromptShowing = false
                    viewModel.unlockApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isBiometricPromptShowing = false
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.biometric_prompt_title))
            .setSubtitle(getString(R.string.biometric_prompt_subtitle))
            .setAllowedAuthenticators(authenticators)
            .build()

        try {
            isBiometricPromptShowing = true
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            isBiometricPromptShowing = false
            e.printStackTrace()
            viewModel.unlockApp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        var authRepository: com.example.data.repository.FirebaseAuthRepository? = null
        var database: AppDatabase? = null
        var repository: com.example.data.repository.TransactionRepositoryImpl? = null

        // 1. Initialize authentication layer
        try {
            authRepository = com.example.data.repository.FirebaseAuthRepository(applicationContext)
        } catch (e: Throwable) {
            android.util.Log.e("INIT_CRASH", "Failed at initializing FirebaseAuthRepository", e)
            android.widget.Toast.makeText(this, "Session init failed, running locally", android.widget.Toast.LENGTH_LONG).show()
        }

        // 2. Initialize local persistence layer
        try {
            database = AppDatabase.getDatabase(applicationContext)
        } catch (e: Throwable) {
            android.util.Log.e("INIT_CRASH", "Failed at initializing database", e)
            android.widget.Toast.makeText(this, "Database error: Falling back to temporary storage", android.widget.Toast.LENGTH_LONG).show()
        }

        val authFinal = authRepository ?: com.example.data.repository.FirebaseAuthRepository(applicationContext)

        try {
            val transactionDao = database?.transactionDao() ?: throw IllegalStateException("Database is null")
            val savingsGoalDao = database.savingsGoalDao()
            val debtDao = database.debtDao()
            val categoryLimitDao = database.categoryLimitDao()
            repository = TransactionRepositoryImpl(transactionDao, savingsGoalDao, debtDao, categoryLimitDao, authFinal, applicationContext)
        } catch (e: Throwable) {
            android.util.Log.e("INIT_CRASH", "Failed at initializing repository layers", e)
        }

        // 3. Instantiate ViewModel with our custom factory
        try {
            val repoFinal = repository ?: throw IllegalStateException("Repository is null")
            val factory = BudgetViewModelFactory(repoFinal, applicationContext)
            viewModel = ViewModelProvider(this, factory)[BudgetViewModel::class.java]
        } catch (e: Throwable) {
            android.util.Log.e("INIT_CRASH", "Failed at instantiating ViewModel", e)
            android.widget.Toast.makeText(this, "App crashed while instantiating data view", android.widget.Toast.LENGTH_LONG).show()
        }

        // 4. Auto-schedule daily reminders if enabled (defaults to true)
        try {
            val sharedPrefs = com.example.data.security.SecureStorageManager.getEncryptedSharedPreferences(this)
            val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
            com.example.data.notification.DailyNotificationWorker.scheduleOrCancel(applicationContext, notificationsEnabled)
        } catch (e: Throwable) {
            android.util.Log.e("INIT_CRASH", "Failed at scheduling notifications", e)
        }

        var authViewModel: com.example.presentation.viewmodel.AuthViewModel? = null
        try {
            val authViewModelFactory = com.example.presentation.viewmodel.AuthViewModelFactory(authFinal)
            authViewModel = ViewModelProvider(this, authViewModelFactory)[com.example.presentation.viewmodel.AuthViewModel::class.java]
        } catch (e: Throwable) {
            android.util.Log.e("INIT_CRASH", "Failed at instantiating AuthViewModel", e)
        }

        setContent {
            if (!::viewModel.isInitialized) {
                MyApplicationTheme {
                    androidx.compose.material3.Surface(
                        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            androidx.compose.foundation.layout.Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator()
                                androidx.compose.material3.Text("Initialisation de l'application réinitialisée...")
                            }
                        }
                    }
                }
                return@setContent
            }

            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val currentCurrency by viewModel.currentCurrency.collectAsState()
            val strings = com.example.ui.localization.getStringsForLanguage(currentLanguage)

            // Dynamic locale binding for system and Compose
            val localeTag = when (currentLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "fr"
                com.example.ui.localization.AppLanguageSupported.ENGLISH -> "en"
                com.example.ui.localization.AppLanguageSupported.ESPANOL -> "es"
                com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "de"
                com.example.ui.localization.AppLanguageSupported.ITALIANO -> "it"
                com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "pt"
                com.example.ui.localization.AppLanguageSupported.CHINESE -> "zh"
                com.example.ui.localization.AppLanguageSupported.JAPANESE -> "ja"
                com.example.ui.localization.AppLanguageSupported.ARABIC -> "ar"
                com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "ru"
                com.example.ui.localization.AppLanguageSupported.KOREAN -> "ko"
            }
            androidx.compose.runtime.LaunchedEffect(localeTag) {
                try {
                    val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                    val primaryLocale = if (currentLocales.isEmpty) null else currentLocales.get(0)
                    val needsUpdate = primaryLocale == null || primaryLocale.language != localeTag
                    if (needsUpdate) {
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                            androidx.core.os.LocaleListCompat.forLanguageTags(localeTag)
                        )
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }

            val currencyFormatter = androidx.compose.runtime.remember(currentCurrency, currentLanguage) {
                com.example.presentation.ui.CurrencyFormatter(currentCurrency, currentLanguage)
            }

            MyApplicationTheme {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.example.ui.localization.LocalAppStrings provides strings,
                    com.example.presentation.ui.LocalCurrencyFormatter provides currencyFormatter
                ) {
                    val isAppLocked by viewModel.isAppLocked.collectAsState()
                    if (isAppLocked) {
                        com.example.presentation.ui.LockScreen(
                            onUnlockClick = {
                                showBiometricPrompt()
                            }
                        )
                    } else {
                        val authViewModelNonNull = authViewModel ?: ViewModelProvider(this@MainActivity, com.example.presentation.viewmodel.AuthViewModelFactory(authFinal))[com.example.presentation.viewmodel.AuthViewModel::class.java]
                        MainScreen(
                            viewModel = viewModel,
                            authViewModel = authViewModelNonNull
                        )
                    }
                }
            }
        }
    }
}
