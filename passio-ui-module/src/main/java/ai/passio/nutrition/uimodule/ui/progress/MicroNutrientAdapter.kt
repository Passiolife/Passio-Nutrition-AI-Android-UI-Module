package ai.passio.nutrition.uimodule.ui.progress

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemMicrosProgressBinding
import ai.passio.nutrition.uimodule.databinding.ItemShowMoreBinding
import ai.passio.nutrition.uimodule.ui.model.MicroNutrient
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlin.math.roundToInt

class MicroNutrientAdapter(
    private val microNutrients: ArrayList<MicroNutrient>,
    private val onShowMoreClicked: (microNutrient: MicroNutrient) -> Unit,
) :
    RecyclerView.Adapter<ViewHolder>() {

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(microNutrients: ArrayList<MicroNutrient>)
        {
            this.microNutrients.clear()
            this.microNutrients.addAll(microNutrients)
            notifyDataSetChanged()
        }

    inner class ShowMoreViewHolder(val binding: ItemShowMoreBinding) :
        ViewHolder(binding.root) {
        fun bind(microNutrient: MicroNutrient) {
            with(binding) {
                showMore.text = microNutrient.name
                showMore.setOnClickListener {
                    onShowMoreClicked.invoke(microNutrient)
                }
            }
        }
    }

    inner class MicroNutrientsViewHolder(val binding: ItemMicrosProgressBinding) :
        ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(microNutrient: MicroNutrient) {
            with(binding) {
                val dp8 = DesignUtils.dp2px(8f)
                val dp32 = DesignUtils.dp2px(32f)
                if (adapterPosition == itemCount - 1) {
                    binding.root.setPadding(dp8, dp8, dp8, dp32)
                } else {
                    binding.root.setPadding(dp8, dp8, dp8, dp8)
                }
                label.text = microNutrient.name.capitalized()
                weight.text = "${microNutrient.value.roundToInt()} ${microNutrient.unitSymbol}"
                progress.progress =
                    ((microNutrient.value * 100) / microNutrient.recommendedValue).toInt()

                val colorState: ColorStateList =
                    if (microNutrient.value < microNutrient.recommendedValue) {
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                root.context,
                                R.color.passio_primary
                            )
                        )
                    } else {
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                root.context,
                                R.color.passio_red500
                            )
                        )
                    }

                weight.setTextColor(colorState)
                progress.progressTintList = colorState
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (microNutrients[position].unitSymbol == "showmore") {
            0
        } else {
            1
        }
//        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if (viewType == 0) {
            return ShowMoreViewHolder(
                ItemShowMoreBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return MicroNutrientsViewHolder(
                ItemMicrosProgressBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return microNutrients.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is MicroNutrientsViewHolder) {
            holder.bind(microNutrients[position])
        } else if (holder is ShowMoreViewHolder) {
            holder.bind(microNutrients[position])
        }
    }
}