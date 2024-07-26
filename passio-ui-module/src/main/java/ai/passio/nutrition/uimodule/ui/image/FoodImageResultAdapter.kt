package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.data.model.PassioAdvisorFoodInfo
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

internal class FoodImageResultAdapter(private val onItemSelectChange: (selectedCount: Int) -> Unit) :
    RecyclerView.Adapter<FoodImageResultAdapter.ImageViewHolder>() {

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
    fun addData(newData: List<PassioAdvisorFoodInfo>) {
        list.clear()
        list.addAll(newData)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(val binding: ItemImageFoodResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(foodInfo: PassioAdvisorFoodInfo) {
            val foodRecord = foodInfo.foodDataInfo!!

            with(binding) {
                image.loadPassioIcon(foodRecord.iconID)
                name.text = foodRecord.foodName.capitalized()
                val cal = foodRecord.nutritionPreview.calories
                calories.text = "$cal Cal"

                val quantity = foodRecord.nutritionPreview.servingQuantity
                val selectedUnit = foodRecord.nutritionPreview.servingUnit
                val weight = foodRecord.nutritionPreview.weightQuantity
                val weightUnit = foodRecord.nutritionPreview.weightUnit
                servingSize.text =
                    "$quantity ${selectedUnit.capitalized()} (${weight.roundToInt()} $weightUnit)"

                if (selectedItemPositions.contains(adapterPosition)) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }
                /*if (selectedItems.contains(foodRecord.resultId)) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }*/

                root.setOnClickListener {
                    /*val id = foodRecord.resultId
                    if (selectedItems.contains(id)) {
                        selectedItems.remove(id)
                    } else {
                        selectedItems.add(id)
                    }*/
                    val id = adapterPosition
                    if (selectedItemPositions.contains(id)) {
                        selectedItemPositions.remove(id)
                    } else {
                        selectedItemPositions.add(id)
                    }
                    onItemSelectChange.invoke(selectedItemPositions.size)
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