package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.databinding.SearchItemLayoutBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.StringKT.capitalized
import ai.passio.nutrition.uimodule.ui.util.loadFoodImage
import ai.passio.nutrition.uimodule.ui.util.loadPassioIcon
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import ai.passio.passiosdk.passiofood.data.model.PassioIDEntityType
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

interface FoodSearchAdapterListener {
    fun onFoodClicked(searchResult: PassioFoodDataInfo)
    fun onFoodClicked(searchResult: FoodRecord)
    fun onFoodAdd(searchResult: PassioFoodDataInfo)
    fun onFoodAdd(searchResult: FoodRecord)
}

class FoodItemSearchAdapter(private val foodSearchListener: FoodSearchAdapterListener) :
    RecyclerView.Adapter<FoodItemSearchAdapter.FoodItemSearchViewHolder>() {

    private val searchResults = mutableListOf<PassioFoodDataInfo>()
    private val searchMyFoodsResults = mutableListOf<FoodRecord>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(searchResults: List<PassioFoodDataInfo>) {
        this.searchMyFoodsResults.clear()
        this.searchResults.clear()
        this.searchResults.addAll(searchResults)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMyItems(searchMyFoodsResults: List<FoodRecord>) {
        this.searchResults.clear()
        this.searchMyFoodsResults.clear()
        this.searchMyFoodsResults.addAll(searchMyFoodsResults)
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

    override fun getItemCount(): Int =
        if (searchResults.isNotEmpty())
            searchResults.size
        else
            searchMyFoodsResults.size


    override fun onBindViewHolder(holder: FoodItemSearchViewHolder, position: Int) {
        if (searchResults.isNotEmpty()) {
            holder.bindTo(searchResults[position])
        } else if (searchMyFoodsResults.isNotEmpty()) {
            holder.bindTo(searchMyFoodsResults[position])
        }
    }

    inner class FoodItemSearchViewHolder(
        private val searchResultBinding: SearchItemLayoutBinding,
    ) : RecyclerView.ViewHolder(searchResultBinding.root) {

        fun bindTo(searchResult: PassioFoodDataInfo) {
            with(searchResultBinding) {
                name.visibility = View.VISIBLE
                name.text = searchResult.foodName.capitalized()
                Log.d("searchResult===", searchResult.toString())
                if (searchResult.type.equals("recipe", true)) {
                    ivSymbol.isVisible = true
                    ivSymbol.loadPassioIcon("", PassioIDEntityType.recipe)
                } else {
                    ivSymbol.isVisible = false
                }

                image.loadPassioIcon(searchResult.iconID)

                if (searchResult.brandName.isNotEmpty()) {
                    servingSize.visibility = View.VISIBLE
                    servingSize.text = searchResult.brandName
                } else {
                    servingSize.visibility = View.GONE
                }

                plusIcon.setOnClickListener {
                    foodSearchListener.onFoodAdd(searchResult)
                }
                root.setOnClickListener {
                    foodSearchListener.onFoodClicked(searchResult)
                }
            }
        }

        fun bindTo(searchResult: FoodRecord) {
            with(searchResultBinding) {
                name.visibility = View.VISIBLE
                name.text = searchResult.name.capitalized()
                if (searchResult.isRecipe()) {
                    ivSymbol.isVisible = true
                    ivSymbol.loadPassioIcon("", PassioIDEntityType.recipe)
                } else {
                    ivSymbol.isVisible = false
                }

                image.loadFoodImage(searchResult)

                if (searchResult.additionalData.isNotEmpty()) {
                    servingSize.visibility = View.VISIBLE
                    servingSize.text = searchResult.additionalData
                } else {
                    servingSize.visibility = View.GONE
                }

                plusIcon.setOnClickListener {
                    foodSearchListener.onFoodAdd(searchResult)
                }
                root.setOnClickListener {
                    foodSearchListener.onFoodClicked(searchResult)
                }
            }
        }
    }
}