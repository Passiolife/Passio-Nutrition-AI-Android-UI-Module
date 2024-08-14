package ai.passio.nutrition.uimodule.ui.foodcreator

import ai.passio.nutrition.uimodule.databinding.ItemNutritionFactsBinding
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class NutritionFactsItemAdapter(private var imageUris: List<Bitmap>) :
    RecyclerView.Adapter<NutritionFactsItemAdapter.NutritionFactsItemViewHolder>() {

    inner class NutritionFactsItemViewHolder(val binding: ItemNutritionFactsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionFactsItemViewHolder {
        val binding =
            ItemNutritionFactsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NutritionFactsItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutritionFactsItemViewHolder, position: Int) {
        val uri = imageUris[position]


    }

    override fun getItemCount() = imageUris.size

}