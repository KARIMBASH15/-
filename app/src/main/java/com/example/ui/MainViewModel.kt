package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BackupManager
import com.example.data.FirebaseSyncManager
import com.example.data.SecurityLockManager
import com.example.data.entities.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val backupManager = BackupManager(db)
    val securityLockManager = SecurityLockManager(application)
    val firebaseSyncManager = FirebaseSyncManager(application)

    val syncPinState = MutableStateFlow(firebaseSyncManager.getSyncPin())
    val lastSyncTimeState = MutableStateFlow(firebaseSyncManager.getLastSyncTime())
    val isAutoSyncState = MutableStateFlow(firebaseSyncManager.isAutoSyncEnabled())

    val notes: StateFlow<List<NoteEntity>> = db.noteDao().getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = db.reminderDao().getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = db.debtDao().getAllDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsVaults: StateFlow<List<SavingsVaultEntity>> = db.savingsDao().getAllVaults()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val links: StateFlow<List<ImportantLinkEntity>> = db.linkDao().getAllLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = db.documentDao().getAllDocuments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receipts: StateFlow<List<PhotoReceiptEntity>> = db.receiptDao().getAllReceipts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Lock State
    private val _isAppLocked = MutableStateFlow(securityLockManager.isLockEnabled())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    init {
        seedInitialDataIfEmpty()
    }

    private fun seedInitialDataIfEmpty() {
        viewModelScope.launch {
            if (db.noteDao().getAllNotes().first().isEmpty()) {
                db.noteDao().insertNote(
                    NoteEntity(
                        title = "قائمة أهداف الشهر",
                        content = "١. الانتهاء من مشروع منظم حياتي\n٢. توفير ٥٠٠ جنيه في صندوق التحويش\n٣. قراءة كتابين في التطوير الذاتي",
                        category = "أهداف",
                        colorHex = "#E0F2FE",
                        isPinned = true
                    )
                )
                db.noteDao().insertNote(
                    NoteEntity(
                        title = "وصفة كيكة الزعفران",
                        content = "المقادير: ٢ كوب طحين، ١ كوب حليب، ٠.٥ كوب زيت، زعفران نقع في ماء ورد.",
                        category = "وصفات",
                        colorHex = "#FEF3C7",
                        isPinned = false
                    )
                )
            }

            if (db.reminderDao().getAllReminders().first().isEmpty()) {
                db.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "موعد فحص السيارة الدوري",
                        description = "المرور بمركز الصيانة للتأكد من الزيت والفرامل",
                        timeFormatted = "10:00 ص",
                        priority = "عالي",
                        repeatType = "شهري"
                    )
                )
                db.reminderDao().insertReminder(
                    ReminderEntity(
                        title = "سداد فاتورة الكهرباء",
                        description = "تاريخ الاستحقاق اليوم",
                        timeFormatted = "08:00 م",
                        priority = "عالي",
                        repeatType = "شهري"
                    )
                )
            }

            if (db.debtDao().getAllDebts().first().isEmpty()) {
                db.debtDao().insertDebt(
                    DebtEntity(
                        personName = "محمد العتيبي",
                        type = "TO_RECEIVE", // لي
                        amount = 350.0,
                        dueDate = "2026-08-15",
                        notes = "باقي قيمة شراء المستلزمات"
                    )
                )
                db.debtDao().insertDebt(
                    DebtEntity(
                        personName = "متجر الإلكترونيات",
                        type = "TO_PAY", // علي
                        amount = 500.0,
                        dueDate = "2026-08-01",
                        notes = "قسط الجهاز المحمول"
                    )
                )
            }

            if (db.savingsDao().getAllVaults().first().isEmpty()) {
                db.savingsDao().insertVault(
                    SavingsVaultEntity(
                        title = "صندوق رحلة الصيف",
                        targetAmount = 5000.0,
                        currentAmount = 2400.0,
                        deadline = "2026-09-30",
                        category = "سفر"
                    )
                )
                db.savingsDao().insertVault(
                    SavingsVaultEntity(
                        title = "صندوق الطوارئ",
                        targetAmount = 10000.0,
                        currentAmount = 6500.0,
                        deadline = "2026-12-31",
                        category = "أمان مالى"
                    )
                )
            }

            if (db.linkDao().getAllLinks().first().isEmpty()) {
                db.linkDao().insertLink(
                    ImportantLinkEntity(
                        title = "بوابة الخدمات الحكومية",
                        url = "https://my.gov.sa",
                        category = "خدمات",
                        description = "موقع المعاملات اليومية والخدمات الحكومية",
                        isFavorite = true
                    )
                )
                db.linkDao().insertLink(
                    ImportantLinkEntity(
                        title = "منصة التدريب التقني",
                        url = "https://coursera.org",
                        category = "تعليم",
                        description = "دورات البرمجة وإدارة المشاريع",
                        isFavorite = false
                    )
                )
            }

            if (db.documentDao().getAllDocuments().first().isEmpty()) {
                db.documentDao().insertDocument(
                    DocumentEntity(
                        title = "الهوية الوطنية",
                        category = "هوية",
                        notes = "تاريخ الانتهاء 2030-05-12"
                    )
                )
                db.documentDao().insertDocument(
                    DocumentEntity(
                        title = "عقد إيجار المنزل",
                        category = "عقود",
                        notes = "مجدد سنويًا في شهر أغسطس"
                    )
                )
            }

            if (db.receiptDao().getAllReceipts().first().isEmpty()) {
                db.receiptDao().insertReceipt(
                    PhotoReceiptEntity(
                        title = "فاتورة صيانة السيارة",
                        amount = 280.0,
                        category = "صيانة",
                        dateFormatted = "2026-07-20",
                        notes = "تغيير زيت وفلتر المحرك"
                    )
                )
                db.receiptDao().insertReceipt(
                    PhotoReceiptEntity(
                        title = "مشتريات السوبرماركت",
                        amount = 145.50,
                        category = "تسوق",
                        dateFormatted = "2026-07-28",
                        notes = "أغراض الأسبوع المنزلية"
                    )
                )
            }
        }
    }

    // --- NOTES CRUD ---
    fun addNote(note: NoteEntity) = viewModelScope.launch { db.noteDao().insertNote(note) }
    fun updateNote(note: NoteEntity) = viewModelScope.launch { db.noteDao().updateNote(note) }
    fun deleteNote(note: NoteEntity) = viewModelScope.launch { db.noteDao().deleteNote(note) }

    // --- REMINDERS CRUD ---
    fun addReminder(reminder: ReminderEntity) = viewModelScope.launch { db.reminderDao().insertReminder(reminder) }
    fun updateReminder(reminder: ReminderEntity) = viewModelScope.launch { db.reminderDao().updateReminder(reminder) }
    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch { db.reminderDao().deleteReminder(reminder) }

    // --- DEBTS CRUD ---
    fun addDebt(debt: DebtEntity) = viewModelScope.launch { db.debtDao().insertDebt(debt) }
    fun updateDebt(debt: DebtEntity) = viewModelScope.launch { db.debtDao().updateDebt(debt) }
    fun deleteDebt(debt: DebtEntity) = viewModelScope.launch { db.debtDao().deleteDebt(debt) }

    // --- SAVINGS CRUD ---
    fun addSavingsVault(vault: SavingsVaultEntity) = viewModelScope.launch { db.savingsDao().insertVault(vault) }
    fun updateSavingsVault(vault: SavingsVaultEntity) = viewModelScope.launch { db.savingsDao().updateVault(vault) }
    fun deleteSavingsVault(vault: SavingsVaultEntity) = viewModelScope.launch { db.savingsDao().deleteVault(vault) }
    fun depositToVault(vault: SavingsVaultEntity, depositAmount: Double, note: String) = viewModelScope.launch {
        val updatedVault = vault.copy(currentAmount = vault.currentAmount + depositAmount)
        db.savingsDao().updateVault(updatedVault)
        db.savingsDao().insertTransaction(
            SavingTransactionEntity(
                vaultId = vault.id,
                type = "DEPOSIT",
                amount = depositAmount,
                note = note
            )
        )
    }

    // --- LINKS CRUD ---
    fun addLink(link: ImportantLinkEntity) = viewModelScope.launch { db.linkDao().insertLink(link) }
    fun updateLink(link: ImportantLinkEntity) = viewModelScope.launch { db.linkDao().updateLink(link) }
    fun deleteLink(link: ImportantLinkEntity) = viewModelScope.launch { db.linkDao().deleteLink(link) }

    // --- DOCUMENTS CRUD ---
    fun addDocument(doc: DocumentEntity) = viewModelScope.launch { db.documentDao().insertDocument(doc) }
    fun updateDocument(doc: DocumentEntity) = viewModelScope.launch { db.documentDao().updateDocument(doc) }
    fun deleteDocument(doc: DocumentEntity) = viewModelScope.launch { db.documentDao().deleteDocument(doc) }

    // --- RECEIPTS CRUD ---
    fun addReceipt(receipt: PhotoReceiptEntity) = viewModelScope.launch { db.receiptDao().insertReceipt(receipt) }
    fun updateReceipt(receipt: PhotoReceiptEntity) = viewModelScope.launch { db.receiptDao().updateReceipt(receipt) }
    fun deleteReceipt(receipt: PhotoReceiptEntity) = viewModelScope.launch { db.receiptDao().deleteReceipt(receipt) }

    // --- APP LOCK CONTROL ---
    fun unlockApp(pin: String): Boolean {
        if (securityLockManager.validatePin(pin)) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun lockApp() {
        if (securityLockManager.isLockEnabled()) {
            _isAppLocked.value = true
        }
    }

    fun updateSecuritySettings(enabled: Boolean, newPin: String) {
        securityLockManager.setLockEnabled(enabled)
        if (newPin.isNotEmpty()) {
            securityLockManager.setPinCode(newPin)
            setSyncPin(newPin)
        }
        _isAppLocked.value = enabled && newPin.isNotEmpty()
    }

    // --- FIREBASE SYNC METHODS ---
    fun setSyncPin(pin: String) {
        firebaseSyncManager.setSyncPin(pin)
        syncPinState.value = pin
    }

    fun setAutoSync(enabled: Boolean) {
        firebaseSyncManager.setAutoSyncEnabled(enabled)
        isAutoSyncState.value = enabled
    }

    fun uploadToFirebase(customPin: String? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportToJson()
            val result = firebaseSyncManager.uploadToFirebase(json, customPin)
            lastSyncTimeState.value = firebaseSyncManager.getLastSyncTime()
            if (result.isSuccess) {
                onResult(true, "تم مزامنة وحفظ البيانات بنجاح في Firebase Cloud! ☁️")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "حدث خطأ أثناء المزامنة مع Firebase")
            }
        }
    }

    fun downloadFromFirebase(customPin: String? = null, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = firebaseSyncManager.downloadFromFirebase(customPin)
            if (result.isSuccess) {
                val json = result.getOrNull() ?: ""
                val imported = backupManager.importFromJson(json)
                lastSyncTimeState.value = firebaseSyncManager.getLastSyncTime()
                if (imported) {
                    onResult(true, "تمت استعادة البيانات بنجاح من Firebase! 🎉")
                } else {
                    onResult(false, "صيغة البيانات المسترجعة غير صحيحة.")
                }
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "لم يتم العثور على بيانات في Firebase")
            }
        }
    }
}
