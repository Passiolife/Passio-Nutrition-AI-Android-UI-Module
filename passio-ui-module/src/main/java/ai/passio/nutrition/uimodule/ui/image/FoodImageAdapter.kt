package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.databinding.ItemFoodImageBinding
import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load

class FoodImageAdapter(private var imageUris: List<Bitmap>) :
    RecyclerView.Adapter<FoodImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemFoodImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ItemFoodImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageUris[position]

        holder.binding.foodImage.load(uri)
    }

    override fun getItemCount() = imageUris.size

}