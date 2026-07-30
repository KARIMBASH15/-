package com.example.data.dao

import androidx.room.*
import com.example.data.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDate ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ReminderEntity>)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isPaid ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(debts: List<DebtEntity>)
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings ORDER BY createdAt DESC")
    fun getAllVaults(): Flow<List<SavingsVaultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: SavingsVaultEntity): Long

    @Update
    suspend fun updateVault(vault: SavingsVaultEntity)

    @Delete
    suspend fun deleteVault(vault: SavingsVaultEntity)

    @Query("SELECT * FROM saving_transactions WHERE vaultId = :vaultId ORDER BY timestamp DESC")
    fun getTransactionsForVault(vaultId: Int): Flow<List<SavingTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SavingTransactionEntity)

    @Query("DELETE FROM savings")
    suspend fun deleteAllVaults()

    @Query("DELETE FROM saving_transactions")
    suspend fun deleteAllTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVaults(vaults: List<SavingsVaultEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<SavingTransactionEntity>)
}

@Dao
interface LinkDao {
    @Query("SELECT * FROM links ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllLinks(): Flow<List<ImportantLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: ImportantLinkEntity): Long

    @Update
    suspend fun updateLink(link: ImportantLinkEntity)

    @Delete
    suspend fun deleteLink(link: ImportantLinkEntity)

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<ImportantLinkEntity>)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<DocumentEntity>)
}

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<PhotoReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: PhotoReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: PhotoReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: PhotoReceiptEntity)

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<PhotoReceiptEntity>)
}
