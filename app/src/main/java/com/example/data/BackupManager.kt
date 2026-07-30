package com.example.data

import com.example.data.entities.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(private val db: AppDatabase) {

    suspend fun exportToJson(): String {
        val root = JSONObject()

        // 1. Notes
        val notes = db.noteDao().getAllNotes().first()
        val notesArray = JSONArray()
        notes.forEach { note ->
            val obj = JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("content", note.content)
                put("category", note.category)
                put("colorHex", note.colorHex)
                put("isPinned", note.isPinned)
                put("createdAt", note.createdAt)
            }
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        // 2. Reminders
        val reminders = db.reminderDao().getAllReminders().first()
        val remindersArray = JSONArray()
        reminders.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("description", r.description)
                put("dueDate", r.dueDate)
                put("timeFormatted", r.timeFormatted)
                put("priority", r.priority)
                put("isCompleted", r.isCompleted)
                put("repeatType", r.repeatType)
            }
            remindersArray.put(obj)
        }
        root.put("reminders", remindersArray)

        // 3. Debts
        val debts = db.debtDao().getAllDebts().first()
        val debtsArray = JSONArray()
        debts.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("personName", d.personName)
                put("type", d.type)
                put("amount", d.amount)
                put("dueDate", d.dueDate)
                put("notes", d.notes)
                put("isPaid", d.isPaid)
                put("createdAt", d.createdAt)
            }
            debtsArray.put(obj)
        }
        root.put("debts", debtsArray)

        // 4. Savings Vaults
        val vaults = db.savingsDao().getAllVaults().first()
        val vaultsArray = JSONArray()
        vaults.forEach { v ->
            val obj = JSONObject().apply {
                put("id", v.id)
                put("title", v.title)
                put("targetAmount", v.targetAmount)
                put("currentAmount", v.currentAmount)
                put("deadline", v.deadline)
                put("category", v.category)
                put("createdAt", v.createdAt)
            }
            vaultsArray.put(obj)
        }
        root.put("savings", vaultsArray)

        // 5. Links
        val links = db.linkDao().getAllLinks().first()
        val linksArray = JSONArray()
        links.forEach { l ->
            val obj = JSONObject().apply {
                put("id", l.id)
                put("title", l.title)
                put("url", l.url)
                put("category", l.category)
                put("description", l.description)
                put("isFavorite", l.isFavorite)
                put("createdAt", l.createdAt)
            }
            linksArray.put(obj)
        }
        root.put("links", linksArray)

        // 6. Documents
        val documents = db.documentDao().getAllDocuments().first()
        val docsArray = JSONArray()
        documents.forEach { doc ->
            val obj = JSONObject().apply {
                put("id", doc.id)
                put("title", doc.title)
                put("category", doc.category)
                put("notes", doc.notes)
                put("fileUri", doc.fileUri)
                put("createdAt", doc.createdAt)
            }
            docsArray.put(obj)
        }
        root.put("documents", docsArray)

        // 7. Receipts
        val receipts = db.receiptDao().getAllReceipts().first()
        val receiptsArray = JSONArray()
        receipts.forEach { rec ->
            val obj = JSONObject().apply {
                put("id", rec.id)
                put("title", rec.title)
                put("amount", rec.amount)
                put("category", rec.category)
                put("imageUri", rec.imageUri)
                put("dateFormatted", rec.dateFormatted)
                put("notes", rec.notes)
                put("createdAt", rec.createdAt)
            }
            receiptsArray.put(obj)
        }
        root.put("receipts", receiptsArray)

        root.put("backupDate", System.currentTimeMillis())
        root.put("appName", "منظم حياتي")

        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            // Import Notes
            if (root.has("notes")) {
                val arr = root.getJSONArray("notes")
                val list = mutableListOf<NoteEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        NoteEntity(
                            id = o.optInt("id", 0),
                            title = o.optString("title", ""),
                            content = o.optString("content", ""),
                            category = o.optString("category", "عام"),
                            colorHex = o.optString("colorHex", "#E0F2FE"),
                            isPinned = o.optBoolean("isPinned", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.noteDao().insertAll(list)
                }
            }

            // Import Reminders
            if (root.has("reminders")) {
                val arr = root.getJSONArray("reminders")
                val list = mutableListOf<ReminderEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        ReminderEntity(
                            id = o.optInt("id", 0),
                            title = o.optString("title", ""),
                            description = o.optString("description", ""),
                            dueDate = o.optLong("dueDate", System.currentTimeMillis()),
                            timeFormatted = o.optString("timeFormatted", ""),
                            priority = o.optString("priority", "متوسط"),
                            isCompleted = o.optBoolean("isCompleted", false),
                            repeatType = o.optString("repeatType", "مرة واحدة")
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.reminderDao().insertAll(list)
                }
            }

            // Import Debts
            if (root.has("debts")) {
                val arr = root.getJSONArray("debts")
                val list = mutableListOf<DebtEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        DebtEntity(
                            id = o.optInt("id", 0),
                            personName = o.optString("personName", ""),
                            type = o.optString("type", "TO_RECEIVE"),
                            amount = o.optDouble("amount", 0.0),
                            dueDate = o.optString("dueDate", ""),
                            notes = o.optString("notes", ""),
                            isPaid = o.optBoolean("isPaid", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.debtDao().insertAll(list)
                }
            }

            // Import Savings
            if (root.has("savings")) {
                val arr = root.getJSONArray("savings")
                val list = mutableListOf<SavingsVaultEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        SavingsVaultEntity(
                            id = o.optInt("id", 0),
                            title = o.optString("title", ""),
                            targetAmount = o.optDouble("targetAmount", 0.0),
                            currentAmount = o.optDouble("currentAmount", 0.0),
                            deadline = o.optString("deadline", ""),
                            category = o.optString("category", "ادخار عام"),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.savingsDao().insertAllVaults(list)
                }
            }

            // Import Links
            if (root.has("links")) {
                val arr = root.getJSONArray("links")
                val list = mutableListOf<ImportantLinkEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        ImportantLinkEntity(
                            id = o.optInt("id", 0),
                            title = o.optString("title", ""),
                            url = o.optString("url", ""),
                            category = o.optString("category", "عام"),
                            description = o.optString("description", ""),
                            isFavorite = o.optBoolean("isFavorite", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.linkDao().insertAll(list)
                }
            }

            // Import Documents
            if (root.has("documents")) {
                val arr = root.getJSONArray("documents")
                val list = mutableListOf<DocumentEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        DocumentEntity(
                            id = o.optInt("id", 0),
                            title = o.optString("title", ""),
                            category = o.optString("category", "وثيقة رسمية"),
                            notes = o.optString("notes", ""),
                            fileUri = o.optString("fileUri", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.documentDao().insertAll(list)
                }
            }

            // Import Receipts
            if (root.has("receipts")) {
                val arr = root.getJSONArray("receipts")
                val list = mutableListOf<PhotoReceiptEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        PhotoReceiptEntity(
                            id = o.optInt("id", 0),
                            title = o.optString("title", ""),
                            amount = o.optDouble("amount", 0.0),
                            category = o.optString("category", "عام"),
                            imageUri = o.optString("imageUri", ""),
                            dateFormatted = o.optString("dateFormatted", ""),
                            notes = o.optString("notes", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    db.receiptDao().insertAll(list)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
