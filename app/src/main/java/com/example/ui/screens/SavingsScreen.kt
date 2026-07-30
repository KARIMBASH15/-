package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entities.SavingsVaultEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.MintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(viewModel: MainViewModel) {
    val vaults by viewModel.savingsVaults.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var vaultToDeposit by remember { mutableStateOf<SavingsVaultEntity?>(null) }
    var vaultToEdit by remember { mutableStateOf<SavingsVaultEntity?>(null) }

    val totalSaved = vaults.sumOf { it.currentAmount }
    val totalTarget = vaults.sumOf { it.targetAmount }.coerceAtLeast(1.0)
    val overallProgress = (totalSaved / totalTarget).coerceIn(0.0, 1.0).toFloat()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    vaultToEdit = null
                    showAddDialog = true
                },
                containerColor = AccentGold,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_savings_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إنشاء صندوق تحويش")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("savings_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🏦 صندوق التحويش والادخار",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Total Savings Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي المبلغ المدخر",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${totalSaved.toInt()} ج.م",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}% تحققت",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = AccentGold,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الهدف المالي الكلي: ${totalTarget.toInt()} جنيه مصري",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (vaults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد صناديق تحويش حالياً.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(vaults, key = { it.id }) { vault ->
                        SavingsVaultCardItem(
                            vault = vault,
                            onDepositClick = { vaultToDeposit = vault },
                            onEdit = {
                                vaultToEdit = vault
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteSavingsVault(vault) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditVaultDialog(
            existingVault = vaultToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { title, targetAmt, category, deadline ->
                if (vaultToEdit == null) {
                    viewModel.addSavingsVault(
                        SavingsVaultEntity(
                            title = title,
                            targetAmount = targetAmt,
                            category = category,
                            deadline = deadline
                        )
                    )
                } else {
                    viewModel.updateSavingsVault(
                        vaultToEdit!!.copy(
                            title = title,
                            targetAmount = targetAmt,
                            category = category,
                            deadline = deadline
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }

    if (vaultToDeposit != null) {
        DepositDialog(
            vault = vaultToDeposit!!,
            onDismiss = { vaultToDeposit = null },
            onConfirmDeposit = { amount, note ->
                viewModel.depositToVault(vaultToDeposit!!, amount, note)
                vaultToDeposit = null
            }
        )
    }
}

@Composable
fun SavingsVaultCardItem(
    vault: SavingsVaultEntity,
    onDepositClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = (vault.currentAmount / vault.targetAmount.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = AccentGold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = vault.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = vault.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                Row {
                    Button(
                        onClick = onDepositClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ إيداع")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تم جمع: ${vault.currentAmount.toInt()} ج.م",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "من أصل: ${vault.targetAmount.toInt()} ج.م",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AccentGold,
                trackColor = Color.LightGray.copy(alpha = 0.4f)
            )

            if (vault.deadline.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "الموعد المستهدف: ${vault.deadline}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AddEditVaultDialog(
    existingVault: SavingsVaultEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Double, category: String, deadline: String) -> Unit
) {
    var title by remember { mutableStateOf(existingVault?.title ?: "") }
    var targetText by remember { mutableStateOf(existingVault?.targetAmount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(existingVault?.category ?: "ادخار عام") }
    var deadline by remember { mutableStateOf(existingVault?.deadline ?: "2026-12-31") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingVault == null) "إنشاء صندوق تحويش جديد" else "تعديل صندوق التحويش") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الصندوق (مثال: رحلة الصيف)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("المبلغ المستهدف (بالجنيه)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف (سفر، طوارئ، مشتريات...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("تاريخ الوصول للهدف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = targetText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onSave(title, amt, category, deadline)
                    }
                },
                enabled = title.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun DepositDialog(
    vault: SavingsVaultEntity,
    onDismiss: () -> Unit,
    onConfirmDeposit: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("إيداع جديد") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إيداع مبلغ في [${vault.title}]") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ المضاف (بالجنيه)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("ملاحظة الإيداع") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirmDeposit(amt, note)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("تأكيد الإيداع")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
