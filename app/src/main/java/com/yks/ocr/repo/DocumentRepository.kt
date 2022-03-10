package com.yks.ocr.repo

import com.yks.ocr.db.DocumentDatabase
import com.yks.ocr.model.Document
import javax.inject.Inject

class DocumentRepository @Inject constructor(private val db: DocumentDatabase){

    fun getAllDocs() = db.getDocumentDao().getAllDocuments()

    suspend fun updateDoc(document: Document) = db.getDocumentDao().updateDocument(document)

    suspend fun insertDoc(document: Document) =  db.getDocumentDao().insertDocument(document)

    suspend fun deleteDoc(id: Int) = db.getDocumentDao().deleteDocument(id)

}