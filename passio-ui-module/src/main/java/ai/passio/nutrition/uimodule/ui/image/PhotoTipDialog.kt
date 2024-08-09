package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.databinding.DialogPhotoTipBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

class PhotoTipDialog :
    DialogFragment() {

    private var _binding: DialogPhotoTipBinding? = null
    private val binding: DialogPhotoTipBinding get() = _binding!!


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
        _binding = DialogPhotoTipBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ok.setOnClickListener {
            dismiss()
        }
    }
}