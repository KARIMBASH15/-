package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.entities.ImportantLinkEntity
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinksScreen(viewModel: MainViewModel) {
    val links by viewModel.links.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var linkToEdit by remember { mutableStateOf<ImportantLinkEntity?>(null) }

    val filteredLinks = links.filter { link ->
        link.title.contains(searchQuery, ignoreCase = true) ||
                link.url.contains(searchQuery, ignoreCase = true) ||
                link.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    linkToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_link_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة رابط")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("links_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🔗 الروابط والمواقع المهمة",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("بحث في الروابط والمواقع...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredLinks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد روابط محفوظة.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredLinks, key = { it.id }) { link ->
                        LinkCardItem(
                            link = link,
                            onOpenUrl = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (link.url.startsWith("http")) link.url else "https://${link.url}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyUrl = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Link", link.url)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الرابط للتقويم 📋", Toast.LENGTH_SHORT).show()
                            },
                            onToggleFavorite = {
                                viewModel.updateLink(link.copy(isFavorite = !link.isFavorite))
                            },
                            onEdit = {
                                linkToEdit = link
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteLink(link) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditLinkDialog(
            existingLink = linkToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { title, url, category, desc, isFav ->
                if (linkToEdit == null) {
                    viewModel.addLink(
                        ImportantLinkEntity(
                            title = title,
                            url = url,
                            category = category,
                            description = desc,
                            isFavorite = isFav
                        )
                    )
                } else {
                    viewModel.updateLink(
                        linkToEdit!!.copy(
                            title = title,
                            url = url,
                            category = category,
                            description = desc,
                            isFavorite = isFav
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun LinkCardItem(
    link: ImportantLinkEntity,
    onOpenUrl: () -> Unit,
    onCopyUrl: () -> Unit,
    onToggleFavorite: () -> Unit,
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (link.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "مفضل",
                    tint = if (link.isFavorite) Color(0xFFF59E0B) else Color.Gray
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = link.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (link.description.isNotEmpty()) {
                    Text(
                        text = link.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            Row {
                IconButton(onClick = onCopyUrl) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = Color.Gray)
                }
                IconButton(onClick = onOpenUrl) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "فتح", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun AddEditLinkDialog(
    existingLink: ImportantLinkEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, url: String, category: String, description: String, isFavorite: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(existingLink?.title ?: "") }
    var url by remember { mutableStateOf(existingLink?.url ?: "") }
    var category by remember { mutableStateOf(existingLink?.category ?: "عام") }
    var description by remember { mutableStateOf(existingLink?.description ?: "") }
    var isFavorite by remember { mutableStateOf(existingLink?.isFavorite ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingLink == null) "حفظ رابط جديد" else "تعديل الرابط") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الموقع أو الصفحة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط الموقع (URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف (خدمات، تعليم، تسوق...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف مختصر") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFavorite, onCheckedChange = { isFavorite = it })
                    Text("إضافة للمفضلة ⭐")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onSave(title, url, category, description, isFavorite)
                    }
                },
                enabled = title.isNotBlank() && url.isNotBlank()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
