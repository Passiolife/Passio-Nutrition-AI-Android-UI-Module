package ai.passio.nutrition.uimodule.ui.voice

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemImageFoodResultBinding
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.data.measurement.Grams
import ai.passio.passiosdk.passiofood.data.model.PassioSpeechRecognitionModel
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

internal class SpeechRecognitionAdapter(private val onItemSelectChange: (selectedCount: Int) -> Unit) :
    RecyclerView.Adapter<SpeechRecognitionAdapter.ImageViewHolder>() {

    private val list = mutableListOf<PassioSpeechRecognitionModel>()

    private val selectedItemPositions = mutableListOf<Int>()

    @SuppressLint("NotifyDataSetChanged")
    fun addData(newData: List<PassioSpeechRecognitionModel>, selectedItemPositions: List<Int>) {
        this.selectedItemPositions.clear()
        this.selectedItemPositions.addAll(selectedItemPositions)
        list.clear()
        list.addAll(newData)
        onItemSelectChange.invoke(selectedItemPositions.size)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        selectedItemPositions.clear()
        onItemSelectChange.invoke(selectedItemPositions.size)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(val binding: ItemImageFoodResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(foodInfo: PassioSpeechRecognitionModel) {
            val foodRecord = foodInfo.advisorInfo.foodDataInfo!!
            val nutritionPreview = foodInfo.advisorInfo.foodDataInfo!!.nutritionPreview

            with(binding) {

                image.loadPassioIcon(foodRecord.iconID)
                name.text = foodRecord.foodName.capitalized()

                val ratio = nutritionPreview.calories / nutritionPreview.weightQuantity
                val caloriesVal = ratio * foodInfo.advisorInfo.weightGrams

//                val cal = foodRecord.nutritionPreview.calories
//                calories.text = "$cal Cal"
                calories.text = "${caloriesVal.singleDecimal()} Cal"
                servingSize.text =
                    "${foodInfo.advisorInfo.weightGrams.roundToInt()} ${Grams.unitName}"

                /*val quantity = foodRecord.nutritionPreview.servingQuantity
                val selectedUnit = foodRecord.nutritionPreview.servingUnit
                val weight = foodRecord.nutritionPreview.weightQuantity
                val weightUnit = foodRecord.nutritionPreview.weightUnit
                servingSize.text =
                    "$quantity ${selectedUnit.capitalized()} (${weight.roundToInt()} $weightUnit)"*/

                if (selectedItemPositions.contains(adapterPosition)) {
                    foodSelect.setImageResource(R.drawable.radio_on)
                } else {
                    foodSelect.setImageResource(R.drawable.radio_off)
                }

                root.setOnClickListener {
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

    fun getSelectedItems(): List<PassioSpeechRecognitionModel> {
        return list.filterIndexed { index, _ -> index in selectedItemPositions }
    }

}