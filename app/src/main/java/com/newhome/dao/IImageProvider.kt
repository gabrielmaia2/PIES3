package com.newhome.dao

import android.graphics.Bitmap
import kotlinx.coroutines.Deferred

interface IImageProvider {
    fun getDefaultBitmap(): Bitmap

    suspend fun saveImage(path: String, bitmap: Bitmap?): Deferred<Unit>

    suspend fun getImage(path: String): Deferred<Bitmap>

    suspend fun removeImage(path: String): Deferred<ByteArray?>

    suspend fun getImageOrDefault(path: String): Deferred<Bitmap>
}
