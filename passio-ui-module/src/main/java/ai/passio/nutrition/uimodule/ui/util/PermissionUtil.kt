package ai.passio.nutrition.uimodule.ui.util

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private interface PermissionCallback {
    fun onGranted()
    fun onDenied()
}

class PermissionUtil(
    private val fragment: Fragment,
    private val permission: String,
) {

    private var permissionCallback: PermissionCallback? = null

    // Register permission launcher
    private var permissionLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d("PermissionUtil", "permissionLauncher: isGranted: $isGranted")
            if (isGranted) {
                permissionCallback?.onGranted()
            } else {
                val permanentlyDenied =
                    !fragment.shouldShowRequestPermissionRationale(permission)
                checkOnDenied(permanentlyDenied)
            }
        }

    fun checkOnDenied(permanentlyDenied: Boolean) {
        if (permanentlyDenied) {
            showPermissionSettingsDialog()
        } else {
            permissionCallback?.onDenied()
        }
    }


    /**
     * Check and request a permission.
     *
     * @param fragment Fragment to host the permission dialog.
     * @param permission The permission to request (e.g., Manifest.permission.CAMERA).
     * @param onGranted Callback when the permission is granted.
     * @param onDenied Callback when the permission is denied or permanently denied.
     */
    fun checkAndRequestPermission(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ) {
        val context = fragment.requireContext()
        permissionCallback = object : PermissionCallback {
            override fun onGranted() {
                onGranted.invoke()
            }

            override fun onDenied() {
                onDenied.invoke()
            }
        }

        when {
            // Permission is already granted
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PermissionUtil", "Permission granted")
                onGranted()
            }

            // Show rationale if needed
            fragment.shouldShowRequestPermissionRationale(permission) -> {
                Log.d("PermissionUtil", "shouldShowRequestPermissionRationale: false")
//                checkOnDenied(false)
                permissionLauncher.launch(permission)
            }

            else -> {
                // Launch the permission request
                permissionLauncher.launch(permission)
            }
        }
    }

    /**
     * Show custom alert dialog to navigate to app settings if permission is permanently denied.
     *
     */
    private fun showPermissionSettingsDialog() {

        CommonDialog.show(
            fragment.requireContext(),
            positiveActionText = "Go to setting",
            negativeActionText = "Cancel",
            title = "Permission Required",
            description = "$permission access is needed to use this feature. Go to app setting and grant permission.",
            listener = object : OnCommonDialogListener {
                override fun onNegativeAction() {
                    permissionCallback?.onDenied()
                }

                override fun onPositiveAction() {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data =
                            Uri.fromParts("package", fragment.requireActivity().packageName, null)
                    }
                    fragment.requireContext().startActivity(intent)
                }

            }
        )
    }


}
