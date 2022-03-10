package com.yks.ocr.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yks.ocr.R

fun ImageView.download(context: Context, path: String?, isCenterCrop: Boolean) {
    if (isCenterCrop)
        Glide.with(context)
            .load(path)
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .into(this)
    else
        Glide.with(context)
            .load(path)
            .placeholder(R.drawable.placeholder)
            .into(this)
}