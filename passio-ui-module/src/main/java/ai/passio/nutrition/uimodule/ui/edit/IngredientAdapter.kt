package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.databinding.FoodLogBodyLayoutBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.StringKT.singleDecimal
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlin.math.roundToInt

class IngredientAdapter(
    private val onIngredientSelected: (index: Int) -> Unit
) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    private val ingredients = mutableListOf<FoodRecordIngredient>()

    fun updateIngredients(ingredients: List<FoodRecordIngredient>) {
        this.ingredients.clear()
        this.ingredients.addAll(ingredients)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = FoodLogBodyLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun getItemCount(): Int = ingredients.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bindTo(ingredients[position])
    }

    inner class IngredientViewHolder(
        private val binding: FoodLogBodyLayoutBinding
    ) : ViewHolder(binding.root) {

        fun bindTo(ingredient: FoodRecordIngredient) {
            with(binding) {
                image.loadPassioIcon(ingredient.iconId)
                name.text = ingredient.name.capitalized()
                val cal = ingredient.nutrientsSelectedSize().calories()?.value?.roundToInt() ?: 0
                calories.text = "$cal cal"

                val quantity = ingredient.selectedQuantity.singleDecimal()
                val selectedUnit = ingredient.selectedUnit
                val weight = ingredient.servingWeight().gramsValue()
                servingSize.text = "$quantity $selectedUnit (${weight.roundToInt()}g)"


                root.setOnClickListener {
                    onIngredientSelected(adapterPosition)
                }
            }
        }
    }
}