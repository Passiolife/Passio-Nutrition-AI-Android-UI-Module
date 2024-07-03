package ai.passio.nutrition.uimodule.ui.engineering

import ai.passio.nutrition.uimodule.databinding.FragmentPhotoBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class AdvisorFragment : Fragment() {

    private lateinit var binding: FragmentPhotoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

}