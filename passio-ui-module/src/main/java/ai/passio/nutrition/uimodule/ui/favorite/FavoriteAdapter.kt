package ai.passio.nutrition.uimodule.ui.favorite

import ai.passio.nutrition.uimodule.databinding.ItemCustomFoodBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FavoriteAdapter(
    private val onEdit: (foodRecord: FoodRecord) -> Unit,
    private val onLog: (foodRecord: FoodRecord) -> Unit,
) : RecyclerView.Adapter<FavoriteAdapter.FoodViewHolder>() {

    private val foodRecords = mutableListOf<FoodRecord>()

    fun updateItems(foodRecords: List<FoodRecord>) {
        this.foodRecords.clear()
        this.foodRecords.addAll(foodRecords)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): FoodRecord {
        return foodRecords[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemCustomFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodViewHolder(binding)
    }

    override fun getItemCount(): Int = foodRecords.size

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bindTo(foodRecords[position])
    }

    inner class FoodViewHolder(
        private val binding: ItemCustomFoodBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(customFood: FoodRecord) {
            with(binding) {
                name.visibility = View.VISIBLE
                name.text = customFood.name.capitalized()

                image.loadFoodImage(customFood)

                if (customFood.additionalData.isNotEmpty()) {
                    servingSize.visibility = View.VISIBLE
                    servingSize.text = customFood.additionalData
                } else {
                    servingSize.visibility = View.GONE
                }

                plusIcon.setOnClickListener {
                    onLog.invoke(customFood)
                }
                root.setOnClickListener {
                    onEdit.invoke(customFood)
                }
            }
        }
    }
}