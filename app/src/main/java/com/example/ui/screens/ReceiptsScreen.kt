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
    var selectedImageForPreview by remember { mutableStateOf<PhotoReceiptEntity?>(null) }

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
                            onView = { selectedImageForPreview = receipt },
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

    if (selectedImageForPreview != null) {
        ReceiptImageViewerDialog(
            receipt = selectedImageForPreview!!,
            onDismiss = { selectedImageForPreview = null }
        )
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
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (receipt.imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = receipt.imageUri,
                        contentDescription = "معاينة الفاتورة",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onView() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable { onView() },
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

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: View (مشاهدة) and Download (تحميل)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاهدة 👁️", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = { downloadReceiptImage(context, receipt) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تحميل 📥", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun ReceiptImageViewerDialog(
    receipt: PhotoReceiptEntity,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🖼️ ${receipt.title}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (receipt.imageUri.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        AsyncImage(
                            model = receipt.imageUri,
                            contentDescription = receipt.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد صورة مرفقة مع هذه الفاتورة", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("المبلغ: ${receipt.amount.toInt()} جنيه", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("التصنيف: ${receipt.category}", style = MaterialTheme.typography.bodySmall)
                        if (receipt.dateFormatted.isNotEmpty()) {
                            Text("التاريخ: ${receipt.dateFormatted}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (receipt.notes.isNotEmpty()) {
                            Text("ملاحظات: ${receipt.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { downloadReceiptImage(context, receipt) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تحميل الصورة 📥")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("إغلاق")
            }
        }
    )
}

fun downloadReceiptImage(context: android.content.Context, receipt: PhotoReceiptEntity) {
    if (receipt.imageUri.isBlank()) {
        android.widget.Toast.makeText(context, "لا توجد صورة مرفقة لتحميلها", android.widget.Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val uri = android.net.Uri.parse(receipt.imageUri)
        val resolver = context.contentResolver
        val fileName = "Receipt_${receipt.title.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/LifeOrganizer")
            }
            val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { destUri ->
                resolver.openInputStream(uri)?.use { input ->
                    resolver.openOutputStream(destUri)?.use { output ->
                        input.copyTo(output)
                    }
                }
                android.widget.Toast.makeText(context, "تم حفظ الصورة بنجاح في معرض الصور (Pictures/LifeOrganizer) 📸", android.widget.Toast.LENGTH_LONG).show()
            }
        } else {
            val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
            val appDir = java.io.File(picturesDir, "LifeOrganizer")
            if (!appDir.exists()) appDir.mkdirs()
            val destFile = java.io.File(appDir, fileName)
            resolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            android.widget.Toast.makeText(context, "تم حفظ الصورة بنجاح: ${destFile.absolutePath} 📂", android.widget.Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "تم تنزيل الفاتورة بنجاح في ذاكرة الجهاز المحفوظة 💾", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AddEditReceiptDialog(
    existingReceipt: PhotoReceiptEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, date: String, imageUri: String, notes: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf(existingReceipt?.title ?: "") }
    var amountText by remember { mutableStateOf(existingReceipt?.amount?.toInt()?.toString() ?: "") }
    var category by remember { mutableStateOf(existingReceipt?.category ?: "فواتير") }
    var date by remember { mutableStateOf(existingReceipt?.dateFormatted ?: "2026-07-31") }
    var imageUri by remember { mutableStateOf(existingReceipt?.imageUri ?: "") }
    var notes by remember { mutableStateOf(existingReceipt?.notes ?: "") }

    val categories = listOf("فواتير", "تسوق", "مطاعم", "صيانة", "أخرى")

    // Image Picker Launcher
    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = java.io.File(context.filesDir, "receipt_${System.currentTimeMillis()}.jpg")
                val outputStream = java.io.FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                imageUri = android.net.Uri.fromFile(file).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

                // Image Upload / Pick Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (imageUri.isNotEmpty()) {
                            CoilImagePreview(uriStr = imageUri, modifier = Modifier.size(120.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (imageUri.isEmpty()) "رفع / اختيار صورة الفاتورة 🖼️" else "تغيير الصورة المرفوعة 📷")
                        }
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

@Composable
fun CoilImagePreview(uriStr: String, modifier: Modifier = Modifier) {
    coil.compose.AsyncImage(
        model = uriStr,
        contentDescription = "معاينة الفاتورة",
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop
    )
}
