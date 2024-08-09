package ai.passio.nutrition.uimodule.ui.mealplan

import ai.passio.nutrition.uimodule.R
import ai.passio.nutrition.uimodule.databinding.ItemMealplanDayBinding
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class DaysAdapter(
    private var currentDay: Int,
    private val days: List<Int>,
    private val daySelected: (day: Int) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding =
            ItemMealplanDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun getItemCount(): Int = days.size

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bindTo(days[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedDay(currentDay: Int) {
        if (currentDay in 1..14 && this.currentDay != currentDay) {
            this.currentDay = currentDay
            notifyDataSetChanged()
        }
    }

    inner class DayViewHolder(
        private val binding: ItemMealplanDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun bindTo(day: Int) {
            with(binding) {
                tvDay.text = "Day $day"
                if (day == currentDay) {
                    tvDay.setBackgroundResource(R.drawable.rc_16_passio_primary)
                    tvDay.setTextColor(ContextCompat.getColor(root.context, R.color.passio_white))
                } else {
                    tvDay.setBackgroundResource(R.drawable.rc_16_passio_gray100)
                    tvDay.setTextColor(ContextCompat.getColor(root.context, R.color.passio_gray900))
                }
                root.setOnClickListener {
                    daySelected.invoke(day)
                    setSelectedDay(day)
                }
            }
        }
    }
}