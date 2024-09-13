package ai.passio.nutrition.uimodule.ui.engineering

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.databinding.FragmentPhotoBinding
import ai.passio.nutrition.uimodule.ui.util.PathUtil
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.passiosdk.passiofood.FoodDetectionConfiguration
import ai.passio.passiosdk.passiofood.PassioSDK
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

private const val SELECT_IMAGE = 2482
private const val TAKE_PHOTO = 1274
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
private val REQUIRED_PERMISSIONS_33 = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
private const val REQUEST_CODE_PERMISSIONS = 231

private val PERMISSIONS = if (Build.VERSION.SDK_INT >= 33) {
    REQUIRED_PERMISSIONS_33
} else {
    REQUIRED_PERMISSIONS
}

class PhotoFragment(
    private val useLocalRecognition: Boolean
) : Fragment() {

    private var uri: Uri? = null
    private lateinit var binding: FragmentPhotoBinding
    private val adapter = PhotoResultAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resultList.adapter = adapter

        binding.gallery.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
                )
                return@setOnClickListener
            } else {
                choosePhotoFromGallery()
            }
        }

        binding.camera.setOnClickListener {
            takePhotoWithCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                choosePhotoFromGallery()
            }
        }
    }

    private fun hasPermissions() = PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun allPermissionsGranted() = PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePhotoWithCamera() {
        val file = File(Environment.getExternalStorageDirectory(), "temp.jpg")
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivityForResult(
            takePicture,
            TAKE_PHOTO
        )
    }

    private fun choosePhotoFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (activity == null) {
            return
        }

        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    try {
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                data.data
                            )
                        val path = PathUtil.getPath(requireContext(), data.data!!)
                        val rotated = modifyOrientation(bitmap, path!!)
                        onImageFetched(rotated)
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                requireContext().toast("Canceled")
            }
        }

        if (requestCode == TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                val file = File(Environment.getExternalStorageDirectory(), "temp.jpg")
                val fis = FileInputStream(file)
                val bitmap = BitmapFactory.decodeStream(fis)
                val rotated = modifyOrientation(bitmap, file.absolutePath)
                bitmap.recycle()
                onImageFetched(rotated)
                fis.close()
                file.delete()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                requireContext().toast("Canceled")
            }
        }
    }

    private fun onImageFetched(bitmap: Bitmap) {
        binding.photoImage.setImageBitmap(bitmap)
        adapter.clear()

        if (useLocalRecognition) {
            runLocalRecognition(bitmap)
        } else {
            runRemoteRecognition(bitmap)
        }
    }

    private fun runRemoteRecognition(bitmap: Bitmap) {
        PassioSDK.instance.recognizeImageRemote(bitmap) { result ->
            result.forEach { model ->
                adapter.addResult(model.foodDataInfo?.foodName ?: "No matched item")
            }
        }
    }

    private fun runLocalRecognition(bitmap: Bitmap) {
        val foodDetectionConfiguration = FoodDetectionConfiguration(
            detectBarcodes = true,
            detectPackagedFood = true
        )
        PassioSDK.instance.detectFoodIn(
            bitmap,
            foodDetectionConfiguration
        ) { candidates ->
            candidates?.barcodeCandidates?.forEach {
                PassioSDK.instance.fetchFoodItemForProductCode(it.barcode) { foodItem ->
                    if (foodItem != null) {
                        adapter.addResult(foodItem.name)
                    }
                }
            }
            candidates?.packagedFoodCandidates?.forEach {
                PassioSDK.instance.fetchFoodItemForProductCode(it.packagedFoodCode) { foodItem ->
                    if (foodItem != null) {
                        adapter.addResult(foodItem.name)
                    }
                }
            }
            candidates?.detectedCandidates?.forEach {
                PassioSDK.instance.fetchFoodItemForPassioID(it.passioID) { foodItem ->
                    if (foodItem != null) {
                        adapter.addResult(foodItem.name)
                    }
                }
            }
        }
    }

    private fun modifyOrientation(bitmap: Bitmap, imagePath: String): Bitmap {
        val ei = ExifInterface(imagePath)
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> bitmap.rotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> bitmap.rotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> bitmap.rotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> bitmap.flip(true, false)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> bitmap.flip(false, true)
            else -> bitmap
        }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            this,
            0,
            0,
            this.width,
            this.height,
            matrix,
            true
        )
    }

    private fun Bitmap.flip(horizontal: Boolean, vertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
        return Bitmap.createBitmap(
            this,
            0,
            0,
            this.width,
            this.height,
            matrix,
            true
        )
    }
}