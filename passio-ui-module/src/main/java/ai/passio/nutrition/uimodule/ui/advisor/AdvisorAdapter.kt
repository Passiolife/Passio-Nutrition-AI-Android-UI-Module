package ai.passio.nutrition.uimodule.ui.advisor

import ai.passio.nutrition.uimodule.databinding.ItemAdvisorProcessingBinding
import ai.passio.nutrition.uimodule.databinding.ItemAdvisorReceiverIngridientsBinding
import ai.passio.nutrition.uimodule.databinding.ItemAdvisorReceiverTextBinding
import ai.passio.nutrition.uimodule.databinding.ItemAdvisorSenderImageBinding
import ai.passio.nutrition.uimodule.databinding.ItemAdvisorSenderTextBinding
import ai.passio.nutrition.uimodule.databinding.ItemAdvisorWelcomeBinding
import ai.passio.nutrition.uimodule.ui.image.FoodImageResultAdapter
import ai.passio.nutrition.uimodule.ui.image.OnItemSelectChange
import ai.passio.nutrition.uimodule.ui.model.PassioAdvisorData
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.disable
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.enable
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.noties.markwon.Markwon

private var chatMarginSpace: Int = 0

internal class AdvisorAdapter(
    private val onLogFood: (passioAdvisorData: PassioAdvisorData) -> Unit,
    private val onFindFood: (passioAdvisorData: PassioAdvisorData) -> Unit,
    private val onViewDiary: (passioAdvisorData: PassioAdvisorData) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    private val list = mutableListOf<PassioAdvisorData>()

    private var markwon: Markwon? = null

    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<PassioAdvisorData>) {
        list.clear()
        list.addAll(newData)
        notifyDataSetChanged()
    }

    inner class WelcomeViewHolder(val binding: ItemAdvisorWelcomeBinding) :
        ViewHolder(binding.root) {
        fun bind(passioAdvisorData: PassioAdvisorData) {
            binding.root.endMargin()
//            val markwon = Markwon.builder(binding.root.context)
//                    .usePlugin(MarkwonPlugin.)
//                    .usePlugin(MarkwonHtmlPlugin.create())
//                .build()

            markwon?.setMarkdown(
                binding.tvMessage,
                passioAdvisorData.passioAdvisorResponse?.markupContent?.trimIndent() ?: ""
            )
        }
    }

    inner class ProcessingViewHolder(val binding: ItemAdvisorProcessingBinding) :
        ViewHolder(binding.root) {
        fun bind(passioAdvisorData: PassioAdvisorData) {
            binding.root.endMargin()
        }
    }

    inner class SenderTextViewHolder(val binding: ItemAdvisorSenderTextBinding) :
        ViewHolder(binding.root) {
        fun bind(passioAdvisorData: PassioAdvisorData) {
            with(binding)
            {
                root.startMargin()
                binding.tvMessage.text = passioAdvisorData.passioAdvisorSender?.textMessage
            }
        }
    }


    inner class SenderImageViewHolder(val binding: ItemAdvisorSenderImageBinding) :
        ViewHolder(binding.root) {
        fun bind(passioAdvisorData: PassioAdvisorData) {
            binding.root.startMargin()
            passioAdvisorData.passioAdvisorSender?.bitmaps?.let {
                binding.rvImages.layoutManager = GridLayoutManager(
                    binding.root.context,
                    if (it.size > 3) 2 else 1,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.rvImages.adapter = AdvisorImageAdapter(it)
            }
        }
    }

    inner class ReceiverTextViewHolder(val binding: ItemAdvisorReceiverTextBinding) :
        ViewHolder(binding.root) {
        fun bind(passioAdvisorData: PassioAdvisorData) {
            binding.root.endMargin()
//            binding.tvMessage.text = passioAdvisorData.passioAdvisorResponse?.markupContent
            binding.findFoods.isVisible = passioAdvisorData.isFindFoodEnabled()

//            val markwon = Markwon.builder(binding.root.context)
//                    .usePlugin(MarkwonPlugin.)
//                    .usePlugin(MarkwonHtmlPlugin.create())
//                .build()

            markwon?.setMarkdown(
                binding.tvMessage,
                passioAdvisorData.passioAdvisorResponse?.markupContent?.trimIndent() ?: ""
            )

            binding.findFoods.setOnClickListener {
                onFindFood.invoke(passioAdvisorData)
            }
        }
    }

    inner class ReceiverIngredientViewHolder(val binding: ItemAdvisorReceiverIngridientsBinding) :
        ViewHolder(binding.root) {
        fun bind(passioAdvisorData: PassioAdvisorData) {
            with(binding) {
                root.endMargin()
                markwon?.setMarkdown(
                    binding.tvMessage,
                    passioAdvisorData.getIngredientContent().trimIndent()
                )
                if (passioAdvisorData.selectedFoodIndexes.size > 0) {
                    logFood.enable()
                } else {
                    logFood.disable()
                }

                val foodResultAdapter = FoodImageResultAdapter(object : OnItemSelectChange {
                    override fun onItemSelectChange(selectedCount: Int) {
                        if (selectedCount > 0) {
                            logFood.enable()
                        } else {
                            logFood.disable()
                        }
                    }

                    override fun onIndexSelect(index: Int) {
                        passioAdvisorData.selectedFoodIndexes.add(index)
                    }

                    override fun onIndexDeselect(index: Int) {
                        passioAdvisorData.selectedFoodIndexes.remove(index)
                    }

                })
                rvResult.adapter = foodResultAdapter
                if (!passioAdvisorData.passioAdvisorResponse?.extractedIngredients.isNullOrEmpty()) {
                    foodResultAdapter.addData(
                        passioAdvisorData.passioAdvisorResponse?.extractedIngredients!!,
                        passioAdvisorData.selectedFoodIndexes,
                        passioAdvisorData.isLogged
                    )
                } else {
                    foodResultAdapter.resetAll()
                }

                logFood.isVisible = !passioAdvisorData.isLogged
                viewDiary.isVisible = passioAdvisorData.isLogged
                viewDiary.setOnClickListener {
                    onViewDiary.invoke(passioAdvisorData)
                }
                logFood.setOnClickListener {
                    onLogFood.invoke(passioAdvisorData)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = list[position]
        return item.dataItemType
//        return super.getItemViewType(position)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = when (viewType) {
            PassioAdvisorData.TYPE_PROCESSING -> {
                val binding =
                    ItemAdvisorProcessingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ProcessingViewHolder(binding)
            }

            PassioAdvisorData.TYPE_SENDER_TEXT -> {
                val binding =
                    ItemAdvisorSenderTextBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                SenderTextViewHolder(binding)
            }

            PassioAdvisorData.TYPE_SENDER_IMAGES -> {
                val binding =
                    ItemAdvisorSenderImageBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                SenderImageViewHolder(binding)
            }

            PassioAdvisorData.TYPE_RECEIVER_TEXT -> {
                val binding =
                    ItemAdvisorReceiverTextBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ReceiverTextViewHolder(binding)
            }

            PassioAdvisorData.TYPE_RECEIVER_INGREDIENT -> {
                val binding =
                    ItemAdvisorReceiverIngridientsBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ReceiverIngredientViewHolder(binding)
            }

            else -> {
                //PassioAdvisorData.TYPE_WELCOME_INSTRUCTION
                val binding =
                    ItemAdvisorWelcomeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                WelcomeViewHolder(binding)
            }
        }
        return viewHolder
    }

    private fun View.startMargin() {
        val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginStart = chatMarginSpace
        this.layoutParams = layoutParams
    }

    private fun View.endMargin() {
        val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginEnd = chatMarginSpace
        this.layoutParams = layoutParams
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (chatMarginSpace == 0) {
            chatMarginSpace = (DesignUtils.screenWidth(holder.itemView.context) * 0.10f).toInt()
        }
        if (markwon == null) {
            markwon = Markwon.builder(holder.itemView.context)
//                    .usePlugin(MarkwonPlugin.)
//                    .usePlugin(MarkwonHtmlPlugin.create())
                .build()
        }

        val item = list[position]

        when (holder) {
            is ProcessingViewHolder -> {
                holder.bind(item)
            }

            is WelcomeViewHolder -> {
                holder.bind(item)
            }

            is SenderTextViewHolder -> {
                holder.bind(item)
            }

            is SenderImageViewHolder -> {
                holder.bind(item)
            }

            is ReceiverTextViewHolder -> {
                holder.bind(item)
            }

            is ReceiverIngredientViewHolder -> {
                holder.bind(item)
            }
        }
    }

    override fun getItemCount() = list.size


}