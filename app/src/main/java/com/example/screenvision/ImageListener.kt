package com.example.screenvision

import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


class ImageListener : OnImageAvailableListener {
    var count = 0
    override fun onImageAvailable(imageReader: ImageReader) {
        var msg: String? = null
        try {
            val image: Image = imageReader.acquireNextImage()
            if (image == null) {
                msg = "onImageAvailable: no image"
                return
            } else {
                msg =
                    """onImageAvailable: #${count++} $image Dimension: ${image.width}x${image.height} Format: ${image.format} $image."""
                takeCapture(image)
                image.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            msg = "onImageAvailable: $e"
        }
        if (msg != null) {
            Log.i("TAG", msg)
        }
//        renderMsg(msg)
    }

    private fun takeCapture(image: Image) {
        if (image == null) {
            Log.d("TAG", "image: NULL")
            return
        }
        val width: Int = image.width
        val height: Int = image.height
        val planes: Array<Image.Plane> = image.planes
        val buffer: ByteBuffer = planes[0].buffer
        val pixelStride: Int = planes[0].pixelStride
        val rowStride: Int = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width
        var mBitmap =
            Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        mBitmap.copyPixelsFromBuffer(buffer)
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height)
        image.close()
        saveBitmapToFile(mBitmap)
    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath,
            "TEMP"
        )
        try {
            if (!directory.exists()) directory.mkdirs()
            val name = "shot" + SimpleDateFormat("yyyyMMddHHmmsss").format(Date())
                .toString() + "." + Bitmap.CompressFormat.PNG.toString()
            val file = File(directory, name)
            if (!file.exists()) {
                file.createNewFile()
            }
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}