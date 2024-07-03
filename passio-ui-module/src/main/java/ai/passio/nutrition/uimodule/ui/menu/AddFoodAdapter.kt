package ai.passio.nutrition.uimodule.ui.menu

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.AddFoodItemBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

data class AddFoodOption(
    val id: Int,
    val textId: Int,
    val iconId: Int
)

class AddFoodAdapter(
    private val items: List<AddFoodOption>,
    private val onItemClicked: (id: Int) -> Unit
) : RecyclerView.Adapter<AddFoodAdapter.AddFoodViewHolder>() {

    inner class AddFoodViewHolder(private val binding: AddFoodItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(option: AddFoodOption) {
            binding.addFoodIcon.setImageResource(option.iconId)
            binding.addFoodName.setText(option.textId)
            binding.root.setOnClickListener {
                onItemClicked(option.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFoodViewHolder {
        val binding = AddFoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddFoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddFoodViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}