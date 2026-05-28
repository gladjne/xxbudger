package com.example.presentation.ui

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.BudgetUiState
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.presentation.viewmodel.AiUiState
import com.example.ui.theme.*

@Composable
fun ProfileTab(
    viewModel: BudgetViewModel,
    authViewModel: com.example.presentation.viewmodel.AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val t = com.example.ui.localization.LocalAppStrings.current
    val p = getProfileLocalization(currentLanguage)
    val notDefined = when(currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Non défini"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "No definido"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Nicht definiert"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Non definito"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Não definido"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "설정되지 않음"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "未設定"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "未定义"
        else -> "Not defined"
    }

    val noGoalDefined = when(currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Aucun objectif défini."
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Ningún objetivo definido."
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Kein Ziel definiert."
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Nessun obiettivo definito."
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Nenhum objetivo definido."
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "설정된 목표가 없습니다."
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "目標が設定されていません。"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "未设定目标。"
        else -> "No main goal defined."
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val userEmail = currentUser?.email ?: if (currentLanguage == com.example.ui.localization.AppLanguageSupported.FRANCAIS) "Démo" else "Demo"

    val userName by viewModel.userName.collectAsState()
    val userFirstName by viewModel.userFirstName.collectAsState()
    val userLastName by viewModel.userLastName.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()
    val studentLevel by viewModel.studentLevel.collectAsState()
    val primaryGoal by viewModel.primaryGoal.collectAsState()
    
    val uiState by viewModel.uiState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    val exportViewModel: com.example.presentation.viewmodel.ExportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val exportUiState by exportViewModel.exportUiState.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showFlexibleExportDialog by remember { mutableStateOf(false) }
    var exportTextState by remember { mutableStateOf("") }
    var showPrivacyScreen by remember { mutableStateOf(false) }
    var showCategoryLimitsScreen by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Dynamic budget metrics calculation
    var totalIncome = 0.0
    var totalExpense = 0.0
    var totalSaving = 0.0
    var balance = 0.0
    var savingsRate = 0.0
    var activeGoalsCount = 0

    if (uiState is BudgetUiState.Success) {
        val success = uiState as BudgetUiState.Success
        totalIncome = success.totalIncome
        totalExpense = success.totalExpense
        totalSaving = success.totalSaving
        balance = success.availableBalance
        activeGoalsCount = success.goals.size
        savingsRate = if (totalIncome > 0.0) (totalSaving / totalIncome) * 100 else 0.0
    }

    // Dynamic budget health evaluation code
    val budgetStatus = UiUtils.getBudgetStatus(totalIncome, totalExpense, balance, savingsRate, UiUtils.currentLanguage)

    val statusColor = when {
        balance < 0.0 -> ColorExpense
        savingsRate >= 10.0 -> ColorSaving
        else -> PrimaryBlue
    }

    if (showPrivacyScreen) {
        PrivacyScreen(
            viewModel = viewModel,
            authViewModel = authViewModel,
            onBack = { showPrivacyScreen = false }
        )
        return
    }

    if (showCategoryLimitsScreen) {
        CategoryLimitsScreen(
            viewModel = viewModel,
            onBack = { showCategoryLimitsScreen = false }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = p.myProfile,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontSize = 26.sp
                    ),
                    modifier = Modifier.testTag("profile_title")
                )
                Text(
                    text = p.subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 13.sp)
                )
            }

            // 1. EN-TÊTE PROFIL CARD
            val initials = remember(userFirstName, userEmail) {
                val f = userFirstName.trim()
                if (f.isNotEmpty()) {
                    f.take(2).uppercase()
                } else {
                    val mail = userEmail.trim()
                    if (mail.isNotEmpty() && mail != "Démo") {
                        val prefix = mail.substringBefore("@")
                        val parts = prefix.split(Regex("[._-]")).filter { it.isNotEmpty() }
                        if (parts.isNotEmpty()) {
                            parts.map { it.take(1) }.joinToString("").take(2).uppercase()
                        } else {
                            "J"
                        }
                    } else {
                        "J"
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_identity_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(PrimaryBlue, ColorSaving, ColorIncome, PrimaryBlue)
                                    )
                                )
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(DarkBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            )
                        }

                        // User names & level Info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (userFirstName.isNotBlank()) userFirstName else p.defaultUser,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Modifier mon profil (CTA principal unique)
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("edit_profile_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = p.editMyProfile,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // 2. "Informations personnelles"
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_details_identity_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = p.personalInfo,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    ProfileInfoRow(
                        label = t.firstName,
                        value = userFirstName
                    )
                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)
                    ProfileInfoRow(
                        label = t.email,
                        value = userEmail
                    )
                }
            }

            // 4. "Préférences" (Theme, Reminders, Onboarding, Language, Currency)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_preferences_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚙️ " + t.preferences,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    var showThemeSelector by remember { mutableStateOf(false) }
                    val currentThemeType by viewModel.currentThemeType.collectAsState()
                    val activeTheme = ThemeManager.getThemeByType(currentThemeType)
                    
                    SettingsActionRow(
                        icon = Icons.Default.Palette,
                        iconColor = PrimaryBlue,
                        title = t.visualTheme,
                        subtitle = if (t.home == "Accueil") "Thème actif : ${activeTheme.name}" else "Active theme: ${activeTheme.name}",
                        onClick = { showThemeSelector = true },
                        tag = "appearance_row"
                    )
                    
                    if (showThemeSelector) {
                        ThemeSelectionDialog(
                            currentThemeType = currentThemeType,
                            onThemeSelected = { selectedType ->
                                viewModel.selectTheme(selectedType)
                                Toast.makeText(context, if (t.home == "Accueil") "Thème appliqué" else "Theme applied", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showThemeSelector = false }
                        )
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Langue Row
                    var showLanguageSelector by remember { mutableStateOf(false) }
                    val currentLanguage by viewModel.currentLanguage.collectAsState()
                    SettingsActionRow(
                        icon = Icons.Default.Translate,
                        iconColor = PrimaryBlue,
                        title = t.language,
                        subtitle = currentLanguage.displayName,
                        onClick = { showLanguageSelector = true },
                        tag = "language_selection_row"
                    )
                    
                    if (showLanguageSelector) {
                        LanguageSelectionDialog(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = { selectedLang ->
                                viewModel.selectLanguage(selectedLang)
                                Toast.makeText(context, if (selectedLang == com.example.ui.localization.AppLanguageSupported.FRANCAIS) "Langue modifiée !" else "Language changed to ${selectedLang.displayName}!", Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showLanguageSelector = false }
                        )
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Devise Row
                    var showCurrencySelector by remember { mutableStateOf(false) }
                    val currentCurrency by viewModel.currentCurrency.collectAsState()
                    val currencyDisplayName = when (currentCurrency) {
                        "€" -> "Euro (€)"
                        "$" -> "Dollar américain ($)"
                        "£" -> "Livre sterling (£)"
                        "¥" -> "Yen (¥)"
                        "₹" -> "Roupie (₹)"
                        "FCFA" -> "Franc CFA (FCFA)"
                        "₦" -> "Naira (₦)"
                        "R" -> "Rand (R)"
                        "CA$" -> "Dollar canadien (CA$)"
                        "A$" -> "Dollar australien (A$)"
                        else -> currentCurrency
                    }

                    SettingsActionRow(
                        icon = Icons.Default.AttachMoney,
                        iconColor = ColorSaving,
                        title = t.currency,
                        subtitle = currencyDisplayName,
                        onClick = { showCurrencySelector = true },
                        tag = "currency_selection_row"
                    )
                    
                    if (showCurrencySelector) {
                        CurrencySelectionDialog(
                            currentCurrency = currentCurrency,
                            onCurrencySelected = { selectedCurrency ->
                                viewModel.selectCurrency(selectedCurrency)
                                val msg = if (t.home == "Accueil") "Devise modifiée : $selectedCurrency" else "Currency updated: $selectedCurrency"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            onDismiss = { showCurrencySelector = false }
                        )
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Rappels journaliers inline row
                    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ColorSaving.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = ColorSaving,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = t.notifications,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = if (t.home == "Accueil") "Me rappeler d'ajouter mes dépenses" else "Remind me to add my expenses",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ColorSaving,
                                checkedTrackColor = ColorSaving.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = DarkBackground
                            ),
                            modifier = Modifier.testTag("notification_switch")
                        )
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // Spending limits row
                    SettingsActionRow(
                        icon = Icons.Default.Warning,
                        iconColor = ColorExpense,
                        title = if (t.home == "Accueil") "Limites de dépenses" else "Spending Limits",
                        subtitle = if (t.home == "Accueil") "Définir des seuils et recevoir des alertes" else "Configure thresholds and receive warnings",
                        onClick = { showCategoryLimitsScreen = true },
                        tag = "spending_limits_row"
                    )

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    SettingsActionRow(
                        icon = Icons.Default.ResetTv,
                        iconColor = PrimaryBlue,
                        title = t.resetOnboarding,
                        subtitle = if (t.home == "Accueil") "Revoir la présentation complète de l'application" else "Review the app onboarding tutorial",
                        onClick = {
                            viewModel.resetOnboarding()
                            Toast.makeText(context, if (t.home == "Accueil") "Onboarding réinitialisé !" else "Onboarding reset!", Toast.LENGTH_SHORT).show()
                        },
                        tag = "onboarding_replay_row"
                    )
                }
            }

            // 5. "Documents" (Export, Share)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_documents_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = p.documents,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    val currentLang = viewModel.currentLanguage.value

                    val shareTitle = when (currentLang) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Textzusammenfassung teilen"
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Share Text Summary"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Compartir resumen de texto"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Condividi riepilogo testuale"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Compartilhar resumo em texto"
                        else -> "Partager résumé textuel"
                    }

                    val shareSubtitle = when (currentLang) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "KI-Analyse kopieren oder als Text teilen"
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Copy or share AI analysis in text format"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Copiar o compartir el análisis de IA como texto"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Copia o condividi l'analisi AI in formato testo"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Copiar ou compartilhar análise de IA em formato texto"
                        else -> "Copier ou partager l’analyse AI en format texte"
                    }

                    val pdfExportTitle = when (currentLang) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Meine Daten exportieren"
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Export My Data"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Exportar mis datos"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Esporta i miei dati"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Exportar meus dados"
                        else -> "Exporter mes données"
                    }

                    val pdfExportSubtitle = when (currentLang) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Als PDF- oder CSV-Datei für einen ausgewählten Zeitraum exportieren"
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Export to PDF or CSV format for the period of your choice"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Exportar en formato PDF o CSV para el período de su elección"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Esporta in formato PDF o CSV per il periodo preferito"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Exportar em formato PDF ou CSV para o período de sua escolha"
                        else -> "Exporter au format PDF ou CSV pour la période de votre choix"
                    }

                    val adviceHeader = when (currentLang) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "EMPFEHLUNGEN:\n"
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "ADVICE:\n"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "CONSEJOS:\n"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "CONSIGLI:\n"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "CONSELHOS:\n"
                        else -> "CONSEILS :\n"
                    }

                    val noAdviceText = when (currentLang) {
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Noch keine Analyse-Ratschläge generiert."
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "No analysis advice generated yet."
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Aún no se han generado consejos de análisis."
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Ancora nessun consiglio di analisi generato."
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Nenhum conselho de análise gerado ainda."
                        else -> "Pas de conseils d'analyse générés pour l'instant."
                    }

                    SettingsActionRow(
                        icon = Icons.Default.Share,
                        iconColor = ColorSaving,
                        title = shareTitle,
                        subtitle = shareSubtitle,
                        onClick = {
                            val aiText = when (val state = aiState) {
                                is AiUiState.Success -> {
                                    "${state.result.summary}\n\n$adviceHeader" + state.result.adviceList.joinToString("\n") { "💡 $it" }
                                }
                                else -> noAdviceText
                            }
                            exportTextState = generateExportReport(
                                userName = userFirstName,
                                primaryGoal = primaryGoal,
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                totalSaving = totalSaving,
                                balance = balance,
                                savingsRate = savingsRate,
                                aiAdvice = aiText
                            )
                            showExportDialog = true
                        },
                        tag = "export_text_row"
                    )

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    SettingsActionRow(
                        icon = Icons.Default.FileDownload,
                        iconColor = PrimaryBlue,
                        title = pdfExportTitle,
                        subtitle = pdfExportSubtitle,
                        onClick = {
                            showFlexibleExportDialog = true
                        },
                        tag = "export_pdf_button"
                    )
                }
            }

            // 6. "Sécurité & données" (Privacy)
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_security_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = p.securityAndData,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    SettingsActionRow(
                        icon = Icons.Default.Security,
                        iconColor = PrimaryBlue,
                        title = p.privacyAndData,
                        subtitle = p.privacySub,
                        onClick = { showPrivacyScreen = true },
                        tag = "privacy_policy_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Se déconnecter (Visually isolated at the absolute bottom of the page in danger colors)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { authViewModel.logout() }
                    .testTag("logout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColorExpense.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, ColorExpense.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log out icon",
                        tint = ColorExpense,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = p.logout,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ColorExpense,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // EDIT IDENTITY DIALOG MOBILE
        if (showEditDialog) {
            var tempFirstName by remember { mutableStateOf(userFirstName) }
            var tempGoal by remember { mutableStateOf(primaryGoal) }
            
            var firstNameError by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                modifier = Modifier.testTag("edit_profile_dialog"),
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempFirstName.isBlank()) {
                                firstNameError = true
                            } else {
                                viewModel.updateProfile(
                                    firstName = tempFirstName.trim(),
                                    lastName = "",
                                    displayName = tempFirstName.trim(),
                                    level = "",
                                    goal = tempGoal.trim()
                                )
                                showEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(p.save, color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text(p.cancel, color = TextSecondary)
                    }
                },
                title = {
                    Text(
                        text = p.editMyProfile,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = tempFirstName,
                            onValueChange = { 
                                tempFirstName = it
                                if (it.isNotBlank()) firstNameError = false
                            },
                            label = { Text("${t.firstName} *") },
                            isError = firstNameError,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor,
                                errorBorderColor = ColorExpense
                            ),
                            supportingText = {
                                if (firstNameError) {
                                    Text(p.firstNameRequired, color = ColorExpense)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_first_name_input")
                        )

                        OutlinedTextField(
                            value = tempGoal,
                            onValueChange = { tempGoal = it },
                            label = { Text(t.mainGoal) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("edit_profile_goal_input")
                        )
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // EXPORT REPORT DIALOG
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                modifier = Modifier.testTag("export_dialog"),
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Rapport Budget Joy", exportTextState)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSaving),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                            Text("Copier", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Rapport Budget Joy")
                                putExtra(Intent.EXTRA_TEXT, exportTextState)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager avec"))
                        }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            Text("Partager", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                title = {
                    Text(
                        text = "📤 Exportation de mon Rapport",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                },
                text = {
                    OutlinedTextField(
                        value = exportTextState,
                        onValueChange = {},
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showFlexibleExportDialog) {
            var currentExportStep by remember { mutableStateOf(1) }
            var formatChoice by remember { mutableStateOf("PDF") }
            var periodChoice by remember { mutableStateOf("THIS_MONTH") }
            
            var customStartMs by remember { mutableStateOf(System.currentTimeMillis()) }
            var customEndMs by remember { mutableStateOf(System.currentTimeMillis()) }
            
            var showStartDatePicker by remember { mutableStateOf(false) }
            var showEndDatePicker by remember { mutableStateOf(false) }

            val totalTxs = if (uiState is BudgetUiState.Success) (uiState as BudgetUiState.Success).transactions else emptyList()
            val totalGoals = if (uiState is BudgetUiState.Success) (uiState as BudgetUiState.Success).goalsAnalysis else emptyList()
            val aiResult = when (val state = aiState) {
                is AiUiState.Success -> state.result
                else -> null
            }

            AlertDialog(
                onDismissRequest = { showFlexibleExportDialog = false },
                modifier = Modifier.testTag("flexible_export_dialog"),
                title = {
                    Text(
                        text = when (currentExportStep) {
                            1 -> "Format d'exportation (1/3)"
                            2 -> "Période d'exportation (2/3)"
                            else -> "Période personnalisée (3/3)"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            fontSize = 18.sp
                        )
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (currentExportStep) {
                            1 -> {
                                Text(
                                    text = "Choisissez le format de fichier pour exporter vos données :",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // PDF option Card/Button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (formatChoice == "PDF") PrimaryBlue.copy(alpha = 0.15f) else Color(0xFF1E1E1E))
                                        .border(1.dp, if (formatChoice == "PDF") PrimaryBlue else BorderColor, RoundedCornerShape(12.dp))
                                        .clickable { formatChoice = "PDF" }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (formatChoice == "PDF"),
                                        onClick = { formatChoice = "PDF" },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue, unselectedColor = TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Rapport PDF moderne (A4)", color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Idéal pour imprimer ou partager un beau document d'analyse rédigé par Joy.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }

                                // CSV option Card/Button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (formatChoice == "CSV") PrimaryBlue.copy(alpha = 0.15f) else Color(0xFF1E1E1E))
                                        .border(1.dp, if (formatChoice == "CSV") PrimaryBlue else BorderColor, RoundedCornerShape(12.dp))
                                        .clickable { formatChoice = "CSV" }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (formatChoice == "CSV"),
                                        onClick = { formatChoice = "CSV" },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue, unselectedColor = TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Fichier CSV (Excel)", color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Idéal pour importer vos données dans Excel, Google Sheets, ou d'autres applications.", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            2 -> {
                                Text(
                                    text = "Choisissez la période à inclure dans vos données :",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                
                                val periods = listOf(
                                    "THIS_MONTH" to "Ce mois",
                                    "PREVIOUS_MONTH" to "Mois précédent",
                                    "LAST_3_MONTHS" to "3 derniers mois",
                                    "CUSTOM" to "Période personnalisée (Sélecteur de dates)"
                                )
                                
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    periods.forEach { (key, label) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (periodChoice == key) PrimaryBlue.copy(alpha = 0.12f) else Color(0xFF1E1E1E))
                                                .border(1.dp, if (periodChoice == key) PrimaryBlue else BorderColor, RoundedCornerShape(10.dp))
                                                .clickable { periodChoice = key }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = (periodChoice == key),
                                                onClick = { periodChoice = key },
                                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(label, color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                            3 -> {
                                Text(
                                    text = "Configurez l'intervalle de dates pour votre exportation :",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                                )
                                
                                val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                                
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Start date field
                                    Column {
                                        Text("Date de début", color = TextSecondary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E1E1E))
                                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                                .clickable { showStartDatePicker = true }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sdf.format(Date(customStartMs)), color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    // End date field
                                    Column {
                                        Text("Date de fin", color = TextSecondary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E1E1E))
                                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                                .clickable { showEndDatePicker = true }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sdf.format(Date(customEndMs)), color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (currentExportStep == 1) {
                                currentExportStep = 2
                            } else if (currentExportStep == 2) {
                                if (periodChoice == "CUSTOM") {
                                    currentExportStep = 3
                                } else {
                                    // Generate directly!
                                    exportViewModel.exportFlexibleReport(
                                        context = context,
                                        format = formatChoice,
                                        periodType = periodChoice,
                                        customStartDate = null,
                                        customEndDate = null,
                                        userEmail = userEmail,
                                        allTransactions = totalTxs,
                                        goals = totalGoals,
                                        aiResult = aiResult,
                                        budgetStatus = budgetStatus
                                    )
                                    showFlexibleExportDialog = false
                                }
                            } else {
                                // Step 3 confirm: Generate flexible report with dates
                                exportViewModel.exportFlexibleReport(
                                    context = context,
                                    format = formatChoice,
                                    periodType = periodChoice,
                                    customStartDate = customStartMs,
                                    customEndDate = customEndMs,
                                    userEmail = userEmail,
                                    allTransactions = totalTxs,
                                    goals = totalGoals,
                                    aiResult = aiResult,
                                    budgetStatus = budgetStatus
                                )
                                showFlexibleExportDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentExportStep == 1) "Suivant" else if (currentExportStep == 2 && periodChoice != "CUSTOM") "Exporter" else if (currentExportStep == 2) "Suivant" else "Exporter",
                            color = DarkBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            if (currentExportStep == 1) {
                                showFlexibleExportDialog = false
                            } else if (currentExportStep == 2) {
                                currentExportStep = 1
                            } else {
                                currentExportStep = 2
                            }
                        }
                    ) {
                        Text(
                            text = if (currentExportStep == 1) "Annuler" else "Retour",
                            color = TextSecondary
                        )
                    }
                },
                containerColor = DarkBackground,
                shape = RoundedCornerShape(24.dp)
            )

            // Date Pickers
            if (showStartDatePicker) {
                BudgetDatePickerDialog(
                    initialDateTimestamp = customStartMs,
                    onDateSelected = {
                        customStartMs = it
                        showStartDatePicker = false
                    },
                    onDismissRequest = { showStartDatePicker = false },
                    language = currentLanguage
                )
            }

            if (showEndDatePicker) {
                BudgetDatePickerDialog(
                    initialDateTimestamp = customEndMs,
                    onDateSelected = {
                        customEndMs = it
                        showEndDatePicker = false
                    },
                    onDismissRequest = { showEndDatePicker = false },
                    language = currentLanguage
                )
            }
        }

        ExportStatusDialog(
            exportUiState = exportUiState,
            onDismiss = { exportViewModel.resetState() }
        )
    }
}

@Composable
fun MiniStatCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground, RoundedCornerShape(16.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = accentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
fun GridLayout2x2(
    item1: @Composable () -> Unit,
    item2: @Composable () -> Unit,
    item3: @Composable () -> Unit,
    item4: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { item1() }
            Box(modifier = Modifier.weight(1f)) { item2() }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { item3() }
            Box(modifier = Modifier.weight(1f)) { item4() }
        }
    }
}

private fun generateExportReport(
    userName: String,
    primaryGoal: String,
    totalIncome: Double,
    totalExpense: Double,
    totalSaving: Double,
    balance: Double,
    savingsRate: Double,
    aiAdvice: String
): String {
    return """
        📊 RAPPORT BUDGET JOY 📊
        ===================================
        👤 PROFIL :
        - Prénom : $userName
        - Objectif : $primaryGoal
        
        💰 BILAN FINANCIER DU MOIS :
        - Revenus : ${UiUtils.formatCurrency(totalIncome)}
        - Dépenses : ${UiUtils.formatCurrency(totalExpense)}
        - Épargne : ${UiUtils.formatCurrency(totalSaving)}
        - Solde Restant : ${UiUtils.formatCurrency(balance)}
        - Taux d'Épargne : ${String.format(java.util.Locale.FRANCE, "%.1f %%", savingsRate)}
        
        ✨ ANALYSE ET RECOMMANDATIONS :
        $aiAdvice
        ===================================
        Généré automatiquement par Budget Joy 💖
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionDialog(
    currentThemeType: BudgetThemeType,
    onThemeSelected: (BudgetThemeType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                "Choisir un thème",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf(
                    ThemeManager.bentoNuit,
                    ThemeManager.oceanBlue,
                    ThemeManager.lavenderSoft,
                    ThemeManager.emeraldFinance,
                    ThemeManager.lightStudent,
                    ThemeManager.softLight,
                    ThemeManager.oceanCalm,
                    ThemeManager.greenFocus,
                    ThemeManager.lavenderClean,
                    ThemeManager.lightPro
                ).forEach { themeColors ->
                    val isSelected = themeColors.type == currentThemeType
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(themeColors.type) }
                            .testTag("theme_option_${themeColors.type.name.lowercase().replace(" ", "_")}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) themeColors.surfaceVariant else themeColors.background
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) themeColors.primaryBlue else themeColors.borderColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = themeColors.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = themeColors.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = themeColors.primaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = themeColors.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = themeColors.textSecondary
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Primary accent
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.primaryBlue)
                                    )
                                    // Background
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.background)
                                            .border(1.dp, themeColors.borderColor, CircleShape)
                                    )
                                    // Revenus dot
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.colorIncome)
                                    )
                                    // Dépenses dot
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.colorExpense)
                                    )
                                    // Épargne dot
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.colorSaving)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(28.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextWhite,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
            .run { if (tag.isNotEmpty()) testTag(tag) else this },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    onLanguageSelected: (com.example.ui.localization.AppLanguageSupported) -> Unit,
    onDismiss: () -> Unit
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(t.close, color = PrimaryBlue)
            }
        },
        title = {
            Text(t.language, color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
            ) {
                items(com.example.ui.localization.AppLanguageSupported.values().toList()) { lang ->
                    val isSelected = lang == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(lang)
                                onDismiss()
                            }
                            .background(
                                if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = lang.displayName,
                            color = if (isSelected) PrimaryBlue else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        titleContentColor = TextWhite
    )
}

@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val currencies = listOf(
        "€" to "Euro (€)",
        "$" to "Dollar américain ($)",
        "£" to "Livre sterling (£)",
        "¥" to "Yen (¥)",
        "₹" to "Roupie (₹)",
        "FCFA" to "Franc CFA (FCFA)",
        "₦" to "Naira (₦)",
        "R" to "Rand (R)",
        "CA$" to "Dollar canadien (CA$)",
        "A$" to "Dollar australien (A$)"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(t.close, color = PrimaryBlue)
            }
        },
        title = {
            Text(t.currency, color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)
            ) {
                items(currencies) { pair ->
                    val symbol = pair.first
                    val name = pair.second
                    val isSelected = symbol == currentCurrency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCurrencySelected(symbol)
                                onDismiss()
                            }
                            .background(
                                if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            color = if (isSelected) PrimaryBlue else TextWhite,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = DarkSurface,
        titleContentColor = TextWhite
    )
}

data class ProfileStrings(
    val myProfile: String,
    val subtitle: String,
    val personalInfo: String,
    val displayName: String,
    val financialSummary: String,
    val financialStatus: String,
    val savingsRate: String,
    val noOperationsAdded: String,
    val documents: String,
    val securityAndData: String,
    val privacyAndData: String,
    val privacySub: String,
    val logout: String,
    val editMyProfile: String,
    val firstNameRequired: String,
    val lastNameOptional: String,
    val displayNameOptional: String,
    val displayNameSub: String,
    val roleOptional: String,
    val mainGoal: String,
    val defaultUser: String,
    val save: String,
    val cancel: String
)

private fun getProfileLocalization(lang: com.example.ui.localization.AppLanguageSupported): ProfileStrings {
    return when (lang) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> ProfileStrings(
            myProfile = "Mon Profil",
            subtitle = "Gère ton identité et analyse tes bilans",
            personalInfo = "👤 Informations personnelles",
            displayName = "Nom affiché",
            financialSummary = "📊 Mon bilan financier",
            financialStatus = "Statut financier",
            savingsRate = "Taux Épargne",
            noOperationsAdded = "Aucune opération ajoutée pour l'instant.",
            documents = "📄 Documents",
            securityAndData = "🔒 Sécurité & données",
            privacyAndData = "Confidentialité & données",
            privacySub = "Consulter vos consentements et effacer vos données",
            logout = "Se déconnecter",
            editMyProfile = "Modifier mon profil",
            firstNameRequired = "Le prénom est obligatoire",
            lastNameOptional = "Nom (Optionnel)",
            displayNameOptional = "Nom affiché (Optionnel)",
            displayNameSub = "Utilisé pour personnaliser l’accueil. Si vide, utilise le prénom.",
            roleOptional = "Statut / rôle (Optionnel)",
            mainGoal = "Objectif principal",
            defaultUser = "Utilisateur",
            save = "Enregistrer",
            cancel = "Annuler"
        )
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> ProfileStrings(
            myProfile = "My Profile",
            subtitle = "Manage your identity and analyze your reports",
            personalInfo = "👤 Personal Information",
            displayName = "Display Name",
            financialSummary = "📊 My Financial Summary",
            financialStatus = "Financial Status",
            savingsRate = "Savings Rate",
            noOperationsAdded = "No operations added yet.",
            documents = "📄 Documents",
            securityAndData = "🔒 Security & Data",
            privacyAndData = "Privacy & Data",
            privacySub = "View your consents and clear your data",
            logout = "Log Out",
            editMyProfile = "Edit My Profile",
            firstNameRequired = "First name is required",
            lastNameOptional = "Last Name (Optional)",
            displayNameOptional = "Display Name (Optional)",
            displayNameSub = "Used to personalize the home screen. If empty, uses first name.",
            roleOptional = "Status / Role (Optional)",
            mainGoal = "Main Goal",
            defaultUser = "User",
            save = "Save",
            cancel = "Cancel"
        )
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> ProfileStrings(
            myProfile = "Mi Perfil",
            subtitle = "Gestiona tu identidad y analiza tus informes",
            personalInfo = "👤 Información Personal",
            displayName = "Nombre de Pantalla",
            financialSummary = "📊 Mi Resumen Financiero",
            financialStatus = "Estado Financiero",
            savingsRate = "Tasa de Ahorro",
            noOperationsAdded = "No hay operaciones añadidas aún.",
            documents = "📄 Documentos",
            securityAndData = "🔒 Seguridad y Datos",
            privacyAndData = "Privacidad y Datos",
            privacySub = "Consulte sus consentimientos y borre sus datos",
            logout = "Cerrar Sesión",
            editMyProfile = "Editar mi Perfil",
            firstNameRequired = "El nombre es obligatorio",
            lastNameOptional = "Apellido (Opcional)",
            displayNameOptional = "Nombre de pantalla (Opcional)",
            displayNameSub = "Se usa para personalizar la pantalla de inicio. Si está vacío, usa el nombre.",
            roleOptional = "Estado / Rol (Opcional)",
            mainGoal = "Objetivo Principal",
            defaultUser = "Usuario",
            save = "Guardar",
            cancel = "Cancelar"
        )
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> ProfileStrings(
            myProfile = "Mein Profil",
            subtitle = "Verwalten Sie Ihre Identität und analysieren Sie Ihre Berichte",
            personalInfo = "👤 Persönliche Informationen",
            displayName = "Anzeigename",
            financialSummary = "📊 Meine Finanzübersicht",
            financialStatus = "Finanzstatus",
            savingsRate = "Sparquote",
            noOperationsAdded = "Noch keine Vorgänge hinzugefügt.",
            documents = "📄 Dokumente",
            securityAndData = "🔒 Sicherheit & Daten",
            privacyAndData = "Datenschutz & Daten",
            privacySub = "Einwilligungen einsehen und Daten löschen",
            logout = "Abmelden",
            editMyProfile = "Mein Profil bearbeiten",
            firstNameRequired = "Vorname ist erforderlich",
            lastNameOptional = "Nachname (Optional)",
            displayNameOptional = "Anzeigename (Optional)",
            displayNameSub = "Wird zur Personalisierung des Startbildschirms verwendet. Wenn leer, wird der Vorname verwendet.",
            roleOptional = "Status / Rolle (Optional)",
            mainGoal = "Hauptziel",
            defaultUser = "Nutzer",
            save = "Speichern",
            cancel = "Abbrechen"
        )
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> ProfileStrings(
            myProfile = "Il mio profilo",
            subtitle = "Gestisci la tua identità e analizza i tuoi bilanci",
            personalInfo = "👤 Informazioni personali",
            displayName = "Nome visualizzato",
            financialSummary = "📊 Mio resoconto finanziario",
            financialStatus = "Stato finanziario",
            savingsRate = "Tasso di risparmio",
            noOperationsAdded = "Nessuna operazione aggiunta.",
            documents = "📄 Documenti",
            securityAndData = "🔒 Sicurezza e dati",
            privacyAndData = "Privacy e dati",
            privacySub = "Visualizza i tuoi consensi ed elimina i tuoi dati",
            logout = "Disconnettiti",
            editMyProfile = "Modifica il mio profilo",
            firstNameRequired = "Il nome è obbligatorio",
            lastNameOptional = "Cognome (Facoltativo)",
            displayNameOptional = "Nome visualizzato (Facoltativo)",
            displayNameSub = "Utilizzato per personalizzare la home page. Se vuoto, usa le nome.",
            roleOptional = "Stato / Ruolo (Facoltativo)",
            mainGoal = "Obiettivo principale",
            defaultUser = "Utente",
            save = "Salva",
            cancel = "Annulla"
        )
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> ProfileStrings(
            myProfile = "Meu Perfil",
            subtitle = "Gerencie sua identidade e analise seus relatórios",
            personalInfo = "👤 Informações Pessoais",
            displayName = "Nome de Exibição",
            financialSummary = "📊 Meu Resumo Financeiro",
            financialStatus = "Status Financeiro",
            savingsRate = "Taxa de Poupança",
            noOperationsAdded = "Nenhuma operação adicionada ainda.",
            documents = "📄 Documentos",
            securityAndData = "🔒 Segurança e Dados",
            privacyAndData = "Privacidade e Dados",
            privacySub = "Consulte seus consentimentos e apague seus dados",
            logout = "Sair",
            editMyProfile = "Editar meu Perfil",
            firstNameRequired = "O nome é obrigatório",
            lastNameOptional = "Sobrenome (Opcional)",
            displayNameOptional = "Nome de exibição (Opcional)",
            displayNameSub = "Usado para personalizar a tela inicial. Se vazio, usa o primeiro nome.",
            roleOptional = "Status / Função (Opcional)",
            mainGoal = "Objetivo Principal",
            defaultUser = "Usuário",
            save = "Salvar",
            cancel = "Cancelar"
        )
        com.example.ui.localization.AppLanguageSupported.CHINESE -> ProfileStrings(
            myProfile = "我的个人资料",
            subtitle = "管理您的身份并分析您的报告",
            personalInfo = "👤 个人信息",
            displayName = "显示名称",
            financialSummary = "📊 我的财务摘要",
            financialStatus = "财务状况",
            savingsRate = "储蓄率",
            noOperationsAdded = "尚无添加的操作。",
            documents = "📄 文件",
            securityAndData = "🔒 安全与数据",
            privacyAndData = "隐私与数据",
            privacySub = "查看您的同意并清除您的数据",
            logout = "退出登录",
            editMyProfile = "修改我的个人资料",
            firstNameRequired = "名字是必填项",
            lastNameOptional = "姓氏（选填）",
            displayNameOptional = "显示名称（选填）",
            displayNameSub = "用于个性化首页。如果为空，则使用名字。",
            roleOptional = "身分 / 角色（选填）",
            mainGoal = "主要目标",
            defaultUser = "用户",
            save = "保存",
            cancel = "取消"
        )
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> ProfileStrings(
            myProfile = "プロファイル",
            subtitle = "プロファイルの管理とレポートの分析",
            personalInfo = "👤 個人情報",
            displayName = "表示名",
            financialSummary = "📊 財務サマリー",
            financialStatus = "財務状況",
            savingsRate = "貯蓄率",
            noOperationsAdded = "取引が登録されていません。",
            documents = "📄 ドキュメント",
            securityAndData = "🔒 セキュリティとデータ",
            privacyAndData = "プライバシーとデータ",
            privacySub = "同意内容の確認とデータの消去",
            logout = "ログアウト",
            editMyProfile = "プロフィール編集",
            firstNameRequired = "名は必須項目です",
            lastNameOptional = "姓（オプション）",
            displayNameOptional = "表示名（オプション）",
            displayNameSub = "ホーム画面のパーソナライズに使用。未入力の場合は名を使用。",
            roleOptional = "ステータス / 役割（オプション）",
            mainGoal = "主な目標",
            defaultUser = "ユーザー",
            save = "保存",
            cancel = "キャンセル"
        )
        com.example.ui.localization.AppLanguageSupported.ARABIC -> ProfileStrings(
            myProfile = "ملفي الشخصي",
            subtitle = "إدارة هويتك وتحليل تقاريرك المالية",
            personalInfo = "👤 معلومات شخصية",
            displayName = "اسم العرض",
            financialSummary = "📊 ملخص مالي",
            financialStatus = "الوضع المالي",
            savingsRate = "نسبة الادخار",
            noOperationsAdded = "لم يتم إضافة أي عمليات بعد.",
            documents = "📄 المستندات",
            securityAndData = "🔒 الأمان والبيانات",
            privacyAndData = "الخصوصية والبيانات",
            privacySub = "عرض موافقاتك ومسح بياناتك",
            logout = "تسجيل الخروج",
            editMyProfile = "تعديل ملفي الشخصي",
            firstNameRequired = "الاسم الأول مطلوب",
            lastNameOptional = "اسم العائلة (اختياري)",
            displayNameOptional = "اسم العرض (اختياري)",
            displayNameSub = "يستخدم لتخصيص الشاشة الرئيسية. إذا ترك فارغًا، يتم استخدام الاسم الأول.",
            roleOptional = "المهنة / الدور (اختياري)",
            mainGoal = "الهدف الرئيسي",
            defaultUser = "مستخدم",
            save = "حفظ",
            cancel = "إلغاء"
        )
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> ProfileStrings(
            myProfile = "Мой профиль",
            subtitle = "Управление профилем и анализ отчетов",
            personalInfo = "👤 Личная информация",
            displayName = "Отображаемое имя",
            financialSummary = "📊 Финансовый отчет",
            financialStatus = "Финансовое состояние",
            savingsRate = "Уровень сбережений",
            noOperationsAdded = "Операции пока не добавлены.",
            documents = "📄 Документы",
            securityAndData = "🔒 Безопасность и данные",
            privacyAndData = "Конфиденциальность",
            privacySub = "Управление согласиями и удаление данных",
            logout = "Выйти из системы",
            editMyProfile = "Редактировать профиль",
            firstNameRequired = "Имя обязательно для заполнения",
            lastNameOptional = "Фамилия (необязательно)",
            displayNameOptional = "Отображаемое имя (необязательно)",
            displayNameSub = "Используется для персонализации главного экрана. Если пусто, используется имя.",
            roleOptional = "Статус / Роль (необязательно)",
            mainGoal = "Основная цель",
            defaultUser = "Пользователь",
            save = "Сохранить",
            cancel = "Отмена"
        )
        com.example.ui.localization.AppLanguageSupported.KOREAN -> ProfileStrings(
            myProfile = "내 프로필",
            subtitle = "사용자 정보 관리 및 재정 분석",
            personalInfo = "👤 개인 정보",
            displayName = "표시 이름",
            financialSummary = "📊 내 재정 요약",
            financialStatus = "재정 상태",
            savingsRate = "저축률",
            noOperationsAdded = "추가된 거래 전송 내역이 없습니다.",
            documents = "📄 문서",
            securityAndData = "🔒 보안 및 데이터",
            privacyAndData = "개인정보 및 데이터",
            privacySub = "동의 내역 확인 및 데이터 영구 삭제",
            logout = "로그아웃",
            editMyProfile = "프로필 수정",
            firstNameRequired = "이름은 필수 지출 항목입니다",
            lastNameOptional = "성 (선택사항)",
            displayNameOptional = "표시 이름 (선택사항)",
            displayNameSub = "홈 화면을 맞춤 설정하는 데 사용됩니다. 비워두면 이름을 사용합니다.",
            roleOptional = "학적 / 역할 (선택사항)",
            mainGoal = "주요 스마트 목표",
            defaultUser = "사용자",
            save = "저장",
            cancel = "취소"
        )
    }
}

