package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.advisor.OnItemSelectChange
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class FoodImageResultAdapter(
    private val onItemSelectChange: OnItemSelectChange,
    private val onEdit: (editIndex: Int, foodRecord: FoodRecord) -> Unit
) :
    RecyclerView.Adapter<FoodImageResultAdapter.ImageViewHolder>() {

    private val list = mutableListOf<FoodRecord>()

    private val selectedItemPositions = mutableListOf<Int>()

    @SuppressLint("NotifyDataSetChanged")
    fun addData(
        newData: List<FoodRecord>,
        selectedItemPositions: List<Int>,
    ) {
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
        fun bind(foodRecord: FoodRecord) {

            with(binding) {
                image.loadPassioIcon(foodRecord.iconId)
                name.text =
                    if (foodRecord.name.isValid()) foodRecord.name.capitalized() else "Nutrition Facts Label"
                val cal = foodRecord.nutrients().calories()?.value ?: 0.0

                calValue.text = "${cal.roundToInt()}"
                calUnit.text = "cal"

                fatValue.text = "${foodRecord.nutrients().fat()?.value?.singleDecimal() ?: 0.0}"
                fatUnit.text = foodRecord.nutrients().fat()?.unit?.symbol ?: "g"

                proteinValue.text =
                    "${foodRecord.nutrients().protein()?.value?.singleDecimal() ?: 0.0}"
                proteinUnit.text = foodRecord.nutrients().protein()?.unit?.symbol ?: "g"

                carbsValue.text =
                    "${foodRecord.nutrients().carbs()?.value?.singleDecimal() ?: 0.0}"
                carbsUnit.text = foodRecord.nutrients().carbs()?.unit?.symbol ?: "g"

                servingSize.text =
                    "${
                        foodRecord.nutrients().weight.gramsValue().singleDecimal()
                    } ${Grams.unitName}"

                foodSelect.isEnabled = true
                if (selectedItemPositions.contains(adapterPosition)) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }

                name.setOnClickListener {
                    onEdit.invoke(adapterPosition, foodRecord)
                }

                root.setOnClickListener {
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
    fun getSelectedItems(): List<FoodRecord> {
        return list.filterIndexed { index, _ -> index in selectedItemPositions }
    }


}