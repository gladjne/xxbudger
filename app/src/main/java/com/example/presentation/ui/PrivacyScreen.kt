// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.BudgetUiState
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.presentation.viewmodel.AiUiState
import com.example.presentation.viewmodel.AuthViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: BudgetViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentLang = viewModel.currentLanguage.value
    val p = remember(currentLang) { getPrivacyLocalization(currentLang) }
    val userEmail = currentUser?.email ?: p.demo

    val uiState by viewModel.uiState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    val exportViewModel: com.example.presentation.viewmodel.ExportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val exportUiState by exportViewModel.exportUiState.collectAsState()

    val scrollState = rememberScrollState()

    var showDeleteTransactionsConfirm by remember { mutableStateOf(false) }
    var showDeleteGoalsConfirm by remember { mutableStateOf(false) }

    // Metrics for PDF export
    var totalIncome = 0.0
    var totalExpense = 0.0
    var totalSaving = 0.0
    var balance = 0.0
    var savingsRate = 0.0

    if (uiState is BudgetUiState.Success) {
        val success = uiState as BudgetUiState.Success
        totalIncome = success.totalIncome
        totalExpense = success.totalExpense
        totalSaving = success.totalSaving
        balance = success.availableBalance
        savingsRate = if (totalIncome > 0.0) (totalSaving / totalIncome) * 100 else 0.0
    }

    val budgetStatus = UiUtils.getBudgetStatus(totalIncome, totalExpense, balance, savingsRate, UiUtils.currentLanguage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = p.privacyTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("privacy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = p.back,
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intro section
            Text(
                text = p.introText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic PDF Export Button styled as modern call to action
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val aiResult = when (val state = aiState) {
                            is AiUiState.Success -> state.result
                            else -> null
                        }
                        val txs = if (uiState is BudgetUiState.Success) (uiState as BudgetUiState.Success).transactions else emptyList()
                        val goalsList = if (uiState is BudgetUiState.Success) (uiState as BudgetUiState.Success).goalsAnalysis else emptyList()

                        exportViewModel.exportBudgetReport(
                            context = context,
                            userEmail = userEmail,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalSaving = totalSaving,
                            balance = balance,
                            savingsRate = savingsRate,
                            budgetStatus = budgetStatus,
                            goals = goalsList,
                            aiResult = aiResult,
                            transactions = txs
                        )
                    }
                    .testTag("privacy_export_pdf_button"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.12f)),
                border = BorderStroke(1.2.dp, PrimaryBlue.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(PrimaryBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export Icon",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = p.exportPdfButton,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = p.exportPdfSub,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Bento Grids of privacy components
            // 1. DONNÉES UTILISÉES
            PrivacyBentoCard(
                title = p.card1Title,
                icon = Icons.Default.Inventory,
                iconColor = PrimaryBlue,
                items = p.card1Items
            )

            // 2. UTILISATION DES DONNÉES
            PrivacyBentoCard(
                title = p.card2Title,
                icon = Icons.Default.Insights,
                iconColor = ColorSaving,
                items = p.card2Items
            )

            // 3. SECURISE & STOCKAGE
            PrivacyBentoCard(
                title = p.card3Title,
                icon = Icons.Default.Storage,
                iconColor = ColorSaving,
                items = p.card3Items
            )

            // 4. INTELLIGENCE ARTIFICIELLE
            PrivacyBentoCard(
                title = p.card4Title,
                icon = Icons.Default.AutoAwesome,
                iconColor = PrimaryBlue,
                items = p.card4Items
            )

            // 5. CONTRÔLE UTILISATEUR (ACTIONS DE SUPPRESSION)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = ColorExpense,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = p.card5Title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = p.card5Sub,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp)
                    )

                    // Logout Button
                    Button(
                        onClick = {
                            authViewModel.logout()
                            Toast.makeText(context, p.logoutToast, Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkBackground),
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                            Text(p.logout, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Delete transactions button
                    Button(
                        onClick = { showDeleteTransactionsConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExpense.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, ColorExpense.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ColorExpense, modifier = Modifier.size(16.dp))
                            Text(p.deleteTransactions, color = ColorExpense, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Delete goals button
                    Button(
                        onClick = { showDeleteGoalsConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExpense.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, ColorExpense.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ColorExpense, modifier = Modifier.size(16.dp))
                            Text(p.deleteGoals, color = ColorExpense, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SHIELD SECURITY & MASVS INTEGRITY AUDIT CARD
            val isFrench = currentLang == com.example.ui.localization.AppLanguageSupported.FRANCAIS
            val isRooted = remember { com.example.data.security.CryptoUtils.isDeviceRooted() }
            val activity = remember {
                var currentContext = context
                var foundAct: android.app.Activity? = null
                while (currentContext is android.content.ContextWrapper) {
                    if (currentContext is android.app.Activity) {
                        foundAct = currentContext
                        break
                    }
                    currentContext = currentContext.baseContext
                }
                foundAct
            }
            
            var isScreenShieldActive by remember {
                mutableStateOf(
                    activity?.let {
                        val flags = it.window?.attributes?.flags ?: 0
                        (flags and android.view.WindowManager.LayoutParams.FLAG_SECURE) != 0
                    } ?: false
                )
            }

            LaunchedEffect(isScreenShieldActive) {
                activity?.let { act ->
                    act.runOnUiThread {
                        try {
                            if (isScreenShieldActive) {
                                act.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                            } else {
                                act.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("security_audit_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, ColorSaving.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ColorSaving.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ColorSaving,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isFrench) "Audit & Bouclier de Sécurité" else "Security Shield & Diagnostics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (isFrench) "Statut d'intégrité en temps réel conforme MASVS" else "Real-time MASVS integrity status",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }

                    HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                    // 1. Root / Device integrity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isRooted) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isRooted) ColorExpense else ColorSaving,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (isFrench) "Intégrité du système" else "System Integrity",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (isFrench) (if (isRooted) "Appareil rooté (Non sécurisé)" else "Environnement certifié") else (if (isRooted) "Device rooted (Insecure)" else "Certified environment"),
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isRooted) ColorExpense.copy(alpha = 0.15f) else ColorSaving.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFrench) (if (isRooted) "COMPROMIS" else "SÉCURISÉ") else (if (isRooted) "COMPROMISED" else "SECURE"),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isRooted) ColorExpense else ColorSaving,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // 2. Cryptography Engine (Hardware security)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ColorSaving,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (isFrench) "Chiffrement matériel (AES-GCM)" else "Hardware Encryption (AES-GCM)",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (isFrench) "Clés 2FA chiffrées par Keystore d'Android" else "2FA secrets secured by Android KeyStore",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ColorSaving.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isFrench) "ACTIF" else "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ColorSaving,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // 3. Anti-Screenshot Protection Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = if (isScreenShieldActive) ColorSaving else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (isFrench) "Bouclier anti-capture" else "Anti-Screenshot Shield",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextWhite, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = if (isFrench) "Bloque les captures d'écran et logiciels d'espionnage" else "Blocks screen captures, overlays & malware recordings",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                        Switch(
                            checked = isScreenShieldActive,
                            onCheckedChange = { isChecked ->
                                isScreenShieldActive = isChecked
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ColorSaving,
                                checkedTrackColor = ColorSaving.copy(alpha = 0.4f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = BorderColor
                            )
                        )
                    }
                }
            }

            // SUPPORT & CONTACT CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Contact support",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = p.supportTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = p.supportSub,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "joy.amedjonekou@gmail.com",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Footer credits
            Text(
                text = p.footer,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }

        // Transactions Confirmation Alert
        if (showDeleteTransactionsConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteTransactionsConfirm = false },
                title = { Text(p.confirmDeleteTitle, color = TextWhite) },
                text = { Text(p.confirmDeleteTransactionsText, color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllTransactions()
                            showDeleteTransactionsConfirm = false
                            Toast.makeText(context, p.deleteToastTransactions, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExpense)
                    ) {
                        Text(p.deleteConfirm, color = TextWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteTransactionsConfirm = false }) {
                        Text(p.cancel, color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Goals Confirmation Alert
        if (showDeleteGoalsConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteGoalsConfirm = false },
                title = { Text(p.confirmDeleteTitle, color = TextWhite) },
                text = { Text(p.confirmDeleteGoalsText, color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllGoals()
                            showDeleteGoalsConfirm = false
                            Toast.makeText(context, p.deleteToastGoals, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExpense)
                    ) {
                        Text(p.deleteConfirm, color = TextWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteGoalsConfirm = false }) {
                        Text(p.cancel, color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        ExportStatusDialog(
            exportUiState = exportUiState,
            onDismiss = { exportViewModel.resetState() }
        )
    }
}

@Composable
fun PrivacyBentoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
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
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { bullet ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = iconColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

data class PrivacyStrings(
    val privacyTitle: String,
    val back: String,
    val demo: String,
    val introText: String,
    val exportPdfButton: String,
    val exportPdfSub: String,
    val card1Title: String,
    val card1Items: List<String>,
    val card2Title: String,
    val card2Items: List<String>,
    val card3Title: String,
    val card3Items: List<String>,
    val card4Title: String,
    val card4Items: List<String>,
    val card5Title: String,
    val card5Sub: String,
    val logout: String,
    val logoutToast: String,
    val deleteTransactions: String,
    val deleteGoals: String,
    val supportTitle: String,
    val supportSub: String,
    val footer: String,
    val confirmDeleteTitle: String,
    val confirmDeleteTransactionsText: String,
    val confirmDeleteGoalsText: String,
    val deleteToastTransactions: String,
    val deleteToastGoals: String,
    val deleteConfirm: String,
    val cancel: String
)

fun getPrivacyLocalization(language: com.example.ui.localization.AppLanguageSupported): PrivacyStrings {
    val englishPrivacy = PrivacyStrings(
        privacyTitle = "Privacy & Data",
        back = "Back",
        demo = "Demo",
        introText = "At Budget Joy, transparency and user control of your personal financial data are our absolute priorities. Review our privacy charter below.",
        exportPdfButton = "Export My Data to PDF",
        exportPdfSub = "Download a complete A4 report containing your ledger entries and AI diagnostics",
        card1Title = "1. Collected Data",
        card1Items = listOf(
            "Your account login credentials (email)",
            "Budget transactions recorded (amounts, details, categories)",
            "Your financial plans, lifespan goals, and milestones",
            "Consolidated budget charts and metrics",
            "Financial advice logs generated in real-time"
        ),
        card2Title = "2. Data Usage",
        card2Items = listOf(
            "Rendering real-time updates on your monthly charts",
            "Smooth sync experience across multiple devices securely",
            "Synthesizing high-fidelity graphics categorizing statements",
            "Formulating bespoke budget optimizer suggestions",
            "Drafting elegant, printable financial statements"
        ),
        card3Title = "3. Storage & Infrastructure",
        card3Items = listOf(
            "Data is persisted locally on device with advanced Room DB protocols",
            "Data is optionally backed up end-to-end with high-availability Cloud Firestore",
            "User sessions are locked securely utilizing dynamic Firebase Authentication methods"
        ),
        card4Title = "4. AI Assist Safeguards",
        card4Items = listOf(
            "When active, consolidated indices are analyzed by Gemini to generate relevant insights.",
            "If offline, local deterministic advisor modules operate silently for flawless privacy."
        ),
        card5Title = "5. Control Panel & Purge Options",
        card5Sub = "You own 100% of your records. Select appropriate diagnostic controls to easily purge or manage entries:",
        logout = "Log Out",
        logoutToast = "Log out successful!",
        deleteTransactions = "Purge All Transactions",
        deleteGoals = "Purge All Lifespan Goals",
        supportTitle = "Developer Support",
        supportSub = "For persistent technical support, deep manual account purges, or questions, escalate to developer inbox:",
        footer = "Budget Joy is meticulously tailored to maintain absolute user anonymity.\nAll rights reserved © 2026.",
        confirmDeleteTitle = "Confirm Purging Data?",
        confirmDeleteTransactionsText = "Are you sure you want to permanently delete all transactions? This clears matches on this device as well as records synced on Firestore securely.",
        confirmDeleteGoalsText = "Are you sure you want to completely delete all goals? This removes all milestones permanently from local DB and remote cloud storage.",
        deleteToastTransactions = "All recorded transactions wiped permanently.",
        deleteToastGoals = "All saving targets cleared successfully.",
        deleteConfirm = "Wipe Data",
        cancel = "Cancel"
    )

    return when (language) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> PrivacyStrings(
            privacyTitle = "Confidentialité & Données",
            back = "Retour",
            demo = "Démo",
            introText = "Chez Budget Joy, la transparence et le contrôle de vos données financières personnelles sont nos priorités absolues. Retrouvez ici le détail de notre charte d'utilisation.",
            exportPdfButton = "Exporter mes données en PDF",
            exportPdfSub = "Télécharger un extrait complet A4 contenant vos écritures et analyses",
            card1Title = "1. Données utilisées",
            card1Items = listOf(
                "Votre adresse e-mail de connexion",
                "Les transactions budgétaires saisies (montants, labels, catégories)",
                "Vos objectifs d'épargne et projets de vie",
                "Les statistiques financières générées dans l'application",
                "Les conseils d'aide budgétaire rédigés"
            ),
            card2Title = "2. Utilisation des données",
            card2Items = listOf(
                "Affichage en temps réel de votre budget mensuel",
                "Synchronisation de vos données entre vos différents appareils",
                "Génération d'analyses graphiques interactives par catégorie",
                "Élaboration de conseils d'optimisation personnalisés",
                "Exportation et partage de vos rapports au format PDF"
            ),
            card3Title = "3. Stockage & Sécurité",
            card3Items = listOf(
                "Les données sont stockées localement en toute sécurité avec Room Database",
                "Les données peuvent être synchronisées en ligne de bout en bout avec Firebase Firestore",
                "La sécurisation et l'authentification de l'utilisateur utilisent Firebase Authentication"
            ),
            card4Title = "4. Assistance IA Intelligente",
            card4Items = listOf(
                "Si Gemini AI est opérationnel, vos indicateurs budgétaires consolidés sont partagés pour formuler des recommandations.",
                "Si Gemini AI n'est pas disponible, un conseiller local hors-ligne prend automatiquement le relais pour votre anonymat total."
            ),
            card5Title = "5. Vos contrôles & suppression",
            card5Sub = "Vous êtes propriétaire exclusif de toutes vos écritures. Utilisez les actions rapides suivantes pour gérer votre espace :",
            logout = "Se déconnecter",
            logoutToast = "Déconnexion réussie !",
            deleteTransactions = "Supprimer mes transactions",
            deleteGoals = "Supprimer mes objectifs d'épargne",
            supportTitle = "Assistance & Support",
            supportSub = "Pour toute demande de suppression de données ou d'aide technique, contactez le support de l’application à l'adresse suivante :",
            footer = "Budget Joy est conçu avec soin pour garantir une confidentialité exemplaire.\nTous droits réservés © 2026.",
            confirmDeleteTitle = "Confirmer la suppression ?",
            confirmDeleteTransactionsText = "Voulez-vous vraiment supprimer définitivement toutes vos transactions ? Cette action est irréversible et supprimera vos écritures sur l'appareil ainsi que sur le cloud Firestore.",
            confirmDeleteGoalsText = "Voulez-vous vraiment effacer tous vos objectifs d'épargne ? Cette opération supprimera définitivement vos paliers locaux et distants Firestore.",
            deleteToastTransactions = "Toutes vos transactions ont été supprimées.",
            deleteToastGoals = "Tous vos objectifs ont été effacés.",
            deleteConfirm = "Supprimer",
            cancel = "Annuler"
        )
        else -> englishPrivacy
    }
}

