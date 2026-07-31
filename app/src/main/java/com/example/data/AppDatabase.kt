package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entities.*

@Database(
    entities = [
        NoteEntity::class,
        ReminderEntity::class,
        DebtEntity::class,
        SavingsVaultEntity::class,
        SavingTransactionEntity::class,
        ImportantLinkEntity::class,
        DocumentEntity::class,
        PhotoReceiptEntity::class,
        UserEntity::class,
        RecoveryRequestEntity::class,
        AppNotificationEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun debtDao(): DebtDao
    abstract fun savingsDao(): SavingsDao
    abstract fun linkDao(): LinkDao
    abstract fun documentDao(): DocumentDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "life_organizer_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
