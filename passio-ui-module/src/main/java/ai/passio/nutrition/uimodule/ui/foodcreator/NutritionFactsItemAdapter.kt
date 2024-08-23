package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.databinding.ItemNutritionFactsBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class NutritionFactsItemAdapter(
    private var nutritionFactsItems: List<NutritionFactsItem>,
    private var showRemove: Boolean,
    private val onItemRemoved: (NutritionFactsItem) -> Unit
) :
    RecyclerView.Adapter<NutritionFactsItemAdapter.NutritionFactsItemViewHolder>() {

    inner class NutritionFactsItemViewHolder(val binding: ItemNutritionFactsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): NutritionFactsItemViewHolder {
        val binding =
            ItemNutritionFactsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NutritionFactsItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutritionFactsItemViewHolder, position: Int) {
        with(holder.binding) {
            val nutritionFactsItem = nutritionFactsItems[position]
            lblName.text = nutritionFactsItem.nutrientName
            lblValue.setText(if (nutritionFactsItem.value == 0.0) "" else nutritionFactsItem.value.toString())
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    nutritionFactsItem.value = s.toString().toDoubleOrNull() ?: 0.0
                }

            }
            if (lblValue.tag != null && lblValue.tag is TextWatcher) {
                lblValue.removeTextChangedListener(lblValue.tag as TextWatcher)
            }
            lblValue.tag = textWatcher
            lblValue.addTextChangedListener(textWatcher)
            lblUnit.text = nutritionFactsItem.unitSymbol
            ivRemove.isVisible = showRemove
            ivRemove.setOnClickListener {
                onItemRemoved.invoke(nutritionFactsItem)
            }
        }
    }

    override fun getItemCount() = nutritionFactsItems.size

}