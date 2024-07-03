package ai.passio.nutrition.uimodule.ui.engineering

import ai.passio.nutrition.uimodule.databinding.PhotoResultItemBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PhotoResultAdapter : RecyclerView.Adapter<PhotoResultAdapter.ResultViewHolder>() {

    private val results = mutableListOf<String>()

    fun addResult(result: String) {
        results.add(result)
        notifyDataSetChanged()
    }

    fun clear() {
        results.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = PhotoResultItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    inner class ResultViewHolder(
        private val binding: PhotoResultItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(result: String) {
            binding.result.text = result
        }
    }
}