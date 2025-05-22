package ai.passio.nutrition.uimodule.ui.nutritioninfo

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemNutritionInfoBinding
import ai.passio.nutrition.uimodule.ui.model.MicroNutrient
import ai.passio.nutrition.uimodule.ui.nutritioninfo.NutritionInfoAdapter.MicroNutrientsViewHolder
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class NutritionInfoAdapter(
    private val microNutrients: List<MicroNutrient>
) :
    RecyclerView.Adapter<MicroNutrientsViewHolder>() {


    inner class MicroNutrientsViewHolder(val binding: ItemNutritionInfoBinding) :
        ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(microNutrient: MicroNutrient) {
            with(binding) {
                label.text = microNutrient.name.capitalized()
                weight.text = "${microNutrient.value.singleDecimal()}"
                weightUnit.text = microNutrient.unitSymbol

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
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MicroNutrientsViewHolder {
        return MicroNutrientsViewHolder(
            ItemNutritionInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return microNutrients.size
    }

    override fun onBindViewHolder(holder: MicroNutrientsViewHolder, position: Int) {
        holder.bind(microNutrients[position])
    }
}