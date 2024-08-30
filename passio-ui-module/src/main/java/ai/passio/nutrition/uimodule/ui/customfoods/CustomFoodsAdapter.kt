package ai.passio.nutrition.uimodule.ui.customfoods

import ai.passio.nutrition.uimodule.databinding.ItemCustomFoodBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CustomFoodsAdapter(
    private val onEdit: (customFood: FoodRecord) -> Unit,
    private val onLog: (customFood: FoodRecord) -> Unit,
) : RecyclerView.Adapter<CustomFoodsAdapter.CustomFoodViewHolder>() {

    private val customFoods = mutableListOf<FoodRecord>()

    fun updateItems(customFoods: List<FoodRecord>) {
        this.customFoods.clear()
        this.customFoods.addAll(customFoods)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): FoodRecord {
        return customFoods[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomFoodViewHolder {
        val binding = ItemCustomFoodBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomFoodViewHolder(binding)
    }

    override fun getItemCount(): Int = customFoods.size

    override fun onBindViewHolder(holder: CustomFoodViewHolder, position: Int) {
        holder.bindTo(customFoods[position])
    }

    inner class CustomFoodViewHolder(
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