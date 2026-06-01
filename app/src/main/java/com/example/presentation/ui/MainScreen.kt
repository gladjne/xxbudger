// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ColorExpense
import com.example.ui.theme.BorderColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ColorSaving
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ArrowBack
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context

@Composable
fun MainScreen(
    viewModel: BudgetViewModel,
    authViewModel: com.example.presentation.viewmodel.AuthViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val passwordUpgradeRequired by authViewModel.passwordUpgradeRequired.collectAsState()
    val is2faPending by authViewModel.is2faPending.collectAsState()
    val is2faOnboardingPending by authViewModel.is2faOnboardingPending.collectAsState()
    var isRegistering by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val currencySelected by viewModel.currencySelected.collectAsState()

    if (!onboardingCompleted) {
        OnboardingScreen(
            currentLanguage = currentLanguage,
            onLanguageSelected = { viewModel.selectLanguage(it) },
            onComplete = { viewModel.completeOnboarding() }
        )
    } else if (currentUser == null) {
        AnimatedContent(
            targetState = isRegistering,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "AuthScreenSwitch"
        ) { registering ->
            if (registering) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { isRegistering = false }
                )
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { isRegistering = true }
                )
            }
        }
    } else if (is2faPending) {
        TwoFactorLoginScreen(
            authViewModel = authViewModel,
            currentLanguage = currentLanguage,
            onLogout = {
                authViewModel.logout()
            }
        )
    } else if (passwordUpgradeRequired) {
        RedefinePasswordScreen(
            authViewModel = authViewModel,
            onLogout = {
                authViewModel.logout()
            }
        )
    } else if (currentUser?.isEmailVerified == false) {
        EmailVerificationScreen(
            authViewModel = authViewModel,
            onLogout = {
                authViewModel.logout()
            }
        )
    } else if (!currencySelected) {
        CurrencySelectionScreen(
            currentLanguage = currentLanguage,
            onCurrencySelected = { viewModel.selectCurrency(it) }
        )
    } else if (is2faOnboardingPending) {
        TwoFactorOnboardingScreen(
            authViewModel = authViewModel,
            currentLanguage = currentLanguage,
            onDismiss = { authViewModel.skip2FAOnboarding() }
        )
    } else {
        val t = com.example.ui.localization.LocalAppStrings.current
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = DarkBackground,
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = PrimaryBlue,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = t.home
                            )
                        },
                        label = { Text(t.home) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = t.add
                            )
                        },
                        label = { Text(t.add) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_add")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = t.history
                            )
                        },
                        label = { Text(t.history) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_history")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = t.analysis
                            )
                        },
                        label = { Text(t.analysis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_analysis")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = t.profile
                            )
                        },
                        label = { Text(t.profile) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryBlue,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardTab(
                            uiState = uiState,
                            viewModel = viewModel,
                            onNavigateToAdd = { selectedTab = 1 },
                            onNavigateToProfile = { selectedTab = 4 },
                            currentUser = currentUser
                        )
                        1 -> AddTransactionTab(
                            viewModel = viewModel,
                            onSuccess = {
                                // After successful adding, transition to dashboard view
                                selectedTab = 0
                            }
                        )
                        2 -> HistoryTab(
                            viewModel = viewModel
                        )
                        3 -> AnalysisTab(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        4 -> ProfileTab(
                            viewModel = viewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TwoFactorLoginScreen(
    authViewModel: com.example.presentation.viewmodel.AuthViewModel,
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFrench = currentLanguage == com.example.ui.localization.AppLanguageSupported.FRANCAIS
    var codeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("two_factor_login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PrimaryBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isFrench) "Double Authentification (2FA)" else "Two-Factor Authentication (2FA)",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isFrench) {
                    "Veuillez saisir le code de sécurité temporaire à 6 chiffres généré par votre application d'authentification."
                } else {
                    "Please enter the temporary 6-digit verification code from your authenticator application."
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedTextField(
                value = codeInput,
                onValueChange = {
                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                        codeInput = it
                    }
                },
                label = { Text(if (isFrench) "Code à 6 chiffres" else "6-Digit Code") },
                placeholder = { Text("000000") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderColor,
                    focusedLabelColor = PrimaryBlue,
                    unfocusedLabelColor = TextSecondary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_2fa_code_input")
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ColorExpense,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = ColorExpense, fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    val correct = authViewModel.verifyLogin2FA(codeInput)
                    if (correct) {
                        Toast.makeText(context, if (isFrench) "Connexion réussie !" else "Logged in successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMessage = if (isFrench) "Code incorrect ou expiré." else "Incorrect or expired code."
                    }
                },
                enabled = codeInput.length == 6,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = DarkBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("verify_login_2fa_button")
            ) {
                Text(
                    text = if (isFrench) "Vérifier & Continuer" else "Verify & Continue",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = DarkBackground)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isFrench) "Retour / Se déconnecter" else "Back / Logout",
                    style = MaterialTheme.typography.bodyMedium.copy(color = ColorExpense, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

private fun buildQrCodeBitmap(text: String, size: Int = 250): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            size,
            size
        )
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        bitmap
    } catch (e: Throwable) {
        null
    }
}

@Composable
fun TwoFactorOnboardingScreen(
    authViewModel: com.example.presentation.viewmodel.AuthViewModel,
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFrench = currentLanguage == com.example.ui.localization.AppLanguageSupported.FRANCAIS
    
    var showActivationStep by remember { mutableStateOf(false) }
    var secretKey by remember { mutableStateOf<String?>(null) }
    var codeInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val qrUri = remember(secretKey) {
        val email = authViewModel.currentUser.value?.email ?: "user"
        secretKey?.let { "otpauth://totp/Budget%20Joy:$email?secret=$it&issuer=Budget%20Joy" }
    }

    val qrBitmap = remember(qrUri) {
        qrUri?.let { buildQrCodeBitmap(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("two_factor_onboarding_screen")
    ) {
        // Aesthetic backgrounds
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ColorSaving.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.8f, size.height * 0.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!showActivationStep) {
                // Step 1: Suggestion Onboarding page
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ColorSaving.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = ColorSaving,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = if (isFrench) "Sécurisez votre compte" else "Secure your account",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isFrench) {
                        "Activez l’authentification à deux facteurs pour protéger vos données financières"
                    } else {
                        "Enable two-factor authentication to protect your financial data"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Primary Button: Activate Now
                Button(
                    onClick = {
                        try {
                            secretKey = authViewModel.generateAndGet2FASecret()
                            showActivationStep = true
                        } catch (e: Exception) {
                            val errMsg = if (isFrench) "Une erreur est survenue lors de l'activation du 2FA." else "An error occurred during 2FA activation."
                            Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = DarkBackground),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("activate_now_button")
                ) {
                    Text(
                        text = if (isFrench) "Activer maintenant" else "Activate now",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DarkBackground
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Button: Later
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("later_button")
                ) {
                    Text(
                        text = if (isFrench) "Plus tard" else "Later",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                    )
                }
            } else {
                // Step 2: Active activation and QR scan interface
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showActivationStep = false
                        codeInput = ""
                        errorMessage = null
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFrench) "Configuration de la sécurité" else "Security Setup",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (isFrench) "1. Scannez le QR Code" else "1. Scan the QR Code",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextWhite, fontWeight = FontWeight.Bold),
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Text(
                                text = if (isFrench) {
                                    "Scannez ce code avec Google Authenticator ou une application équivalente."
                                } else {
                                    "Scan this code with Google Authenticator or an equivalent authentication application."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                modifier = Modifier.align(Alignment.Start)
                            )

                            // Render the dynamic QR Code image
                            qrBitmap?.let { bitmap ->
                                Box(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .background(Color.White, RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                        .align(Alignment.CenterHorizontally)
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isFrench) "2. Clé secrète manuelle" else "2. Manual Secret Key",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextWhite, fontWeight = FontWeight.Bold),
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkBackground, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = secretKey ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("2FA Secret Key", secretKey ?: "")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, if (isFrench) "Clé copiée !" else "Key copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy key",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isFrench) "3. Saisir le code de vérification" else "3. Enter Verification Code",
                                style = MaterialTheme.typography.titleSmall.copy(color = TextWhite, fontWeight = FontWeight.Bold),
                                modifier = Modifier.align(Alignment.Start)
                            )

                            OutlinedTextField(
                                value = codeInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        codeInput = it
                                    }
                                },
                                placeholder = { Text("000000") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderColor,
                                    focusedLabelColor = PrimaryBlue,
                                    unfocusedLabelColor = TextSecondary
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_2fa_code_input")
                            )

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = ColorExpense,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        showActivationStep = false
                                        codeInput = ""
                                        errorMessage = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = if (isFrench) "Retour" else "Back", color = ColorExpense)
                                }

                                Button(
                                    onClick = {
                                        val ok = authViewModel.verifyAndEnable2FA(codeInput)
                                        if (ok) {
                                            Toast.makeText(context, if (isFrench) "Authentification à deux facteurs activée !" else "Two-factor authentication enabled!", Toast.LENGTH_LONG).show()
                                        } else {
                                            errorMessage = if (isFrench) "Code invalide ou expiré." else "Invalid or expired code."
                                        }
                                    },
                                    enabled = codeInput.length == 6,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = DarkBackground),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("verify_and_enable_onboarding_2fa_button")
                                ) {
                                    Text(
                                        text = if (isFrench) "Vérifier & Activer" else "Verify & Activate",
                                        fontWeight = FontWeight.Bold,
                                        color = DarkBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
