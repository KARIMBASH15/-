package com.example.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.entities.PhotoReceiptEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var receiptToEdit by remember { mutableStateOf<PhotoReceiptEntity?>(null) }

    val totalSpent = receipts.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    receiptToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_receipt_fab")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "حفظ فاتورة جديدة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("receipts_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📷 حفظ الصور والفواتير",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Spending Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي قيمة الفواتير الموثقة", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${totalSpent.toInt()} ج.م",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${receipts.size} فاتورة",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (receipts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد فواتير أو صور موثقة.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(receipts, key = { it.id }) { receipt ->
                        ReceiptCardItem(
                            receipt = receipt,
                            onEdit = {
                                receiptToEdit = receipt
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteReceipt(receipt) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditReceiptDialog(
            existingReceipt = receiptToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { title, amt, category, date, imageUri, notes ->
                if (receiptToEdit == null) {
                    viewModel.addReceipt(
                        PhotoReceiptEntity(
                            title = title,
                            amount = amt,
                            category = category,
                            dateFormatted = date,
                            imageUri = imageUri,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.updateReceipt(
                        receiptToEdit!!.copy(
                            title = title,
                            amount = amt,
                            category = category,
                            dateFormatted = date,
                            imageUri = imageUri,
                            notes = notes
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReceiptCardItem(
    receipt: PhotoReceiptEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (receipt.imageUri.isNotEmpty()) {
                AsyncImage(
                    model = receipt.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${receipt.amount.toInt()} جنيه | ${receipt.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (receipt.dateFormatted.isNotEmpty()) {
                    Text(
                        text = "التاريخ: ${receipt.dateFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AddEditReceiptDialog(
    existingReceipt: PhotoReceiptEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, date: String, imageUri: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf(existingReceipt?.title ?: "") }
    var amountText by remember { mutableStateOf(existingReceipt?.amount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(existingReceipt?.category ?: "فواتير") }
    var date by remember { mutableStateOf(existingReceipt?.dateFormatted ?: "2026-07-29") }
    var imageUri by remember { mutableStateOf(existingReceipt?.imageUri ?: "") }
    var notes by remember { mutableStateOf(existingReceipt?.notes ?: "") }

    val categories = listOf("فواتير", "تسوق", "مطاعم", "صيانة", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingReceipt == null) "إضافة فاتورة جديدة" else "تعديل الفاتورة") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الفاتورة أو الشراء") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ (بالجنيه)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("التصنيف:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("التاريخ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = imageUri,
                    onValueChange = { imageUri = it },
                    label = { Text("رابط/مسار الصورة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank()) {
                        onSave(title, amt, category, date, imageUri, notes)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
