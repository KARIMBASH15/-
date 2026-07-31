package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BackupManager
import com.example.data.FirebaseSyncManager
import com.example.data.SecurityLockManager
import com.example.data.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val backupManager = BackupManager(db)
    val securityLockManager = SecurityLockManager(application)
    val firebaseSyncManager = FirebaseSyncManager(application)
    val aiConsultantManager = com.example.data.AiConsultantManager(application)
    val authManager = com.example.data.AuthManager(application)
    val appNotificationManager = com.example.data.AppNotificationManager(application)

    private val _isUserLoggedIn = MutableStateFlow(authManager.isLoggedIn())
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _currentUsername = MutableStateFlow(authManager.getCurrentUsername())
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _currentUserRole = MutableStateFlow(authManager.getCurrentRole())
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    val allUsers: StateFlow<List<UserEntity>> = db.userDao().getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recoveryRequests: StateFlow<List<RecoveryRequestEntity>> = db.userDao().getAllRecoveryRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<AppNotificationEntity>> = db.notificationDao().getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = allNotifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
        // Ensure Admin Account exists in DB
        viewModelScope.launch(Dispatchers.IO) {
            val adminUser = db.userDao().getUserByUsername("km512")
            if (adminUser == null) {
                db.userDao().insertUser(
                    UserEntity(
                        username = "km512",
                        passwordHash = "8090",
                        recoveryEmail = "admin@lifeorganizer.com",
                        role = "ADMIN"
                    )
                )
            }
        }
    }

    fun loginUser(
        usernameInput: String,
        passwordInput: String,
        rememberMe: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanUsername = usernameInput.trim()
            val cleanPassword = passwordInput.trim()

            if (cleanUsername.isBlank() || cleanPassword.isBlank()) {
                withContext(Dispatchers.Main) { onError("الرجاء إدخال اسم المستخدم وكلمة السر") }
                return@launch
            }

            // Special Admin Account Check
            if (cleanUsername.equals("km512", ignoreCase = true) && cleanPassword == "8090") {
                authManager.saveSession("km512", "ADMIN", rememberMe)
                _currentUsername.value = "km512"
                _currentUserRole.value = "ADMIN"
                _isUserLoggedIn.value = true

                // Auto-restore cloud data on login
                restoreUserCloudData("km512")
                withContext(Dispatchers.Main) { onSuccess() }
                return@launch
            }

            // Standard User Check in DB
            val existingUser = db.userDao().getUserByUsername(cleanUsername)
            if (existingUser != null && existingUser.passwordHash == cleanPassword) {
                authManager.saveSession(existingUser.username, existingUser.role, rememberMe)
                _currentUsername.value = existingUser.username
                _currentUserRole.value = existingUser.role
                _isUserLoggedIn.value = true

                // Auto-restore cloud data on login
                restoreUserCloudData(existingUser.username)
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) { onError("اسم المستخدم أو كلمة السر غير صحيحة") }
            }
        }
    }

    private suspend fun restoreUserCloudData(username: String) {
        try {
            // First try default sync pin
            val resDefault = firebaseSyncManager.downloadFromFirebase()
            if (resDefault.isSuccess && !resDefault.getOrNull().isNullAndBlank()) {
                backupManager.importFromJson(resDefault.getOrNull()!!)
            } else {
                // Try username key
                val resUsername = firebaseSyncManager.downloadFromFirebase(username.lowercase())
                if (resUsername.isSuccess && !resUsername.getOrNull().isNullAndBlank()) {
                    backupManager.importFromJson(resUsername.getOrNull()!!)
                }
            }
            lastSyncTimeState.value = firebaseSyncManager.getLastSyncTime()
            triggerAutoSync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun String?.isNullAndBlank(): Boolean {
        return this.isNullOrBlank() || this == "null" || this == "{}"
    }

    fun registerUser(
        usernameInput: String,
        passwordInput: String,
        emailInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanUsername = usernameInput.trim()
            val cleanPassword = passwordInput.trim()
            val cleanEmail = emailInput.trim()

            if (cleanUsername.isBlank() || cleanPassword.isBlank() || cleanEmail.isBlank()) {
                withContext(Dispatchers.Main) { onError("جميع الحقول مطلوبة للتسجيل") }
                return@launch
            }

            val existingUser = db.userDao().getUserByUsername(cleanUsername)
            if (existingUser != null) {
                withContext(Dispatchers.Main) { onError("اسم المستخدم مسجل بالفعل، اختر اسم آخر أو سجل دخولك") }
                return@launch
            }

            val newUser = UserEntity(
                username = cleanUsername,
                passwordHash = cleanPassword,
                recoveryEmail = cleanEmail,
                role = if (cleanUsername.equals("km512", ignoreCase = true)) "ADMIN" else "USER"
            )
            db.userDao().insertUser(newUser)

            authManager.saveSession(newUser.username, newUser.role, rememberMe = true)
            _currentUsername.value = newUser.username
            _currentUserRole.value = newUser.role
            _isUserLoggedIn.value = true

            triggerAutoSync()
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun submitRecoveryRequest(
        usernameInput: String,
        emailInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanUsername = usernameInput.trim()
            val cleanEmail = emailInput.trim()

            if (cleanUsername.isBlank() || cleanEmail.isBlank()) {
                withContext(Dispatchers.Main) { onError("الرجاء إدخال اسم المستخدم وإيميل الاسترداد") }
                return@launch
            }

            db.userDao().insertRecoveryRequest(
                RecoveryRequestEntity(
                    username = cleanUsername,
                    recoveryEmail = cleanEmail
                )
            )

            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun logoutUser() {
        authManager.clearSession()
        _isUserLoggedIn.value = false
        _currentUsername.value = ""
        _currentUserRole.value = "USER"
    }

    fun deleteUser(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.userDao().deleteUser(username)
        }
    }

    fun deleteRecoveryRequest(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.userDao().deleteRecoveryRequest(id)
        }
    }

    fun clearAllDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllTables()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiConsultantManager.shutdown()
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

    fun triggerAutoSync() {
        if (firebaseSyncManager.isAutoSyncEnabled()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val json = backupManager.exportToJson()
                    firebaseSyncManager.uploadToFirebase(json)
                    val username = authManager.getCurrentUsername()
                    if (username.isNotBlank()) {
                        firebaseSyncManager.uploadToFirebase(json, customPin = username.lowercase())
                    }
                    lastSyncTimeState.value = firebaseSyncManager.getLastSyncTime()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- NOTES CRUD ---
    fun addNote(note: NoteEntity) = viewModelScope.launch { db.noteDao().insertNote(note); triggerAutoSync() }
    fun updateNote(note: NoteEntity) = viewModelScope.launch { db.noteDao().updateNote(note); triggerAutoSync() }
    fun deleteNote(note: NoteEntity) = viewModelScope.launch { db.noteDao().deleteNote(note); triggerAutoSync() }

    // --- REMINDERS CRUD ---
    fun addReminder(reminder: ReminderEntity) = viewModelScope.launch { db.reminderDao().insertReminder(reminder); triggerAutoSync() }
    fun updateReminder(reminder: ReminderEntity) = viewModelScope.launch { db.reminderDao().updateReminder(reminder); triggerAutoSync() }
    fun deleteReminder(reminder: ReminderEntity) = viewModelScope.launch { db.reminderDao().deleteReminder(reminder); triggerAutoSync() }

    // --- DEBTS CRUD ---
    fun addDebt(debt: DebtEntity) = viewModelScope.launch { db.debtDao().insertDebt(debt); triggerAutoSync() }
    fun updateDebt(debt: DebtEntity) = viewModelScope.launch { db.debtDao().updateDebt(debt); triggerAutoSync() }
    fun deleteDebt(debt: DebtEntity) = viewModelScope.launch { db.debtDao().deleteDebt(debt); triggerAutoSync() }

    // --- SAVINGS CRUD ---
    fun addSavingsVault(vault: SavingsVaultEntity) = viewModelScope.launch { db.savingsDao().insertVault(vault); triggerAutoSync() }
    fun updateSavingsVault(vault: SavingsVaultEntity) = viewModelScope.launch { db.savingsDao().updateVault(vault); triggerAutoSync() }
    fun deleteSavingsVault(vault: SavingsVaultEntity) = viewModelScope.launch { db.savingsDao().deleteVault(vault); triggerAutoSync() }
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
        triggerAutoSync()
    }

    // --- LINKS CRUD ---
    fun addLink(link: ImportantLinkEntity) = viewModelScope.launch { db.linkDao().insertLink(link); triggerAutoSync() }
    fun updateLink(link: ImportantLinkEntity) = viewModelScope.launch { db.linkDao().updateLink(link); triggerAutoSync() }
    fun deleteLink(link: ImportantLinkEntity) = viewModelScope.launch { db.linkDao().deleteLink(link); triggerAutoSync() }

    // --- DOCUMENTS CRUD ---
    fun addDocument(doc: DocumentEntity) = viewModelScope.launch { db.documentDao().insertDocument(doc); triggerAutoSync() }
    fun updateDocument(doc: DocumentEntity) = viewModelScope.launch { db.documentDao().updateDocument(doc); triggerAutoSync() }
    fun deleteDocument(doc: DocumentEntity) = viewModelScope.launch { db.documentDao().deleteDocument(doc); triggerAutoSync() }

    // --- RECEIPTS CRUD ---
    fun addReceipt(receipt: PhotoReceiptEntity) = viewModelScope.launch { db.receiptDao().insertReceipt(receipt); triggerAutoSync() }
    fun updateReceipt(receipt: PhotoReceiptEntity) = viewModelScope.launch { db.receiptDao().updateReceipt(receipt); triggerAutoSync() }
    fun deleteReceipt(receipt: PhotoReceiptEntity) = viewModelScope.launch { db.receiptDao().deleteReceipt(receipt); triggerAutoSync() }

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

    // --- NOTIFICATION METHODS ---
    fun sendBroadcastNotification(title: String, message: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val notificationEntity = AppNotificationEntity(
                title = title,
                message = message,
                sender = "الإدارة 🛡️"
            )
            val insertedId = db.notificationDao().insertNotification(notificationEntity)
            
            // Post system status bar notification with subtle tone
            appNotificationManager.sendNotification(
                id = insertedId.toInt(),
                title = title,
                message = message
            )

            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.notificationDao().markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            db.notificationDao().markAllAsRead()
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.notificationDao().deleteNotification(id)
        }
    }
}
