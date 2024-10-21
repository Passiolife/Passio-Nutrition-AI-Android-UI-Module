package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

internal interface OnItemSelectChange {
    fun onItemSelectChange(selectedCount: Int)
    fun onIndexSelect(index: Int)
    fun onIndexDeselect(index: Int)
}

internal class FoodImageResultAdapter(private val onItemSelectChange: OnItemSelectChange) :
    RecyclerView.Adapter<FoodImageResultAdapter.ImageViewHolder>() {

    private var isLogged = false
    private val list = mutableListOf<PassioAdvisorFoodInfo>()

    //    private val selectedItems = mutableListOf<String>()
    private val selectedItemPositions = mutableListOf<Int>()

    @SuppressLint("NotifyDataSetChanged")
    fun resetAll() {
        list.clear()
        selectedItemPositions.clear()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(
        newData: List<PassioAdvisorFoodInfo>,
        selectedItemPositions: List<Int>,
    ) {
        list.clear()
        list.addAll(newData)
        this.selectedItemPositions.clear()
        this.selectedItemPositions.addAll(selectedItemPositions)
        onItemSelectChange.onItemSelectChange(selectedItemPositions.size)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(
        newData: List<PassioAdvisorFoodInfo>,
        selectedItemPositions: List<Int>,
        isLogged: Boolean
    ) {
        this.isLogged = isLogged
        list.clear()
        list.addAll(newData)
        this.selectedItemPositions.clear()
        this.selectedItemPositions.addAll(selectedItemPositions)
        onItemSelectChange.onItemSelectChange(selectedItemPositions.size)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(val binding: ItemImageFoodResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(foodInfo: PassioAdvisorFoodInfo) {
            val packagedFoodItem = foodInfo.packagedFoodItem
            val advisorInfo = foodInfo.foodDataInfo

            with(binding) {

                if (packagedFoodItem != null) {
                    val foodRecord = FoodRecord(packagedFoodItem)
                    image.loadPassioIcon(foodRecord.iconId)
                    name.text =
                        if (foodRecord.name.isValid()) foodRecord.name.capitalized() else "Nutrition Facts Label"
                    val cal = foodRecord.nutrients().calories()?.value ?: 0.0
                    calories.text = "${cal.singleDecimal()} Cal"
                    servingSize.text =
                        "${
                            foodRecord.nutrients().weight.gramsValue().roundToInt()
                        } ${Grams.unitName}"
                } else if (advisorInfo != null) {
                    val nutritionPreview = advisorInfo.nutritionPreview

                    image.loadPassioIcon(advisorInfo.iconID)
                    name.text = advisorInfo.foodName.capitalized()

                    val ratio = nutritionPreview.calories / nutritionPreview.weightQuantity
                    val caloriesVal = ratio * foodInfo.weightGrams

                    calories.text = "${caloriesVal.singleDecimal()} Cal"
                    servingSize.text =
                        "${foodInfo.weightGrams.roundToInt()} ${Grams.unitName}"
                }

                foodSelect.isEnabled = true
                if (isLogged) {
                    foodSelect.isEnabled = false
                    if (selectedItemPositions.contains(adapterPosition)) {
                        foodSelect.setImageResource(R.drawable.ic_mark_correct)
                    } else {
                        foodSelect.setImageResource(R.drawable.ic_mark_incorrect)
                    }
                } else if (selectedItemPositions.contains(adapterPosition)) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }


                root.setOnClickListener {
                    if (isLogged) {
                        return@setOnClickListener
                    }
                    val id = adapterPosition
                    if (selectedItemPositions.contains(id)) {
                        onItemSelectChange.onIndexDeselect(id)
                        selectedItemPositions.remove(id)
                    } else {
                        onItemSelectChange.onIndexSelect(id)
                        selectedItemPositions.add(id)
                    }
                    onItemSelectChange.onItemSelectChange(selectedItemPositions.size)
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
    fun getSelectedItems(): List<PassioAdvisorFoodInfo> {
        return list.filterIndexed { index, _ -> index in selectedItemPositions }
    }


}