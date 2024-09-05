package ai.passio.nutrition.uimodule.ui.util

import ai.passio.nutrition.uimodule.ui.image.TakePhotoFragment
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

interface PhotoPickerListener {
    fun onImagePicked(uris: List<Uri>)
}

class PhotoPickerManager() {

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private var photoPickerListener: PhotoPickerListener? = null

    fun init(fragment: Fragment, photoPickerListener: PhotoPickerListener) {
        this.photoPickerListener = photoPickerListener
        pickImagesLauncher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    result.data?.clipData?.let { clipData ->
                        if (clipData.itemCount > TakePhotoFragment.MAX_IMAGES) {
                            fragment.requireContext()
                                .toast("You can only select up to ${TakePhotoFragment.MAX_IMAGES} images.")
                            return@registerForActivityResult
                        }
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until clipData.itemCount) {
                            uris.add(clipData.getItemAt(i).uri)
                        }
                        photoPickerListener.onImagePicked(uris)
                    } ?: result.data?.data?.let {
                        photoPickerListener.onImagePicked(listOf(it))
                    }
                }
            }
    }

    fun pickSingleImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    fun pickMultipleImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"))
    }
}