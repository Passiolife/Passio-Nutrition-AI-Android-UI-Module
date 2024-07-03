package ai.passio.nutrition.uimodule.ui.engineering

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.VoiceRecognitionItemBinding
import ai.passio.passiosdk.passiofood.data.model.PassioLogAction
import ai.passio.passiosdk.passiofood.data.model.PassioSpeechRecognitionModel
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView

class VoiceRecognitionAdapter :
    RecyclerView.Adapter<VoiceRecognitionAdapter.VoiceRecognitionViewHolder>() {

    inner class VoiceRecognitionViewHolder(
        private val binding: VoiceRecognitionItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(model: PassioSpeechRecognitionModel) {
            with(binding) {
                if (model.action == PassioLogAction.ADD) {
                    action.setImageDrawable(
                        AppCompatResources.getDrawable(
                            binding.root.context,
                            R.drawable.plus
                        )
                    )
                } else if (model.action == PassioLogAction.REMOVE) {
                    action.setImageDrawable(
                        AppCompatResources.getDrawable(
                            binding.root.context,
                            R.drawable.minus
                        )
                    )
                } else {
                    action.setImageDrawable(
                        AppCompatResources.getDrawable(
                            binding.root.context,
                            R.drawable.not_available
                        )
                    )
                }

                food.text =
                    model.advisorInfo.foodDataInfo?.foodName?.capitalize() +
                            ", " + model.advisorInfo.portionSize +
                            " (${model.advisorInfo.weightGrams} g)"

                meal.text = model.mealTime?.mealName
            }
        }
    }

    private val items = mutableListOf<PassioSpeechRecognitionModel>()

    fun update(newItems: List<PassioSpeechRecognitionModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceRecognitionViewHolder {
        val binding =
            VoiceRecognitionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VoiceRecognitionViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VoiceRecognitionViewHolder, position: Int) {
        holder.bind(items[position])
    }
}