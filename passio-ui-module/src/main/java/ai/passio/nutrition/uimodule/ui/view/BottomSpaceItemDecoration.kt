package ai.passio.nutrition.uimodule.ui.view
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BottomSpaceItemDecoration(private val bottomSpaceHeight: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val layoutManager = parent.layoutManager as? GridLayoutManager ?: return
        val adapter = parent.adapter ?: return

        val position = parent.getChildAdapterPosition(view)
        val spanCount = layoutManager.spanCount
        val itemCount = adapter.itemCount

        // Check if the item is in the last row
        if (isInLastRow(position, spanCount, itemCount)) {
            outRect.bottom = bottomSpaceHeight
        }
    }

    private fun isInLastRow(position: Int, spanCount: Int, itemCount: Int): Boolean {
        val lastRowItemCount = itemCount % spanCount
        return if (lastRowItemCount == 0) {
            position >= itemCount - spanCount
        } else {
            position >= itemCount - lastRowItemCount
        }
    }
}
