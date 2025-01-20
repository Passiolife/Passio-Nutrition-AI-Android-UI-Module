package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.model.ImageFoodResult
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
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

//    private val selectedItemPositions = mutableListOf<Int>()

    @SuppressLint("NotifyDataSetChanged")
    fun addData(
        newData: List<ImageFoodResult>,
//        selectedItemPositions: List<Int>,
    ) {
        list.clear()
        list.addAll(newData)
//        this.selectedItemPositions.clear()
//        this.selectedItemPositions.addAll(selectedItemPositions)
        onItemSelectChange.onItemSelectChange(list.count { it.isSelected })
//        onItemSelectChange.onItemSelectChange(selectedItemPositions.size)
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

                if (foodRecord.nutrients().calories() != null) {
                    val cal = foodRecord.nutrients().calories()?.value ?: 0.0
                    calValue.text = "${cal.roundToInt()}"
                    calUnit.text = "cal"
                } else {
                    calValue.text = " - "
                    calUnit.text = " - "
                }

                if (foodRecord.nutrients().fat() != null) {
                    fatValue.text = "${foodRecord.nutrients().fat()?.value?.singleDecimal() ?: 0.0}"
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

                foodSelect.isEnabled = true
//                if (selectedItemPositions.contains(adapterPosition)) {
//                    foodSelect.setImageResource(R.drawable.radio_on)
//                } else {
//                    foodSelect.setImageResource(R.drawable.radio_off)
//                }
                if (imageFoodResult.isSelected) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }

                if (imageFoodResult.isBarcodeDataMissing() || imageFoodResult.isNutritionFactsDataMissing()) {
                    mainItem.setBackgroundResource(R.drawable.rc_8_red)
                } else {
                    mainItem.setBackgroundResource(R.drawable.rc_8_white)
                }
                root.setOnClickListener {

                    onItemSelectChange.onTapped(adapterPosition, imageFoodResult)

                }
                foodSelect.setOnClickListener {
                    if (imageFoodResult.isBarcodeDataMissing() || imageFoodResult.isNutritionFactsDataMissing())
                        return@setOnClickListener

                    imageFoodResult.isSelected = !imageFoodResult.isSelected
//                    val id = adapterPosition
//                    if (selectedItemPositions.contains(id)) {
//                        onItemSelectChange.onIndexDeselect(id)
//                        selectedItemPositions.remove(id)
//                    } else {
//                        onItemSelectChange.onIndexSelect(id)
//                        selectedItemPositions.add(id)
//                    }
//                    onItemSelectChange.onItemSelectChange(selectedItemPositions.size)
                    onItemSelectChange.onItemSelectChange(list.count { it.isSelected })
                    notifyItemChanged(adapterPosition)
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

    /*fun getSelectedItems(): List<PassioAdvisorFoodInfo> {
        return list.filter { selectedItems.contains(it.foodDataInfo?.resultId) }
    }*/
    fun getSelectedItems(): List<ImageFoodResult> {
        return list.filter { it.isSelected }
//        return list.filterIndexed { index, _ -> index in selectedItemPositions }
    }


}