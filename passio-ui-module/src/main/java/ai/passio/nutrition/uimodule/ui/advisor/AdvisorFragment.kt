package ai.passio.nutrition.uimodule.ui.advisor

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.data.ResultWrapper
import ai.passio.nutrition.uimodule.databinding.FragmentAdvisorBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ai.passio.nutrition.uimodule.ui.base.BaseFragment
import ai.passio.nutrition.uimodule.ui.base.BaseToolbar
import ai.passio.nutrition.uimodule.ui.image.TakePhotoFragment
import ai.passio.nutrition.uimodule.ui.model.PassioAdvisorData
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.disable
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.enable
import ai.passio.nutrition.uimodule.ui.util.toast
import ai.passio.nutrition.uimodule.ui.util.uriToBitmap
import ai.passio.nutrition.uimodule.ui.view.VerticalSpaceItemDecoration
import ai.passio.passiosdk.passiofood.data.model.PassioResult
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdvisorFragment : BaseFragment<AdvisorViewModel>(isSharedContext = true) {

    private var _binding: FragmentAdvisorBinding? = null
    private val binding: FragmentAdvisorBinding get() = _binding!!
    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private var advisorAdapter: AdvisorAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvisorBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        with(binding)
        {
            toolbar.setup(getString(R.string.ai_advisor), baseToolbarListener)
            addPhoto.setOnClickListener {
                showAttachmentMenu(addPhoto)
            }
            retry.setOnClickListener {
                binding.progressInit.isVisible = true
                binding.failedInit.isVisible = false
                viewModel.initConversation()
            }
            send.setOnClickListener {
                viewModel.sendTextMessage(binding.message.text.toString())
                binding.message.text?.clear()
            }
        }

        viewModel.initConversation()
        setupPickImage()

    }

    private fun setupPickImage() {
        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    result.data?.clipData?.let { clipData ->
                        if (clipData.itemCount > TakePhotoFragment.MAX_IMAGES) {
                            requireContext().toast("You can only select up to ${TakePhotoFragment.MAX_IMAGES} images.")
                            return@registerForActivityResult
                        }
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until clipData.itemCount) {
                            uris.add(clipData.getItemAt(i).uri)
                        }
                        onImagesPicked(uris)
                    } ?: result.data?.data?.let {
                        onImagesPicked(listOf(it))
                    }
                }
            }
    }

    private fun pickImages() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select Pictures"))
    }

    private fun onImagesPicked(imageUris: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO)
        {
            val bitmaps = mutableListOf<Bitmap>()
            imageUris.forEach { uri ->
                val bitmap = uriToBitmap(requireContext(), uri)
                if (bitmap != null) {
                    bitmaps.add(bitmap)
                }
            }
            lifecycleScope.launch(Dispatchers.Default) {
                viewModel.sendImages(bitmaps)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        binding.progressInit.isVisible = true
        binding.failedInit.isVisible = false
        sharedViewModel.photoFoodResultLD.observe(viewLifecycleOwner) {
            viewModel.sendImages(it)
        }
        viewModel.initConversation.observe(viewLifecycleOwner) { passioResult ->
            binding.progressInit.isVisible = false
            if (passioResult is PassioResult.Success) {
                binding.failedInit.isVisible = false
            } else if (passioResult is PassioResult.Error) {
                binding.retry.text =
                    passioResult.message + "\n" + getString(R.string.failed_to_init_ai_advisor)
                binding.failedInit.isVisible = true
            }
        }
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            requireContext().toast(errorMessage)
        }
        viewModel.passioAdvisorEvent.observe(viewLifecycleOwner, ::onMessageSentReceived)

        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.progressInit.isVisible = it
            setupViewsWhileLoading(it)
        }
        viewModel.logFoodEvent.observe(viewLifecycleOwner, ::foodItemLogged)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun foodItemLogged(resultWrapper: ResultWrapper<Boolean>) {
        when (resultWrapper) {
            is ResultWrapper.Success -> {
                if (resultWrapper.value) {
                    binding.rvAdvisor.adapter?.notifyDataSetChanged()
                    requireContext().toast("Food item(s) logged.")
                } else {
                    requireContext().toast("Could not log food item(s).")
                }
            }

            is ResultWrapper.Error -> {
                requireContext().toast(resultWrapper.error)
            }
        }
    }

    private fun setupViewsWhileLoading(isLoading: Boolean) {
        with(binding)
        {
            if (isLoading) {
                addPhoto.disable()
                send.disable()
            } else {
                addPhoto.enable()
                send.enable()
            }
        }
    }

    private fun setupAdvisorAdapter() {
        with(binding)
        {
            if (rvAdvisor.adapter == null) {
                rvAdvisor.addItemDecoration(VerticalSpaceItemDecoration(DesignUtils.dp2px(8f)))
                advisorAdapter = AdvisorAdapter(::onLogFood, ::onFindFood, ::onViewDiary)
                rvAdvisor.adapter = advisorAdapter
            }
        }
    }


    private fun onFindFood(passioAdvisorData: PassioAdvisorData) {
        viewModel.findFood(passioAdvisorData)
    }

    private fun onLogFood(passioAdvisorData: PassioAdvisorData) {
        viewModel.logRecords(passioAdvisorData)
    }

    private fun onViewDiary(passioAdvisorData: PassioAdvisorData) {
        viewModel.navigateToDiary()
    }

    private fun onMessageSentReceived(advisorData: List<PassioAdvisorData>) {
        setupViewsWhileLoading(advisorData.count { it.dataItemType == PassioAdvisorData.TYPE_PROCESSING } > 0)
        setupAdvisorAdapter()
        advisorAdapter?.addData(advisorData)
        if (advisorData.isNotEmpty()) {
            lifecycleScope.launch {
                delay(200)
                binding.rvAdvisor.scrollToPosition(advisorData.size - 1)
            }
        }
    }

    private val baseToolbarListener = object : BaseToolbar.ToolbarListener {
        override fun onBack() {
            viewModel.navigateBack()
        }

        override fun onRightIconClicked() {
            showPopupMenu(binding.toolbar.findViewById(R.id.toolbarMenu))
        }

    }

    private fun showAttachmentMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.advisor_menu, popupMenu.menu)
        showMenuIcons(popupMenu)

        // Optional: Handle menu item clicks if needed
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.capture_photo -> {
                    viewModel.navigateToTakePhoto()
                    true
                }

                R.id.select_photo -> {
                    pickImages()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

}