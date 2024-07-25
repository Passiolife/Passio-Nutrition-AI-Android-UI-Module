package ai.passio.nutrition.uimodule.ui.view
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalSpaceItemDecoration(
    private val startEndSpace: Int,
    private val betweenSpace: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        // Set start margin for the first item
        if (position == 0) {
            outRect.left = startEndSpace
        }

        // Set end margin for the last item
        if (position == itemCount - 1) {
            outRect.right = startEndSpace
        }

        // Set margin between items
        if (position != itemCount - 1) {
            outRect.right = betweenSpace
        }
    }
}
