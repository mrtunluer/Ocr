package com.yks.ocr.utils

import com.yks.ocr.model.Document

sealed class LoadState {
    data class Success(val docs: List<Document>) : LoadState()
    data class Error(val exception: Throwable) : LoadState()
    object Loading : LoadState()
    object Empty : LoadState()
}