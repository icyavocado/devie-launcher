package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.voxel.simplesearchlauncher.settings.SettingsPalette;

/* Settings card container that follows the custom color palette (Theme
 * experiment); falls back to the day/night resource when no custom colors
 * are set. Replaces the hardcoded background attr on the settings cards. */
public class ThemeCardView extends LinearLayout {
    public ThemeCardView(Context context) {
        super(context);
        applyPalette();
    }

    public ThemeCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyPalette();
    }

    public ThemeCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyPalette();
    }

    private void applyPalette() {
        setBackground(SettingsPalette.cardBackground(getContext()));
    }
}
