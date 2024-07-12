package ai.passio.nutrition.uimodule.ui.profile

import ai.passio.nutrition.uimodule.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class GenericSpinnerAdapter<T>(
    context: Context,
    private val items: List<T>,
    private val itemToString: (T) -> String
) : ArrayAdapter<T>(context, R.layout.generic_spinner_view, android.R.id.text1, items) {

    init {
//        setDropDownViewResource(R.layout.generic_spinner_view)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.generic_spinner_view, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = itemToString(items[position])
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.generic_spinner_item, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = itemToString(items[position])
        return view
    }
}