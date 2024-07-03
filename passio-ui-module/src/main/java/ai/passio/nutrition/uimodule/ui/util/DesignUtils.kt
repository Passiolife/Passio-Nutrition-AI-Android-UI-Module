package ai.passio.nutrition.uimodule.ui.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.graphics.drawable.DrawableCompat

object DesignUtils {

    private fun applyDimension(typedValue: Int, value: Float): Float {
        return TypedValue.applyDimension(typedValue, value, Resources.getSystem().displayMetrics)
    }

    fun dp2pxFloat(value: Float): Float {
        return applyDimension(TypedValue.COMPLEX_UNIT_DIP, value)
    }

    fun dp2px(value: Float): Int {
        return Math.round(dp2pxFloat(value))
    }

    fun sp2pxFloat(value: Float): Float {
        return applyDimension(TypedValue.COMPLEX_UNIT_SP, value)
    }

    fun sp2px(value: Float): Int {
        return Math.round(sp2pxFloat(value))
    }

    fun screenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun screenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun screenVisibleHeight(activity: Activity): Int {
        return activity.resources.displayMetrics.heightPixels - getStatusBarHeight(activity)
    }

    fun getStatusBarHeight(activity: Activity): Int {
        val rectangle = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rectangle)
        return rectangle.top
    }

    fun getBottomNavigationHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("design_bottom_navigation_height", "dimen", context.packageName)
        return if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId) else 0
    }

    fun Drawable.changeColor(color: Int): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(this)
        DrawableCompat.setTint(wrappedDrawable, color)
        return wrappedDrawable
    }
}