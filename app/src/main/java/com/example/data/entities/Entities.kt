package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "عام",
    val colorHex: String = "#E0F2FE",
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val timeFormatted: String = "",
    val priority: String = "متوسط", // عالي, متوسط, منخفض
    val isCompleted: Boolean = false,
    val repeatType: String = "مرة واحدة" // مرة واحدة, يومي, أسبوعي, شهري
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personName: String,
    val type: String, // "TO_RECEIVE" (لي) or "TO_PAY" (علي)
    val amount: Double,
    val dueDate: String = "",
    val notes: String = "",
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings")
data class SavingsVaultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: String = "",
    val category: String = "ادخار عام",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saving_transactions")
data class SavingTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vaultId: Int,
    val type: String, // "DEPOSIT" (إيداع) or "WITHDRAWAL" (سحب)
    val amount: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "links")
data class ImportantLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val category: String = "عام",
    val description: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "وثيقة رسمية", // شهادات, عقود, هوية, أخرى
    val notes: String = "",
    val fileUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "receipts")
data class PhotoReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double = 0.0,
    val category: String = "عام", // فواتير, تسوق, مطاعم, صيانة, أخرى
    val imageUri: String = "",
    val dateFormatted: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val recoveryEmail: String = "",
    val role: String = "USER", // "ADMIN" or "USER"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recovery_requests")
data class RecoveryRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val recoveryEmail: String,
    val status: String = "قيد الانتظار",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val sender: String = "الإدارة 🛡️",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

