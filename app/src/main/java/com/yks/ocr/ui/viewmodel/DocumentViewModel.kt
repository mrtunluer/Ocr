package com.yks.ocr.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognizer
import com.yks.ocr.model.Document
import com.yks.ocr.repo.DocumentRepository
import com.yks.ocr.utils.LoadState
import com.yks.ocr.utils.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val documentRepo: DocumentRepository
) : ViewModel(){

    private val _state = MutableStateFlow<LoadState>(LoadState.Loading)
    val state = _state.asStateFlow()

    val scannedText: MutableLiveData<Text> by lazy {
        MutableLiveData<Text>()
    }

    val scannedException: MutableLiveData<Exception> by lazy {
        MutableLiveData<Exception>()
    }

    fun getAllDocs() {
        viewModelScope.launch(Dispatchers.IO) {
            documentRepo.getAllDocs().distinctUntilChanged().collect {docs ->
                if (docs.isNotEmpty()) {
                    _state.value = LoadState.Success(docs)
                } else {
                    _state.value = LoadState.Empty
                }
            }
        }
    }

    fun insertDoc(fileName: String, title: String, scanned: String, time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val doc = Document(
                fileName =fileName,
                title = title,
                scanned = scanned,
                time = time
            )
            documentRepo.insertDoc(doc)
        }
    }

    fun updateDoc(fileName: String, id: Int, title: String, scanned: String, time: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val doc = Document(
                fileName = fileName,
                id = id,
                title = title,
                scanned = scanned,
                time = time
            )
            documentRepo.updateDoc(doc)
        }
    }

    fun deleteDoc(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            documentRepo.deleteDoc(id)
        }
    }

    fun saveImgToInternalStorage(bitmap: Bitmap, context: Context, fileName: String): Boolean{
        return try {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)){
                    throw IOException("Couldn't save image")
                }
            }
            true
        }catch (e: IOException){
            context.toast(e.message.toString())
            false
        }
    }

    fun deleteImgToInternalStorage(context: Context, fileName: String): Boolean{
        return try {
            context.deleteFile(fileName)
            true
        }catch (e: IOException){
            context.toast(e.message.toString())
            false
        }
    }

    fun processImage(inputImage: InputImage, recognizer: TextRecognizer) =
        recognizer.process(inputImage).addOnSuccessListener { text ->
            text?.let {
                scannedText.value = it
            }
        }.addOnFailureListener { exception ->
            scannedException.value = exception
        }

}