package com.yks.ocr.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yks.ocr.model.Document

@Database(
    entities = [Document::class],
    version = 1
)
abstract class DocumentDatabase: RoomDatabase() {
    companion object{
        const val DB_NAME = "document_db"
    }
    abstract fun getDocumentDao(): DocumentDao
}