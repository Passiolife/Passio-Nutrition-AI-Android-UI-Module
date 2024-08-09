package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.DialogOpenFoodFactsBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment


class OpenFoodFactsDialog :
    DialogFragment() {

    private var _binding: DialogOpenFoodFactsBinding? = null
    private val binding: DialogOpenFoodFactsBinding get() = _binding!!


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            DesignUtils.screenWidth(requireContext()) - DesignUtils.dp2px(20f),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOpenFoodFactsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpannable()
        binding.ok.setOnClickListener {
            dismiss()
        }
    }

    private fun openURL(url: String) {
//        val url = "https://developers.android.com"
//        val intent = CustomTabsIntent.Builder()
//            .build()
//        intent.launchUrl(requireContext(), Uri.parse(url))


        // get the current toolbar background color (this might work differently in your app)
        @ColorInt val colorPrimaryLight =
            ContextCompat.getColor(requireContext(), R.color.passio_primary)
        @ColorInt val colorPrimaryDark =
            ContextCompat.getColor(requireContext(), R.color.passio_primary)

        val intent = CustomTabsIntent.Builder() // set the default color scheme
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorPrimaryLight)
                    .build()
            ) // set the alternative dark color scheme
            .setColorSchemeParams(
                CustomTabsIntent.COLOR_SCHEME_DARK, CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(colorPrimaryDark)
                    .build()
            )
            .build()
        intent.launchUrl(requireContext(), Uri.parse(url))
    }


    private fun setupSpannable() {
        val text = binding.lblTitle.text.toString()
        val spannableString = SpannableString(text)

        // Define the clickable span for "Open Food Facts"
        val openFoodFactsSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openURL("https://en.openfoodfacts.org")
            }
        }

        // Define the clickable span for "Open Database License"
        val openDatabaseLicenseSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openURL("https://opendatacommons.org/licenses/odbl/1-0")
            }
        }
        @ColorInt val colorPrimaryLight =
            ContextCompat.getColor(requireContext(), R.color.passio_primary)
        // Set the color and clickable span for "Open Food Facts"
        val openFoodFactsStart = text.indexOf("Open Food Facts")
        val openFoodFactsEnd = openFoodFactsStart + "Open Food Facts".length
        spannableString.setSpan(
            ForegroundColorSpan(colorPrimaryLight),
            openFoodFactsStart,
            openFoodFactsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            openFoodFactsSpan,
            openFoodFactsStart,
            openFoodFactsEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the color and clickable span for "Open Database License"
        val openDatabaseLicenseStart = text.indexOf("Open Database License")
        val openDatabaseLicenseEnd = openDatabaseLicenseStart + "Open Database License".length
        spannableString.setSpan(
            ForegroundColorSpan(colorPrimaryLight),
            openDatabaseLicenseStart,
            openDatabaseLicenseEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            openDatabaseLicenseSpan,
            openDatabaseLicenseStart,
            openDatabaseLicenseEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply the spannable string to the TextView
        binding.lblTitle.text = spannableString
        binding.lblTitle.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }
}