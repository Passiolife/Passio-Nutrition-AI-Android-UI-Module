package ai.passio.nutrition.uimodule.ui.view.tickseekbar;


import androidx.annotation.ColorInt;

/**
 * for collecting each section track color
 */
public interface ColorCollector {
    boolean collectSectionTrackColor(@ColorInt int[] colorIntArr);
}