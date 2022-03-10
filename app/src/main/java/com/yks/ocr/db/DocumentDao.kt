package com.yks.ocr.db

import androidx.room.*
import com.yks.ocr.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM Document ORDER BY time desc")
    fun getAllDocuments(): Flow<List<Document>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Query("DELETE FROM Document where id = :id")
    suspend fun deleteDocument(id: Int)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateDocument(document: Document)

}