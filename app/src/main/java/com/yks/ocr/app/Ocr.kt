package com.yks.ocr.app

import android.app.Application
import android.graphics.Bitmap
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Ocr: Application(){
    companion object{
        var bitmap: Bitmap? = null
        var imagePath: String? = null
        var orientation: Int? = null
    }
}