// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.Categories
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.presentation.viewmodel.BudgetUiState
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HistoryTab(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.filterType.collectAsState()

    // Deletion confirmation state
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    if (transactionToEdit != null) {
        TransactionEditDialog(
            transaction = transactionToEdit!!,
            viewModel = viewModel,
            onDismiss = { transactionToEdit = null },
            onSaved = { transactionToEdit = null }
        )
    }

    if (transactionToDelete != null) {
        val formattedMsg = t.deleteTxPromptConfirm.format(
            transactionToDelete?.label ?: "",
            UiUtils.formatCurrency(transactionToDelete?.amount ?: 0.0)
        )
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = t.deleteTxPromptTitle,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = formattedMsg,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorExpense)
                ) {
                    Text(t.confirmDeleteBtn, color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(t.cancelBtn, color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tab Title
        Text(
            text = t.historyTitle,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.padding(top = 16.dp)
        )

        // Search Bar Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            placeholder = { Text(t.searchPlaceholder, color = TextMuted) },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = t.cancelBtn,
                            tint = TextSecondary
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = DarkSurfaceVariant,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Filter chips Row (FlowRow is perfect for wrapping beautifully to avoid cropped texts!)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = t.filterAll,
                isSelected = selectedFilter == "TOUTES",
                onClick = { viewModel.onFilterTypeChange("TOUTES") }
            )
            FilterChipItem(
                label = t.filterIncome,
                isSelected = selectedFilter == "INCOME",
                onClick = { viewModel.onFilterTypeChange("INCOME") }
            )
            FilterChipItem(
                label = t.filterExpense,
                isSelected = selectedFilter == "EXPENSE",
                onClick = { viewModel.onFilterTypeChange("EXPENSE") }
            )
            FilterChipItem(
                label = t.filterSaving,
                isSelected = selectedFilter == "SAVING",
                onClick = { viewModel.onFilterTypeChange("SAVING") }
            )
        }

        // Transactions List representation
        if (transactions.isEmpty()) {
            EmptyStateHistory(
                searchQuery = searchQuery,
                selectedFilter = selectedFilter,
                onResetFilters = {
                    viewModel.onSearchQueryChange("")
                    viewModel.onFilterTypeChange("TOUTES")
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("history_list"),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    HistoryTransactionItem(
                        transaction = tx,
                        onDeleteClick = { transactionToDelete = tx },
                        onClick = { transactionToEdit = tx }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else DarkSurface
    val border = if (isSelected) PrimaryBlue else BorderColor
    val textColor = if (isSelected) PrimaryBlue else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable { onClick() }
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("filter_chip_$label"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        )
    }
}

@Composable
fun HistoryTransactionItem(
    transaction: Transaction,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val typeColor = when (transaction.type) {
        TransactionType.INCOME.name -> ColorIncome
        TransactionType.EXPENSE.name -> ColorExpense
        TransactionType.SAVING.name -> ColorSaving
        else -> TextPrimary
    }

    val prefix = when (transaction.type) {
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
            .clickable { onClick() }
            .testTag("history_transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
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
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(typeColor.copy(alpha = 0.15f), CircleShape),
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$prefix${UiUtils.formatCurrency(transaction.amount)}",
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
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = t.delete,
                        tint = ColorExpense.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateHistory(
    searchQuery: String,
    selectedFilter: String,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = t.emptySearchMessage,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (searchQuery.isNotBlank() || selectedFilter != "TOUTES") {
                    t.emptySearchDesc
                } else {
                    t.emptyHistoryDesc
                },
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (searchQuery.isNotBlank() || selectedFilter != "TOUTES") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = t.resetFiltersBtn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditDialog(
    transaction: Transaction,
    viewModel: BudgetViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val context = LocalContext.current
    var label by remember { mutableStateOf(transaction.label) }
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var selectedType by remember { mutableStateOf(TransactionType.valueOf(transaction.type)) }

    val categoryList = when (selectedType) {
        TransactionType.INCOME -> Categories.incomeCategories
        TransactionType.EXPENSE -> Categories.expenseCategories
        TransactionType.SAVING -> Categories.savingCategories
    }

    val initialCategory = if (categoryList.contains(transaction.category)) {
        transaction.category
    } else if (categoryList.contains("Autre")) {
        "Autre"
    } else {
        categoryList.firstOrNull() ?: ""
    }

    var selectedCategory by remember(selectedType) { mutableStateOf(initialCategory) }
    var customCategoryText by remember { mutableStateOf(transaction.customCategory ?: "") }
    var selectedDateTimestamp by remember { mutableStateOf(transaction.dateTimestamp) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val goals = (uiState as? BudgetUiState.Success)?.goals ?: emptyList()
    var selectedGoalId by remember { mutableStateOf(transaction.associatedGoalId) }
    var goalDropdownExpanded by remember { mutableStateOf(false) }

    val debts = (uiState as? BudgetUiState.Success)?.debts ?: emptyList()
    var selectedDebtId by remember { mutableStateOf(transaction.associatedDebtId) }
    var debtDropdownExpanded by remember { mutableStateOf(false) }

    if (showDatePicker) {
        BudgetDatePickerDialog(
            initialDateTimestamp = selectedDateTimestamp,
            onDateSelected = {
                selectedDateTimestamp = it
                showDatePicker = false
            },
            onDismissRequest = { showDatePicker = false },
            language = viewModel.currentLanguage.value
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = t.editTxDialogTitle,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.values().forEach { type ->
                        val isSelected = selectedType == type
                        val typeColor = when (type) {
                            TransactionType.INCOME -> ColorIncome
                            TransactionType.EXPENSE -> ColorExpense
                            TransactionType.SAVING -> ColorSaving
                        }
                        val background = if (isSelected) typeColor.copy(alpha = 0.15f) else DarkBackground
                        val border = if (isSelected) typeColor else BorderColor

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(background)
                                .border(1.dp, border, RoundedCornerShape(12.dp))
                                .clickable { selectedType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (type) {
                                    TransactionType.INCOME -> t.txTypeIncome
                                    TransactionType.EXPENSE -> t.txTypeExpense
                                    TransactionType.SAVING -> t.txTypeSaving
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) typeColor else TextSecondary
                                )
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(t.descLabel) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_tx_label_input")
                )

                // Montant
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("${t.valAmountLabel} (${UiUtils.currentCurrencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_tx_amount_input")
                )

                // Date selection action trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkBackground)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Text(t.txDateLabel, color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = UiUtils.formatShortDate(selectedDateTimestamp, viewModel.currentLanguage.value),
                        color = PrimaryBlue,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Category selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = UiUtils.getLocalizedCategory(selectedCategory, t),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(t.categoryLabel) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor
                        ),
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextWhite)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(DarkSurface).border(1.dp, BorderColor)
                    ) {
                        categoryList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(UiUtils.getLocalizedCategory(cat, t), color = TextWhite) },
                                onClick = {
                                    selectedCategory = cat
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (Categories.isCustomCategory(selectedCategory)) {
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = { customCategoryText = it },
                        label = { Text(t.customCatInputPrompt) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_tx_custom_category_input")
                    )
                }

                // Link to goals if SAVING type
                if (selectedType == TransactionType.SAVING && goals.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedGoal = goals.find { it.id == selectedGoalId }
                        val selectedGoalName = selectedGoal?.name ?: t.goalLinkNone

                        OutlinedTextField(
                            value = selectedGoalName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(t.goalLinkPrompt) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor
                            ),
                            trailingIcon = {
                                IconButton(onClick = { goalDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextWhite)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { goalDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = goalDropdownExpanded,
                            onDismissRequest = { goalDropdownExpanded = false },
                            modifier = Modifier.background(DarkSurface).border(1.dp, BorderColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text(t.goalLinkNone, color = TextWhite) },
                                onClick = {
                                    selectedGoalId = null
                                    goalDropdownExpanded = false
                                }
                            )
                            goals.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name, color = TextWhite) },
                                    onClick = {
                                        selectedGoalId = g.id
                                        goalDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Link to debts if EXPENSE type
                if (selectedType == TransactionType.EXPENSE && debts.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedDebt = debts.find { it.id == selectedDebtId }
                        val selectedDebtName = selectedDebt?.name ?: t.debtLinkNone

                        OutlinedTextField(
                            value = selectedDebtName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(t.debtLinkPrompt) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor
                            ),
                            trailingIcon = {
                                IconButton(onClick = { debtDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextWhite)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { debtDropdownExpanded = true }
                        )

                        DropdownMenu(
                            expanded = debtDropdownExpanded,
                            onDismissRequest = { debtDropdownExpanded = false },
                            modifier = Modifier.background(DarkSurface).border(1.dp, BorderColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text(t.debtLinkNone, color = TextWhite) },
                                onClick = {
                                    selectedDebtId = null
                                    debtDropdownExpanded = false
                                }
                            )
                            debts.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.name, color = TextWhite) },
                                    onClick = {
                                        selectedDebtId = d.id
                                        debtDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (label.isNotBlank() && amt != null && amt > 0.0) {
                        viewModel.updateTransaction(
                            id = transaction.id,
                            type = selectedType,
                            category = selectedCategory,
                            customCategory = if (Categories.isCustomCategory(selectedCategory)) customCategoryText.trim() else null,
                            label = label.trim(),
                            amount = amt,
                            dateTimestamp = selectedDateTimestamp,
                            associatedGoalId = selectedGoalId,
                            associatedDebtId = selectedDebtId
                        )
                        Toast.makeText(context, t.toastTxUpdated, Toast.LENGTH_SHORT).show()
                        onSaved()
                    } else {
                        Toast.makeText(context, t.toastFormInvalid, Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(t.saveBtn, color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(t.cancelBtn, color = TextSecondary)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
