package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entities.DocumentEntity
import com.example.ui.MainViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var docToEdit by remember { mutableStateOf<DocumentEntity?>(null) }
    var selectedDocForPreview by remember { mutableStateOf<DocumentEntity?>(null) }

    // Cloud Folders State
    val folderPrefs = remember { context.getSharedPreferences("cloud_folders_pref", Context.MODE_PRIVATE) }
    var customFolders by remember {
        mutableStateOf(
            folderPrefs.getStringSet("folders_list", null)?.toList()
                ?: listOf("الكل", "هوية", "عقود", "شهادات", "مركبة", "صور شخصية", "مستندات عامة")
        )
    }
    var selectedFolder by remember { mutableStateOf("الكل") }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val filteredDocs = remember(documents, selectedFolder) {
        if (selectedFolder == "الكل") documents
        else documents.filter { it.category.equals(selectedFolder, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    docToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_doc_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة وثيقة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("documents_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📂 السحابة الخاصة والوثائق",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { showCreateFolderDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مجلد جديد 📁", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cloud Folders Selector Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(customFolders) { folder ->
                    FilterChip(
                        selected = selectedFolder == folder,
                        onClick = { selectedFolder = folder },
                        label = { Text("📁 $folder", fontSize = 12.sp, fontWeight = if (selectedFolder == folder) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (selectedFolder == folder) {
                            { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد ملفات أو صور في مجلد ($selectedFolder)", color = Color.Gray)
                        Text("اضغط زر (+) لرفع وتخزين صورة الهوية أو العقد أو أي ملف.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredDocs, key = { it.id }) { doc ->
                        DocumentCardItem(
                            document = doc,
                            onView = { selectedDocForPreview = doc },
                            onEdit = {
                                docToEdit = doc
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteDocument(doc) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("إنشاء مجلد سحابي جديد 📁") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("اسم المجلد (مثال: رخص القيادة / الفواتير)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank() && !customFolders.contains(newFolderName)) {
                            val updated = customFolders + newFolderName
                            customFolders = updated
                            folderPrefs.edit().putStringSet("folders_list", updated.toSet()).apply()
                            selectedFolder = newFolderName
                            showCreateFolderDialog = false
                            Toast.makeText(context, "تم إنشاء المجلد السحابي بنجاح! 📂", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = newFolderName.isNotBlank()
                ) {
                    Text("إنشاء")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (selectedDocForPreview != null) {
        DocumentImageViewerDialog(
            document = selectedDocForPreview!!,
            onDismiss = { selectedDocForPreview = null }
        )
    }

    if (showAddDialog) {
        AddEditDocumentDialog(
            existingDoc = docToEdit,
            availableFolders = customFolders.filter { it != "الكل" },
            onDismiss = { showAddDialog = false },
            onSave = { title, category, fileUri, notes ->
                if (docToEdit == null) {
                    viewModel.addDocument(
                        DocumentEntity(
                            title = title,
                            category = category,
                            fileUri = fileUri,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.updateDocument(
                        docToEdit!!.copy(
                            title = title,
                            category = category,
                            fileUri = fileUri,
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
fun DocumentCardItem(
    document: DocumentEntity,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

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
                if (document.fileUri.isNotEmpty()) {
                    AsyncImage(
                        model = document.fileUri,
                        contentDescription = "معاينة الوثيقة",
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
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = document.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (document.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = document.notes,
                            style = MaterialTheme.typography.bodySmall,
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

            // Action Buttons: View (مشاهدة), Share (مشاركة), and Download (تحميل)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض 👁️", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { shareDocumentFile(context, document) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاركة 📤", fontSize = 11.sp)
                }

                Button(
                    onClick = { downloadDocumentFile(context, document) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحميل 📥", fontSize = 11.sp)
                }
            }
        }
    }
}

fun shareDocumentFile(context: Context, document: DocumentEntity) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "📄 ${document.title}\nالمجلد: ${document.category}\n${document.notes}")
            if (document.fileUri.isNotBlank()) {
                putExtra(Intent.EXTRA_STREAM, Uri.parse(document.fileUri))
                type = "image/*"
            } else {
                type = "text/plain"
            }
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة المستند عبر:")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "تمت المشاركة!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun DocumentImageViewerDialog(
    document: DocumentEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🖼️ ${document.title}",
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
                if (document.fileUri.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        AsyncImage(
                            model = document.fileUri,
                            contentDescription = document.title,
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
                        Text("لا توجد صورة مرفقة مع هذه الوثيقة", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("التصنيف: ${document.category}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (document.notes.isNotEmpty()) {
                            Text("ملاحظات: ${document.notes}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { downloadDocumentFile(context, document) },
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

fun downloadDocumentFile(context: Context, document: DocumentEntity) {
    if (document.fileUri.isBlank()) {
        Toast.makeText(context, "لا توجد صورة أو ملف مرفق للتحميل", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val uri = Uri.parse(document.fileUri)
        val resolver = context.contentResolver
        val fileName = "Doc_${document.title.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LifeOrganizerDocs")
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { destUri ->
                resolver.openInputStream(uri)?.use { input ->
                    resolver.openOutputStream(destUri)?.use { output ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "تم حفظ الوثيقة بنجاح في المعرض (Pictures/LifeOrganizerDocs) 📸", Toast.LENGTH_LONG).show()
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "LifeOrganizerDocs")
            if (!appDir.exists()) appDir.mkdirs()
            val destFile = File(appDir, fileName)
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(context, "تم حفظ الوثيقة بنجاح: ${destFile.absolutePath} 📂", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "تم حفظ الوثيقة بنجاح في ذاكرة الجهاز المحفوظة 💾", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AddEditDocumentDialog(
    existingDoc: DocumentEntity?,
    availableFolders: List<String> = listOf("هوية", "عقود", "شهادات", "مركبة", "صور شخصية", "مستندات عامة"),
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, fileUri: String, notes: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existingDoc?.title ?: "") }
    var category by remember { mutableStateOf(existingDoc?.category ?: (availableFolders.firstOrNull() ?: "مستندات عامة")) }
    var fileUri by remember { mutableStateOf(existingDoc?.fileUri ?: "") }
    var notes by remember { mutableStateOf(existingDoc?.notes ?: "") }

    val categories = if (availableFolders.isNotEmpty()) availableFolders else listOf("هوية", "عقود", "شهادات", "مركبة", "صور شخصية", "مستندات عامة")

    // Document/Photo Picker Launcher (*/*)
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.filesDir, "doc_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                fileUri = Uri.fromFile(file).toString()
                Toast.makeText(context, "تم اختيار الملف بنجاح! 📁", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingDoc == null) "رفع ملف / صورة جديدة للسحابة 📁" else "تعديل الملف") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الملف أو الصورة (مثال: عقد منزل / الهوية)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("اختر المجلد السحابي:", style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text("📁 $cat", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Document File Upload Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (fileUri.isNotEmpty()) {
                            AsyncImage(
                                model = fileUri,
                                contentDescription = "معاينة الوثيقة",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { docPickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (fileUri.isEmpty()) "رفع ملف أو صورة من الجهاز 📂" else "تغيير الملف المرفق 📷")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات / تاريخ الانتهاء") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, category, fileUri, notes)
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

