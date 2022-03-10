package com.yks.ocr.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Document(
    val fileName: String,
    val title: String,
    val scanned: String,
    val time: Long,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
): Serializable