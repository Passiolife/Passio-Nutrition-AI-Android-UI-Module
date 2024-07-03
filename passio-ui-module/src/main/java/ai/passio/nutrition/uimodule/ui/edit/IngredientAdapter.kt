package ai.passio.nutrition.uimodule.ui.edit

import ai.passio.nutrition.uimodule.databinding.IngredientLayoutBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecordIngredient
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
        val binding = IngredientLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IngredientViewHolder(binding)
    }

    override fun getItemCount(): Int = ingredients.size

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bindTo(ingredients[position], position)
    }

    inner class IngredientViewHolder(
        private val binding: IngredientLayoutBinding
    ) : ViewHolder(binding.root) {

        fun bindTo(ingredient: FoodRecordIngredient, position: Int) {
            with(binding) {
                image.loadPassioIcon(ingredient.iconId)
                name.text = ingredient.name.capitalize()
                val cal = ingredient.nutrientsSelectedSize().calories()?.value?.roundToInt() ?: 0
                calories.text = "$cal cal"

                root.setOnClickListener {
                    onIngredientSelected(position)
                }
            }
        }
    }
}