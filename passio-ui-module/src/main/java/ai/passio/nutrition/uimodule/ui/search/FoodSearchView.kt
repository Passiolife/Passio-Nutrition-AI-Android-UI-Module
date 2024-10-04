package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FoodSearchLayoutBinding
import ai.passio.nutrition.uimodule.ui.model.FoodRecord
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.util.StringKT.isValid
import ai.passio.nutrition.uimodule.ui.util.ViewEXT.showKeyboard
import ai.passio.nutrition.uimodule.ui.view.VerticalSpaceItemDecoration
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val INPUT_DELAY = 450L

class FoodSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface PassioSearchListener {
        fun onQueryChange(query: String)
        fun onFoodItemSelected(searchItem: PassioFoodDataInfo)
        fun onFoodItemLog(searchItem: PassioFoodDataInfo)
        fun onFoodItemSelected(searchItem: FoodRecord)
        fun onFoodItemLog(searchItem: FoodRecord)
        fun onTextCleared()
        fun onViewDismissed()
    }

    private val foodSearchAdapterListener = object : FoodSearchAdapterListener {
        override fun onFoodClicked(searchResult: PassioFoodDataInfo) {
            listener?.onFoodItemSelected(searchResult)
        }

        override fun onFoodClicked(searchResult: FoodRecord) {
            listener?.onFoodItemSelected(searchResult)
        }

        override fun onFoodAdd(searchResult: PassioFoodDataInfo) {
            listener?.onFoodItemLog(searchResult)
        }

        override fun onFoodAdd(searchResult: FoodRecord) {
            listener?.onFoodItemLog(searchResult)
        }

    }

    private var binding: FoodSearchLayoutBinding? = null
    private var listener: PassioSearchListener? = null
    private val searchAdapter = FoodItemSearchAdapter(foodSearchAdapterListener)
    private val myFoodSearchAdapter = FoodItemSearchAdapter(foodSearchAdapterListener)
    private val suggestionAdapter = FoodSuggestionsAdapter(::onSuggestion)

    private var searchTerm = ""


    init {
        binding = FoodSearchLayoutBinding.inflate(LayoutInflater.from(context), this)
        orientation = VERTICAL
        background = ContextCompat.getDrawable(context, R.drawable.rc_8_white)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (binding == null) {
            return
        }
        setupSearchBar()
        with(binding!!) {
            rvPassioFoods.addItemDecoration(VerticalSpaceItemDecoration(DesignUtils.dp2px(8f)))
            rvPassioFoods.adapter = searchAdapter
            rvMyFoods.addItemDecoration(VerticalSpaceItemDecoration(DesignUtils.dp2px(8f)))
            rvMyFoods.adapter = myFoodSearchAdapter
            searchSuggestionRecycler.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            searchSuggestionRecycler.adapter = suggestionAdapter
        }
    }

    fun setup(searchViewListener: PassioSearchListener) {
        this.listener = searchViewListener
        binding?.searchExit?.setOnClickListener {
            listener?.onViewDismissed()
        }
    }

    fun updateSearchResult(
        query: String,
        results: List<PassioFoodDataInfo>,
        suggestions: List<String>,
        myFoods: List<FoodRecord>
    ) {
        if (this.isAttachedToWindow && this.context != null && query == searchTerm) {
            binding?.viewProgress?.isVisible = false
            binding?.lblMyFoods?.isVisible = myFoods.isNotEmpty()
            binding?.lblPassioFoods?.isVisible = results.isNotEmpty()
            myFoodSearchAdapter.updateMyItems(myFoods)
            searchAdapter.updateItems(results)
            suggestionAdapter.updateSuggestions(suggestions)
        }
    }

    private fun setupSearchBar() {
        if (binding == null) return
        with(binding!!) {
            clearText.setOnClickListener {
                searchEditText.setText(StringBuilder().toString())
            }
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    //                    if (s.isEmpty()) {
                    //                        searchClear.visibility = View.GONE
                    //                    } else {
                    //                        searchClear.visibility = View.VISIBLE
                    //                    }

                    searchTerm = s.toString()
                    handler.postDelayed(inputFinishCheck, INPUT_DELAY)
                    clearText.isVisible = searchTerm.isValid()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    onTextChanged()
                }
            })

            searchEditText.showKeyboard()

            //            searchClear.setOnClickListener {
            //                searchEditText.text.clear()
            //                listener.onTextCleared()
            //            }
        }
    }

    private val inputFinishCheck = Runnable {
        val currentQuery = searchTerm
        binding?.viewKeepTyping?.isVisible =
            currentQuery.isNotEmpty() && currentQuery.trim().length < 3

        if (currentQuery.isEmpty() || currentQuery.length < 3) {
            myFoodSearchAdapter.updateItems(listOf())
            searchAdapter.updateItems(listOf())
            suggestionAdapter.updateSuggestions(listOf())
            binding?.lblMyFoods?.isVisible = false
            binding?.lblPassioFoods?.isVisible = false
            binding?.viewProgress?.isVisible = false
            return@Runnable
        }


        binding?.viewProgress?.isVisible = true
        listener?.onQueryChange(currentQuery)
    }

    fun onTextChanged() {
        handler.removeCallbacks(inputFinishCheck)
    }

    private fun onSuggestion(suggestion: String) {
        myFoodSearchAdapter.updateItems(listOf())
        searchAdapter.updateItems(listOf())
        suggestionAdapter.updateSuggestions(listOf())
        binding?.let {
            binding?.lblMyFoods?.isVisible = false
            binding?.lblPassioFoods?.isVisible = false
            it.searchEditText.setText(suggestion)
            it.searchEditText.setSelection(suggestion.length)
        }
    }

    override fun onDetachedFromWindow() {
        binding = null
        super.onDetachedFromWindow()
    }
}