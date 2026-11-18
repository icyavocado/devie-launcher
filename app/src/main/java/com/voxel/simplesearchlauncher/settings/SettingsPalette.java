package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;

import androidx.core.content.ContextCompat;

import com.ovacoda.devie.R;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;

import android.view.View;

/* Custom color palette for the settings screens (Theme experiment). Values
 * live in settings_storage (-1 = unset → the built-in day/night palette);
 * text colors derive from the card luminance so users cannot build
 * unreadable combinations. */
public final class SettingsPalette {

    private static final int UNSET = -1;
    private static final int CARD_RADIUS_DP = 14;

    private SettingsPalette() {
    }

    public static int background() {
        return SettingsStorage.getInstance().getThemeColor("theme_bg");
    }

    public static int card() {
        return SettingsStorage.getInstance().getThemeColor("theme_card");
    }

    public static int accent() {
        return SettingsStorage.getInstance().getThemeColor("theme_accent");
    }

    public static boolean hasCustomColors() {
        return background() != UNSET || card() != UNSET || accent() != UNSET;
    }

    public static void setBackground(int color) {
        SettingsStorage.getInstance().setThemeColor("theme_bg", color);
    }

    public static void setCard(int color) {
        SettingsStorage.getInstance().setThemeColor("theme_card", color);
    }

    public static void setAccent(int color) {
        SettingsStorage.getInstance().setThemeColor("theme_accent", color);
    }

    public static void reset() {
        SettingsStorage.getInstance().setThemeColor("theme_bg", UNSET);
        SettingsStorage.getInstance().setThemeColor("theme_card", UNSET);
        SettingsStorage.getInstance().setThemeColor("theme_accent", UNSET);
    }

    /** Window + toolbar background honoring the custom palette. Called
     * after setContentView so the toolbar exists. */
    public static void applyToActivity(android.app.Activity activity) {
        int bg = background();
        if (bg == UNSET) {
            return;
        }
        Window window = activity.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(bg));
        window.setStatusBarColor(bg);
        window.setNavigationBarColor(bg);
        View toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setBackgroundColor(bg);
        }
    }

    /** Card background: the custom color, else the day/night resource. */
    public static Drawable cardBackground(Context context) {
        int card = card();
        if (card == UNSET) {
            return ContextCompat.getDrawable(context, R.drawable.settings_list_item_background);
        }
        float density = context.getResources().getDisplayMetrics().density;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(card);
        drawable.setCornerRadius(CARD_RADIUS_DP * density);
        return drawable;
    }

    /* Derived text colors: dark text on a light card, light text on a dark
     * card; falls back to the built-in palette when unset. */
    public static int labelColor(Context context) {
        int card = card();
        if (card == UNSET) {
            return isNight(context) ? 0xffe3e7ec : 0xff1f2a38;
        }
        return luminance(card) > 0.5f ? 0xff1f2a38 : 0xffe3e7ec;
    }

    public static int subLabelColor(Context context) {
        int card = card();
        if (card == UNSET) {
            return isNight(context) ? 0xff77808b : 0xff99a3ad;
        }
        return luminance(card) > 0.5f ? 0xff99a3ad : 0xff77808b;
    }

    public static int headerColor(Context context) {
        int bg = background();
        if (bg == UNSET) {
            return isNight(context) ? 0xff8a93a0 : 0xff6b7683;
        }
        return luminance(bg) > 0.5f ? 0xff6b7683 : 0xff8a93a0;
    }

    /** Accent for switch tinting; -1 when the built-in accent is in use. */
    public static int accentOrUnset() {
        return accent();
    }

    private static boolean isNight(Context context) {
        int night = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return night == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private static float luminance(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;
        return (float) (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }
}
