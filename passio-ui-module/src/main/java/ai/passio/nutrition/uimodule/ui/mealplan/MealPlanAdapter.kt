package ai.passio.nutrition.uimodule.ui.mealplan

import ai.passio.nutrition.uimodule.databinding.ItemMealplanFoodBinding
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.data.model.PassioMealPlanItem
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class MealPlanAdapter(
    private val onLogAdd: (foodRecord: PassioMealPlanItem) -> Unit,
    private val onLogEdit: (foodRecord: PassioMealPlanItem) -> Unit
) : RecyclerView.Adapter<MealPlanAdapter.MealPlanViewHolder>() {

    private val records = mutableListOf<PassioMealPlanItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateLogs(records: List<PassioMealPlanItem>) {
        this.records.clear()
        this.records.addAll(records)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealPlanViewHolder {

        val binding =
            ItemMealplanFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MealPlanViewHolder(binding)
    }

    override fun getItemCount(): Int = records.size

    override fun onBindViewHolder(holder: MealPlanViewHolder, position: Int) {
        holder.bindTo(records[position])
    }

    inner class MealPlanViewHolder(
        private val binding: ItemMealplanFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindTo(foodRecordData: PassioMealPlanItem) {
            val foodRecord = foodRecordData.meal
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


                addFood.setOnClickListener {
                    onLogAdd.invoke(foodRecordData)
                }
                root.setOnClickListener {
                    onLogEdit.invoke(foodRecordData)
                }
            }
        }
    }
}