package ai.passio.nutrition.uimodule.ui.view

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class BitmapAnalyzer(private val listener: (Bitmap?) -> Unit) : ImageAnalysis.Analyzer {

    private var isTakePhotoOn = false
    fun takePhoto() {
        isTakePhotoOn = true
    }

    override fun analyze(image: ImageProxy) {
        if (isTakePhotoOn) {
            isTakePhotoOn = false
            val bitmap = imageProxyToBitmap(image)
            listener.invoke(bitmap)
        }
        image.close()
    }

    private val yuvBytes = init2DByteArray(3, null)
    private var rgbBytes: IntArray? = null

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        rgbBytes = IntArray(imageProxy.width * imageProxy.height)

        val planes = imageProxy.planes
        imageProxyToYUV(planes, yuvBytes)

        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride

        convertYUV420ToARGB8888(
            yuvBytes[0]!!,
            yuvBytes[1]!!,
            yuvBytes[2]!!,
            imageProxy.width,
            imageProxy.height,
            yRowStride,
            uvRowStride,
            uvPixelStride,
            rgbBytes!!
        )

        var bitmap = Bitmap.createBitmap(
            rgbBytes!!,
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )

        bitmap =
            rotateBitmap(src = bitmap, degrees = imageProxy.imageInfo.rotationDegrees.toFloat())
        return bitmap
    }
}

private fun rotateBitmap(src: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(
        src,
        0,
        0,
        src.width,
        src.height,
        matrix,
        true
    )
}


private fun convertYUV420ToARGB8888(
    yData: ByteArray,
    uData: ByteArray,
    vData: ByteArray,
    width: Int,
    height: Int,
    yRowStride: Int,
    uvRowStride: Int,
    uvPixelStride: Int,
    out: IntArray
) {
    var yp = 0
    for (j in 0 until height) {
        val pY = yRowStride * j
        val pUV = uvRowStride * (j shr 1)

        for (i in 0 until width) {
            val uv_offset = pUV + (i shr 1) * uvPixelStride

            try {
                out[yp++] = YUV2RGB(
                    0xff and yData[pY + i].toInt(),
                    0xff and uData[uv_offset].toInt(),
                    0xff and vData[uv_offset].toInt()
                )
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
                throw e
            }

        }
    }
}

private fun imageProxyToYUV(
    planes: Array<ImageProxy.PlaneProxy>,
    yuvBytes: Array<ByteArray?>
) {
    planes.indices.forEach { i ->
        val buffer = planes[i].buffer
        buffer.rewind()
        if (yuvBytes[i] == null || yuvBytes[i]!!.size != buffer.capacity()) {
            yuvBytes[i] = ByteArray(buffer.capacity())
        }
        buffer.get(yuvBytes[i]!!)
    }
}

private fun init2DByteArray(
    dim1: Int, dim2: Int?,
    init: (Int, Int) -> Byte = { _, _ -> 0 }
): Array<ByteArray?> {
    return Array(dim1) { i ->
        if (dim2 == null) {
            null
        } else {
            ByteArray(dim2) { j ->
                init(i, j)
            }
        }
    }
}

private fun YUV2RGB(yValue: Int, uValue: Int, vValue: Int): Int {
    var y = yValue
    var u = uValue
    var v = vValue
    // Adjust and check YUV values
    y = if (y - 16 < 0) 0 else y - 16
    u -= 128
    v -= 128

    // This is the floating point equivalent. We do the conversion in integer
    // because some Android devices do not have floating point in hardware.
    // nR = (int)(1.164 * nY + 2.018 * nU);
    // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
    // nB = (int)(1.164 * nY + 1.596 * nV);
    val y1192 = 1192 * y
    var r = y1192 + 1634 * v
    var g = y1192 - 833 * v - 400 * u
    var b = y1192 + 2066 * u

    // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
    r = if (r > kMaxChannelValue) kMaxChannelValue else if (r < 0) 0 else r
    g = if (g > kMaxChannelValue) kMaxChannelValue else if (g < 0) 0 else g
    b = if (b > kMaxChannelValue) kMaxChannelValue else if (b < 0) 0 else b

    return -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
}

private const val kMaxChannelValue = 262143