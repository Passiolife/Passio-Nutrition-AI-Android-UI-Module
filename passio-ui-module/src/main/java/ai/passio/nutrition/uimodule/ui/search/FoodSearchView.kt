package ai.passio.nutrition.uimodule.ui.search

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.FoodSearchLayoutBinding
import ai.passio.nutrition.uimodule.ui.util.DesignUtils
import ai.passio.nutrition.uimodule.ui.view.VerticalSpaceItemDecoration
import ai.passio.passiosdk.passiofood.PassioFoodDataInfo
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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
        fun onTextCleared()
        fun onViewDismissed()
    }

    private var binding: FoodSearchLayoutBinding? = null
    private var listener: PassioSearchListener? = null
    private val searchAdapter = FoodItemSearchAdapter(::onSearchItemClicked, ::onSearchItemAdded)
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
            searchRecyclerView.layoutManager = LinearLayoutManager(context)
            searchRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(DesignUtils.dp2px(8f)))
            searchRecyclerView.adapter = searchAdapter
            searchSuggestionRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
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
        suggestions: List<String>
    ) {
        if (this.isAttachedToWindow && this.context != null && query == searchTerm) {
            searchAdapter.updateItems(results)
            suggestionAdapter.updateSuggestions(suggestions)
        }
    }

    private fun setupSearchBar() {
        if (binding == null) return
        with(binding!!) {
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    //                    if (s.isEmpty()) {
                    //                        searchClear.visibility = View.GONE
                    //                    } else {
                    //                        searchClear.visibility = View.VISIBLE
                    //                    }

                    searchTerm = s.toString()
                    handler.postDelayed(inputFinishCheck, INPUT_DELAY)
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

            //            searchClear.setOnClickListener {
            //                searchEditText.text.clear()
            //                listener.onTextCleared()
            //            }
        }
    }

    private val inputFinishCheck = Runnable {
        val currentQuery = searchTerm
        if (currentQuery.isEmpty() || currentQuery.length < 3) {
            searchAdapter.updateItems(listOf())
            suggestionAdapter.updateSuggestions(listOf())
            return@Runnable
        }

        listener?.onQueryChange(currentQuery)
    }

    fun onTextChanged() {
        handler.removeCallbacks(inputFinishCheck)
    }

    private fun onSearchItemClicked(searchResult: PassioFoodDataInfo) {
        listener?.onFoodItemSelected(searchResult)
    }

    private fun onSearchItemAdded(searchResult: PassioFoodDataInfo) {
        listener?.onFoodItemLog(searchResult)
    }

    private fun onSuggestion(suggestion: String) {
        searchAdapter.updateItems(listOf())
        suggestionAdapter.updateSuggestions(listOf())
        binding?.let {
            it.searchEditText.setText(suggestion)
            it.searchEditText.setSelection(suggestion.length)
        }
    }

    override fun onDetachedFromWindow() {
        binding = null
        super.onDetachedFromWindow()
    }
}