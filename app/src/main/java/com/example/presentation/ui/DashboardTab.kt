// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.presentation.viewmodel.AiUiState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.presentation.viewmodel.BudgetUiState
import com.example.domain.analytics.BudgetAnalysisResult
import com.example.domain.analytics.GoalAnalysis
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.example.domain.analytics.AdviceColorType
import com.example.ui.theme.*
import com.example.data.sync.SyncManager
import com.example.data.sync.SyncState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardTab(
    uiState: BudgetUiState,
    viewModel: com.example.presentation.viewmodel.BudgetViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: com.example.domain.model.UserSession? = null
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()
    val userFirstName by viewModel.userFirstName.collectAsState()
    val userLastName by viewModel.userLastName.collectAsState()

    val initials = remember(userFirstName, userLastName, userDisplayName, currentUser) {
        val f = userFirstName.trim()
        val l = userLastName.trim()
        val d = userDisplayName.trim()
        if (f.isNotEmpty() && l.isNotEmpty()) {
            "${f.take(1)}${l.take(1)}".uppercase()
        } else {
            val dParts = d.split(Regex("\\s+")).filter { it.isNotEmpty() }
            if (dParts.size >= 2) {
                "${dParts[0].take(1)}${dParts[1].take(1)}".uppercase()
            } else if (f.isNotEmpty()) {
                f.take(1).uppercase()
            } else if (l.isNotEmpty()) {
                l.take(1).uppercase()
            } else if (dParts.isNotEmpty()) {
                dParts[0].take(1).uppercase()
            } else {
                val mail = currentUser?.email?.trim() ?: ""
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
    }

    val greetingName = remember(userDisplayName, userFirstName, currentUser) {
        val fName = userFirstName.trim()
        val dName = userDisplayName.trim()
        if (fName.isNotEmpty()) {
            fName
        } else if (dName.isNotEmpty()) {
            dName
        } else {
            val fbDisplayName = currentUser?.displayName?.trim()
            if (!fbDisplayName.isNullOrEmpty()) {
                fbDisplayName
            } else {
                val email = currentUser?.email?.trim() ?: ""
                if (email.isNotEmpty() && email.contains("@")) {
                    email.substringBefore("@")
                } else {
                    ""
                }
            }
        }
    }

    val greetingText = if (greetingName.isNotEmpty()) {
        try {
            t.helloWithName.format(greetingName)
        } catch (e: Exception) {
            "Hello $greetingName 👋"
        }
    } else {
        t.helloNoName
    }

    when (uiState) {
        is BudgetUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
        is BudgetUiState.Success -> {
            // Calculate a dynamic savings progress percentage for Bento Card #4
            val savingPercentage = if (uiState.totalIncome > 0) {
                ((uiState.totalSaving / uiState.totalIncome) * 100).coerceIn(0.0, 100.0).toInt()
            } else {
                0
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header (With Joy JD Avatar matching Bento HTML exactly)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Budget Joy",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PrimaryBlue, // #D0BCFF Lavender
                                        fontSize = 24.sp,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Text(
                                    text = greetingText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.testTag("dashboard_greeting_text")
                                )
                                val currentDateFormatted = remember(currentLanguage) {
                                    try {
                                        UiUtils.formatDate(System.currentTimeMillis(), currentLanguage)
                                    } catch (e: Exception) {
                                        ""
                                    }
                                }
                                if (currentDateFormatted.isNotEmpty()) {
                                    Text(
                                        text = currentDateFormatted,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                            }
                            SyncStatusIndicator(onClick = { viewModel.triggerSync() })
                        }

                        // Joy Avatar Profile Bubble
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderColor, CircleShape)
                                .clickable(
                                    onClickLabel = "ouvrir les paramètres du profil"
                                ) {
                                    onNavigateToProfile()
                                }
                                .semantics {
                                    contentDescription = "ouvrir les paramètres du profil"
                                }
                                .testTag("avatar_profile_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Balance Card (Hero section, 32dp container rounded, subtle gradient)
                item {
                    BalanceCard(balance = uiState.availableBalance)
                }

                // First row of Bento Grid: Revenus & Dépenses (2 cards per line limit respected)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoTotalCard(
                            label = t.income,
                            amount = uiState.totalIncome,
                            backgroundColor = ColorIncomeBg,
                            borderColor = ColorIncomeBorder,
                            iconBgColor = ColorIncomeIconBg,
                            textColor = ColorIncome,
                            imageVector = Icons.Default.ArrowUpward,
                            modifier = Modifier.weight(1f)
                        )
                        BentoTotalCard(
                            label = t.expense,
                            amount = uiState.totalExpense,
                            backgroundColor = ColorExpenseBg,
                            borderColor = ColorExpenseBorder,
                            iconBgColor = ColorExpenseIconBg,
                            textColor = ColorExpense,
                            imageVector = Icons.Default.ArrowDownward,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Second row of Bento Grid: Épargne & Progress Metrics (2 cards per line respected)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BentoTotalCard(
                            label = t.saving,
                            amount = uiState.totalSaving,
                            backgroundColor = ColorSavingBg,
                            borderColor = ColorSavingBorder,
                            iconBgColor = ColorSavingIconBg,
                            textColor = ColorSaving,
                            imageVector = Icons.Default.Star,
                            modifier = Modifier.weight(1f)
                        )
                        BentoProgressCard(
                            ratio = savingPercentage,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Bento AI dynamic/fallback Advice Card
                item {
                    BentoAiAdviceCard(viewModel = viewModel)
                }

                // Bento Savings Goal Card
                item {
                    BentoGoalCard(goalsAnalysis = uiState.goalsAnalysis)
                }

                // Recent Operations Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t.labelLatestTransactions,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        if (uiState.recentTransactions.isNotEmpty()) {
                            Text(
                                text = t.labelRecentTop5,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                // Operations items
                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        EmptyStateDashboard(onNavigateToAdd = onNavigateToAdd)
                    }
                } else {
                    items(uiState.recentTransactions, key = { it.id }) { tx ->
                        RecentTransactionItem(transaction = tx)
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, modifier: Modifier = Modifier) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val glowColor = if (balance >= 0) PrimaryBlue else ColorExpense
    var isBalanceVisible by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        radius = 450f
                    )
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        text = t.availableBalance.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextSecondary,
                            letterSpacing = 1.8.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { isBalanceVisible = !isBalanceVisible },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("toggle_balance_visibility")
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isBalanceVisible) "Masquer le solde" else "Afficher le solde",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                val displayText = if (isBalanceVisible) {
                    UiUtils.formatCurrency(balance)
                } else {
                    "•••• ${UiUtils.currentCurrencySymbol}"
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        fontSize = 38.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BentoTotalCard(
    label: String,
    amount: Double,
    backgroundColor: Color,
    borderColor: Color,
    iconBgColor: Color,
    textColor: Color,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(24.dp), // rounded-3xl from HTML design
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Background Box representing Tailwind icon holder
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconBgColor, RoundedCornerShape(12.dp)), // rounded-xl from HTML design
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = UiUtils.formatCurrency(amount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 18.sp
                )
            }
        }
    }
}

/**
 * Custom Bento Progress Card matching the 68% indicator in HTML design
 */
@Composable
fun BentoProgressCard(
    ratio: Int,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    Card(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(24.dp), // rounded-3xl from HTML design
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = t.progressTitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = t.savedRatioText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                )
            }

            // Exquisite custom progress circle matching the HTML mockup exactly
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .weight(0.9f),
                contentAlignment = Alignment.Center
            ) {
                // Background Track Grey circle
                CircularProgressIndicator(
                    progress = { 1.0f },
                    modifier = Modifier.fillMaxSize(),
                    color = BorderColor,
                    strokeWidth = 4.dp
                )

                // Colored progress ring in lavender Accent
                CircularProgressIndicator(
                    progress = { ratio / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = PrimaryBlue,
                    strokeWidth = 4.dp
                )

                Text(
                    text = "$ratio%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RecentTransactionItem(transaction: Transaction, modifier: Modifier = Modifier) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val typeColor = when (transaction.type) {
        TransactionType.INCOME.name -> ColorIncome
        TransactionType.EXPENSE.name -> ColorExpense
        TransactionType.SAVING.name -> ColorSaving
        else -> TextPrimary
    }

    val typeBg = when (transaction.type) {
        TransactionType.INCOME.name -> ColorIncomeBg
        TransactionType.EXPENSE.name -> ColorExpenseBg
        TransactionType.SAVING.name -> ColorSavingBg
        else -> DarkSurfaceVariant
    }

    val typePrefix = when (transaction.type) {
        TransactionType.INCOME.name -> "+"
        TransactionType.EXPENSE.name -> "-"
        TransactionType.SAVING.name -> "⏸ "
        else -> ""
    }

    val icon = when (transaction.type) {
        TransactionType.INCOME.name -> Icons.Default.ArrowUpward
        TransactionType.EXPENSE.name -> Icons.Default.ArrowDownward
        TransactionType.SAVING.name -> Icons.Default.Star
        else -> Icons.Default.Star
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_transaction_item"),
        shape = RoundedCornerShape(16.dp), // rounded-2xl from HTML design
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle matching bento list avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(typeColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = UiUtils.getLocalizedCategory(transaction.displayCategory, t),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "$typePrefix${UiUtils.formatCurrency(transaction.amount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = UiUtils.formatDate(transaction.dateTimestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyStateDashboard(onNavigateToAdd: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💰 Pas encore d'opérations",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Commence à suivre ton budget en cliquant ci-dessous.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted
                ),
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateToAdd,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Ajouter ma première opération",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun BentoAdviceCard(
    analysisResult: BudgetAnalysisResult,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val (bgColor, borderColor, accentColor, icon) = when (analysisResult.adviceColorType) {
        AdviceColorType.INFO -> {
            Row(bgColor = ColorSavingBg, borderColor = ColorSavingBorder, accentColor = ColorSaving, icon = Icons.Default.Info)
        }
        AdviceColorType.SUCCESS -> {
            Row(bgColor = ColorIncomeBg, borderColor = ColorIncomeBorder, accentColor = ColorIncome, icon = Icons.Default.CheckCircle)
        }
        AdviceColorType.WARNING -> {
            Row(bgColor = Color(0xFF2E2620), borderColor = Color(0xFF534135), accentColor = Color(0xFFFFB74D), icon = Icons.Default.Warning)
        }
        AdviceColorType.ERROR -> {
            Row(bgColor = ColorExpenseBg, borderColor = ColorExpenseBorder, accentColor = ColorExpense, icon = Icons.Default.Error)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_advice_card"),
        shape = RoundedCornerShape(24.dp), // Bento rounded style
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Styled visual indicator matching Bento icon style
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Conseil Budget Icon",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = t.aiPersonalAdviceLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = analysisResult.adviceMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = analysisResult.adviceDescription,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                )
            }
        }
    }
}

// Small private helper class to keep tuple matching clean for Bento visual properties
private data class Row(
    val bgColor: Color,
    val borderColor: Color,
    val accentColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BentoGoalCard(
    goalsAnalysis: List<GoalAnalysis>,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val mainGoalAnalysis = goalsAnalysis.firstOrNull { it.remainingAmount > 0.0 } ?: goalsAnalysis.firstOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bento_goal_card"),
        shape = RoundedCornerShape(24.dp), // Bento rounded style
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        if (mainGoalAnalysis == null) {
            // Empty State
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(ColorSaving.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "No Goal Icon",
                        tint = ColorSaving,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = t.labelObjective.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ColorSaving,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Finance un projet ! 🎯",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Définis un objectif dans l'onglet Analyses pour y affecter tes économies.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }
        } else {
            // Has Goal
            val g = mainGoalAnalysis.goal
            val progressPercentage = mainGoalAnalysis.progressPercentage.toInt()
            val animatedPercent by animateFloatAsState(
                targetValue = progressPercentage / 100f,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
                label = "GoalProgressAnimation"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = t.labelObjective.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ColorSaving,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = g.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${UiUtils.formatCurrency(mainGoalAnalysis.remainingAmount)} ${t.labelRemaining}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    if (mainGoalAnalysis.remainingAmount > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mainGoalAnalysis.projectionMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = ColorSaving,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    mainGoalAnalysis.insightMessage?.let { insight ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Progression circulaire
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1.0f },
                        modifier = Modifier.fillMaxSize(),
                        color = BorderColor,
                        strokeWidth = 5.dp
                    )

                    CircularProgressIndicator(
                        progress = { animatedPercent },
                        modifier = Modifier.fillMaxSize(),
                        color = ColorSaving,
                        strokeWidth = 5.dp
                    )

                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = TextWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun BentoAiAdviceCard(
    viewModel: com.example.presentation.viewmodel.BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val history by viewModel.adviceHistory.collectAsStateWithLifecycle()
    var showHistory by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bento_ai_advice_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // First Row: Icon, Title & Refresh Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology, // Brain / Psychology symbol
                            contentDescription = "AI Advice Icon",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = t.aiPersonalAdviceLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 10.sp
                                )
                            )
                            
                            val isDemo = (aiState as? AiUiState.Success)?.result?.isDemo ?: true
                            val badgeText = if (isDemo) "Local ⚡" else "Gemini 🤖"
                            val badgeColor = if (isDemo) ColorSaving else PrimaryBlue
                            
                            Box(
                                modifier = Modifier
                                    .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = badgeColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Joy AI Coach ✨",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.generateAiAdvice() },
                    enabled = aiState !is AiUiState.Loading,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = TextSecondary)
                ) {
                    if (aiState is AiUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = PrimaryBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rafraîchir",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body content
            when (val state = aiState) {
                is AiUiState.Idle -> {
                    Text(
                        text = "Calcul de votre stratégie budgétaire en cours...",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 13.sp)
                    )
                }
                is AiUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = PrimaryBlue,
                            trackColor = BorderColor
                        )
                        Text(
                            text = "Analyse des transactions et des objectifs en cours...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
                is AiUiState.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = state.result.summary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhite,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )

                        state.result.adviceList.forEach { advice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkBackground, RoundedCornerShape(12.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "💡",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = advice,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is AiUiState.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            tint = ColorExpense,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall.copy(color = ColorExpense, fontSize = 12.sp)
                        )
                    }
                }
            }

            // Session history expandable section
            if (history.size > 1) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHistory = !showHistory }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Historique de session (${history.size})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Text(
                        text = if (showHistory) "Masquer" else "Afficher",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }

                if (showHistory) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        history.drop(1).forEachIndexed { idx, prevResult ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkBackground.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Conseils précédents #${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                )
                                Text(
                                    text = prevResult.summary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyncStatusIndicator(
    onClick: (() -> Unit)? = null
) {
    val syncState by SyncManager.syncState.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = syncState != SyncState.IDLE,
        enter = fadeIn(animationSpec = tween(300)) + expandHorizontally(),
        exit = fadeOut(animationSpec = tween(300)) + shrinkHorizontally()
    ) {
        val text = when (syncState) {
            SyncState.SYNCED -> "Synchronisé"
            SyncState.OFFLINE -> "Hors ligne"
            SyncState.SYNCING -> "Synchronisation..."
            SyncState.ERROR -> "Erreur sync"
            SyncState.IDLE -> ""
        }

        val color = when (syncState) {
            SyncState.SYNCED -> Color(0xFF4CAF50) // Green
            SyncState.OFFLINE -> Color(0xFFFF9800)      // Soft Orange
            SyncState.SYNCING -> Color(0xFF9C27B0)      // Violet/Bleu theme
            SyncState.ERROR -> Color(0xFFE57373)        // Soft Red
            SyncState.IDLE -> Color.Transparent
        }

        val icon = when (syncState) {
            SyncState.SYNCED -> Icons.Default.CheckCircle
            SyncState.OFFLINE -> Icons.Default.Info
            SyncState.SYNCING -> Icons.Default.Refresh
            SyncState.ERROR -> Icons.Default.Warning
            SyncState.IDLE -> Icons.Default.CheckCircle
        }

        val rotationModifier = if (syncState == SyncState.SYNCING) {
            val infiniteTransition = rememberInfiniteTransition(label = "rotation")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing)
                ),
                label = "rotationAngle"
            )
            Modifier.rotate(angle)
        } else {
            Modifier
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
            modifier = if (onClick != null) {
                Modifier
                    .padding(start = 4.dp)
                    .clickable { onClick() }
            } else {
                Modifier.padding(start = 4.dp)
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = color,
                    modifier = Modifier
                        .size(12.dp)
                        .then(rotationModifier)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

