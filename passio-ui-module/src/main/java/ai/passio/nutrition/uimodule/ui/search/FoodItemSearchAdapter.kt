package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.databinding.SearchItemLayoutBinding
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FoodItemSearchAdapter(
    private val onClick: (searchResult: PassioFoodDataInfo) -> Unit,
) : RecyclerView.Adapter<FoodItemSearchAdapter.FoodItemSearchViewHolder>() {

    private val searchResults = mutableListOf<PassioFoodDataInfo>()

    fun updateItems(searchResults: List<PassioFoodDataInfo>) {
        this.searchResults.clear()
        this.searchResults.addAll(searchResults)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemSearchViewHolder {
        val binding = SearchItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FoodItemSearchViewHolder(binding)
    }

    override fun getItemCount(): Int = searchResults.size

    override fun onBindViewHolder(holder: FoodItemSearchViewHolder, position: Int) {
        holder.bindTo(searchResults[position])
    }

    inner class FoodItemSearchViewHolder(
        private val searchResultBinding: SearchItemLayoutBinding,
    ) : RecyclerView.ViewHolder(searchResultBinding.root) {

        fun bindTo(searchResult: PassioFoodDataInfo) {
            with(searchResultBinding) {
                name.visibility = View.VISIBLE
                name.text = searchResult.foodName.capitalize()

                image.loadPassioIcon(searchResult.iconID)

                if (searchResult.brandName.isNotEmpty()) {
                    servingSize.visibility = View.VISIBLE
                    servingSize.text = searchResult.brandName
                } else {
                    servingSize.visibility = View.GONE
                }

                this.root.setOnClickListener {
                    onClick(searchResult)
                }
            }
        }
    }
}