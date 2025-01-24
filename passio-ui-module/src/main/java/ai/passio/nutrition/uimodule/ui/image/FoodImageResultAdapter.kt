package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.model.PassioFoodResultType
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt


interface OnFoodImageSelectChange {
    fun onItemSelectChange(selectedCount: Int)
    fun onTapped(editIndex: Int, imageFoodResult: ImageFoodResult)
}

class FoodImageResultAdapter(
    private val onItemSelectChange: OnFoodImageSelectChange
) :
    RecyclerView.Adapter<FoodImageResultAdapter.ImageViewHolder>() {

    private val list = mutableListOf<ImageFoodResult>()

    @SuppressLint("NotifyDataSetChanged")
    fun addData(
        newData: List<ImageFoodResult>,
    ) {
        list.clear()
        list.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(val binding: ItemImageFoodResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(imageFoodResult: ImageFoodResult) {

            val foodRecord = imageFoodResult.record
            with(binding) {
                image.loadFoodImage(foodRecord)
                name.text = foodRecord.name.capitalized()
                if (imageFoodResult.resultType == PassioFoodResultType.BARCODE && !foodRecord.name.isValid()) {
                    name.text = "Barcode Not Found"
                }

                if (imageFoodResult.isBarcodeDataMissing() || imageFoodResult.isNutritionFactsDataMissing()) {
                    mainItem.setBackgroundResource(R.drawable.rc_8_rose)
                    servingSize.text = "item will not be logged, edit data manually"
                    foodSelect.setImageResource(R.drawable.ic_edit)
                    llData.isVisible = false
                } else {
                    llData.isVisible = true
                    mainItem.setBackgroundResource(R.drawable.rc_8_white)

                    if (foodRecord.nutrients().calories() != null) {
                        val cal = foodRecord.nutrients().calories()?.value ?: 0.0
                        calValue.text = "${cal.roundToInt()}"
                        calUnit.text = "cal"
                    } else {
                        calValue.text = " - "
                        calUnit.text = " - "
                    }

                    if (foodRecord.nutrients().fat() != null) {
                        fatValue.text =
                            "${foodRecord.nutrients().fat()?.value?.singleDecimal() ?: 0.0}"
                        fatUnit.text = foodRecord.nutrients().fat()?.unit?.symbol ?: "g"
                    } else {
                        fatValue.text = " - "
                        fatUnit.text = " - "
                    }

                    if (foodRecord.nutrients().protein() != null) {
                        proteinValue.text =
                            "${foodRecord.nutrients().protein()?.value?.singleDecimal() ?: 0.0}"
                        proteinUnit.text = foodRecord.nutrients().protein()?.unit?.symbol ?: "g"
                    } else {
                        proteinValue.text = " - "
                        proteinUnit.text = " - "
                    }

                    if (foodRecord.nutrients().carbs() != null) {
                        carbsValue.text =
                            "${foodRecord.nutrients().carbs()?.value?.singleDecimal() ?: 0.0}"
                        carbsUnit.text = foodRecord.nutrients().carbs()?.unit?.symbol ?: "g"
                    } else {
                        carbsValue.text = " - "
                        carbsUnit.text = " - "
                    }

                    servingSize.text =
                        "${foodRecord.selectedQuantity.singleDecimal()} ${foodRecord.selectedUnit} (${
                            foodRecord.nutrients().weight.gramsValue().singleDecimal()
                        } ${Grams.unitName})"
                }

                if (imageFoodResult.isSelected) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }

                root.setOnClickListener {
                    onItemSelectChange.onTapped(adapterPosition, imageFoodResult)
                }
                foodSelect.setOnClickListener {
                    if (imageFoodResult.isBarcodeDataMissing() || imageFoodResult.isNutritionFactsDataMissing()) {
                        onItemSelectChange.onTapped(adapterPosition, imageFoodResult)
                    } else {
                        imageFoodResult.isSelected = !imageFoodResult.isSelected
                        onItemSelectChange.onItemSelectChange(list.count { it.isSelected })
                        notifyItemChanged(adapterPosition)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ItemImageFoodResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

}