package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun SecurityLockScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var isLockEnabled by remember { mutableStateOf(viewModel.securityLockManager.isLockEnabled()) }
    var pinCode by remember { mutableStateOf(viewModel.securityLockManager.getPinCode()) }
    var isBiometrics by remember { mutableStateOf(viewModel.securityLockManager.isBiometricsEnabled()) }

    var newPinInput by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("security_screen")
    ) {
        Text(
            text = "🔒 قفل التطبيق والحماية والأمان",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
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
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تفعيل قفل التطبيق", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("طلب رمز PIN عند فتح التطبيق", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Switch(
                        checked = isLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && pinCode.isEmpty()) {
                                showPinDialog = true
                            } else {
                                isLockEnabled = enabled
                                viewModel.updateSecuritySettings(enabled, pinCode)
                            }
                        }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تفعيل البصمة / الوجه", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("السماح بالفتح عبر بصمة الإصبع", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Switch(
                        checked = isBiometrics,
                        onCheckedChange = {
                            isBiometrics = it
                            viewModel.securityLockManager.setBiometricsEnabled(it)
                        },
                        enabled = isLockEnabled
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Button(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Password, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (pinCode.isEmpty()) "تعيين رمز PIN جديد" else "تغيير رمز PIN الحالى")
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تعيين رمز PIN المكون من 4 أرقام") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 4) newPinInput = it },
                        label = { Text("رمز PIN (4 أرقام)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            pinCode = newPinInput
                            isLockEnabled = true
                            viewModel.updateSecuritySettings(true, newPinInput)
                            Toast.makeText(context, "تم حفظ الرمز وتفعيل القفل! 🔒", Toast.LENGTH_SHORT).show()
                            showPinDialog = false
                            newPinInput = ""
                        } else {
                            Toast.makeText(context, "الرمز يجب أن يتكون من 4 أرقام", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun AppLockOverlayScreen(
    onUnlockSubmit: (String) -> Boolean
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("app_lock_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "منظم حياتي مقفل",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "أدخل رمز PIN المكون من 4 أرقام للمتابعة",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "رمز القفل المعتمد لحماية وتشفير بياناتك",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Display
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f))
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Keypad Grid
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "OK")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                errorMessage = ""
                                when (key) {
                                    "C" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    "OK" -> {
                                        if (enteredPin.length == 4) {
                                            val success = onUnlockSubmit(enteredPin)
                                            if (!success) {
                                                errorMessage = "رمز PIN غير صحيح. حاول مجدداً."
                                                enteredPin = ""
                                            }
                                        }
                                    }
                                    else -> {
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                val success = onUnlockSubmit(enteredPin)
                                                if (!success) {
                                                    errorMessage = "رمز PIN غير صحيح. حاول مجدداً."
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (key == "OK") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (key == "OK") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
