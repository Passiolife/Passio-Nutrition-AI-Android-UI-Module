package ai.passio.nutrition.uimodule.ui.image

import ai.passio.nutrition.uimodule.databinding.ItemImageBinding
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation

class ImageAdapter(
    private val imageList: List<Bitmap>,
    private val onRemove: (position: Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUri: Bitmap) {
            binding.imageView.load(imageUri) {
                transformations(RoundedCornersTransformation(16f))
            }
            binding.remove.setOnClickListener {
                onRemove.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}