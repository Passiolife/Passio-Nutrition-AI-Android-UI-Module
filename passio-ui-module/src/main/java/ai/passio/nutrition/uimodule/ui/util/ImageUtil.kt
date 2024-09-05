package ai.passio.nutrition.uimodule.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}


fun saveBitmapToStorage(context: Context, bitmap: Bitmap): String? {
    // Get the app's preferred directory (no permission required)
    val directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    // Create a unique file name
    val fileName = "image_${System.currentTimeMillis()}_${Math.random()}.jpg"

    // Create the file in that directory
    val file = File(directory, fileName)

    try {
        // Create output stream to write the bitmap to the file
        val outputStream = FileOutputStream(file)

        // Compress the bitmap as PNG and write to the output stream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Flush and close the stream
        outputStream.flush()
        outputStream.close()

        // Return the file's absolute path
        return file.absolutePath

    } catch (e: IOException) {
        e.printStackTrace()
    }

    return null // Return null if there was an error
}