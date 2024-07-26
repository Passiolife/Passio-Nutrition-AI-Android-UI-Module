package ai.passio.nutrition.uimodule.ui.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration(
    private val spaceAround: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        outRect.top = spaceAround

        // Set margin bottom for the last
        if (position == itemCount - 1) {
            outRect.bottom = spaceAround
        }
    }
}
