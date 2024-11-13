package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.ui.activity.PassioUiModuleActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

internal const val USER_IMAGE_PREFIX = "userFood_"
internal fun generateImageID(): String {
    return "$USER_IMAGE_PREFIX${UUID.randomUUID()}"
}

internal fun isUserImage(imageId: String): Boolean {
    return imageId.startsWith(USER_IMAGE_PREFIX)

}

fun uriToBitmap(uri: Uri): Bitmap? {
    return try {
        val inputStream = PassioUiModuleActivity.getContext().contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun deleteImageFromStorage(fileName: String): Boolean {
    // Get the app's preferred directory (no permission required)
    val directory =
        PassioUiModuleActivity.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    // Create a unique file name
    val tempFileName = "$fileName.jpg"

    // Create the file in that directory
    val file = File(directory, tempFileName)

    file.deleteOnExit()
    return true

}

fun getBitmapFromStorage(fileName: String): Bitmap? {
    // Get the app's preferred directory (no permission required)
    val directory =
        PassioUiModuleActivity.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    // Create a unique file name
    val tempFileName = "$fileName.jpg"

    // Create the file in that directory
    val file = File(directory, tempFileName)

    if (file.exists()) {
        return BitmapFactory.decodeFile(file.path)
    }
    return null

}

fun saveBitmapToStorage(bitmap: Bitmap, fileName: String): String? {
    // Get the app's preferred directory (no permission required)
    val directory =
        PassioUiModuleActivity.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    // Create a unique file name
    val tempFileName = "$fileName.jpg"

    // Create the file in that directory
    val file = File(directory, tempFileName)

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