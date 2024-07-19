package ai.passio.nutrition.uimodule.ui.weight

import ai.passio.nutrition.uimodule.databinding.ItemWeightRecordBinding
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.model.WeightRecord
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class WeightAdapter(
    private val weightRecords: ArrayList<WeightRecord>,
    private val onTapped: (weightRecord: WeightRecord) -> Unit,
) :
    RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(weightRecords: List<WeightRecord>) {
        this.weightRecords.clear()
        this.weightRecords.addAll(weightRecords)
        notifyDataSetChanged()
    }


    inner class WeightViewHolder(val binding: ItemWeightRecordBinding) :
        ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(weightRecord: WeightRecord) {
            with(binding) {
                weight.text = weightRecord.getDisplayWeight()
                weightUnit.text = UserCache.getProfile().measurementUnit.weightUnit.value
                dateTime.text = "${weightRecord.getDisplayDay()}\n${weightRecord.getDisplayTime()}"
                root.setOnClickListener {
                    onTapped.invoke(weightRecord)
                }
            }
        }
    }

    fun getItem(position: Int): WeightRecord
    {
        return weightRecords[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        return WeightViewHolder(
            ItemWeightRecordBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return weightRecords.size
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.bind(weightRecord = weightRecords[position])
    }
}