package com.voxel.simplesearchlauncher.settings;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import android.widget.CompoundButton;

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
     * after setContentView so the toolbar exists. The toolbar title and
     * navigation arrow derive from the custom background's luminance. */
    public static void applyToActivity(android.app.Activity activity) {
        int bg = background();
        if (bg == UNSET) {
            return;
        }
        Window window = activity.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(bg));
        window.setStatusBarColor(bg);
        window.setNavigationBarColor(bg);
        View toolbarView = activity.findViewById(R.id.toolbar);
        if (toolbarView instanceof androidx.appcompat.widget.Toolbar) {
            androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) toolbarView;
            toolbar.setBackgroundColor(bg);
            boolean darkBackground = luminance(bg) <= 0.5f;
            toolbar.setTitleTextColor(darkBackground ? 0xffe3e7ec : 0xff1f2a38);
            /* The home arrow uses colorControlNormal; tint it directly since
             * the surrounding theme stays Light in light mode. */
            android.graphics.drawable.Drawable nav = toolbar.getNavigationIcon();
            if (nav != null) {
                nav = androidx.core.graphics.drawable.DrawableCompat.wrap(nav).mutate();
                androidx.core.graphics.drawable.DrawableCompat.setTint(nav, darkBackground ? 0xffe3e7ec : 0xff1f2a38);
                toolbar.setNavigationIcon(nav);
            }
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

    /* Neutral unchecked color, derived from the palette so it stays visible
     * on custom backgrounds. */
    static int uncheckedColor(Context context) {
        boolean darkTrack = isNight(context) || background() != UNSET && luminance(background()) <= 0.5f;
        return darkTrack ? 0xff4a5560 : 0xffb8bfc9;
    }

    /** The card surface behind switches: the custom card color when set,
     * else the day/night card. Used for pre-compositing switch colors. */
    public static int surfaceColor(Context context) {
        int card = card();
        if (card != UNSET) {
            return card;
        }
        return isNight(context) ? 0xff1f242b : 0xffffffff;
    }

    /** Resolves the switch-tint base: the custom accent when set, else the
     * built-in teal, so the ON/OFF pill treatment is uniform. */
    public static int resolveBaseAccent(Context context) {
        int accent = accentOrUnset();
        if (accent != UNSET) {
            return accent;
        }
        return ContextCompat.getColor(context, R.color.evie_theme_green);
    }

    /** Applies the theme switch opacities to a SwitchCompat: from a base
     * color over the card surface, ON = thumb full accent / pill 75% blend,
     * OFF = thumb 50% blend / pill 25% blend — all pre-composited opaque so
     * the thumb never shows the track through it. */
    public static void switchTints(androidx.appcompat.widget.SwitchCompat switchCompat, int baseAccent, int surface) {
        int onThumb = baseAccent;
        int offThumb = ColorUtils.blendARGB(baseAccent, surface, 0.5f);
        int onTrack = ColorUtils.blendARGB(baseAccent, surface, 0.75f);
        int offTrack = ColorUtils.blendARGB(baseAccent, surface, 0.25f);
        int[][] states = {new int[]{android.R.attr.state_checked}, new int[]{}};
        switchCompat.setThumbTintList(new android.content.res.ColorStateList(states, new int[]{onThumb, offThumb}));
        switchCompat.setTrackTintList(new android.content.res.ColorStateList(states, new int[]{onTrack, offTrack}));
    }

    private static int blendAlpha(int color, float alpha) {
        int base = color & 0xFFFFFF;
        return ((int) (alpha * 255) << 24) | base;
    }

    /** Tints checkboxes and radios: checked state gets the accent, unchecked
     * gets a neutral gray. No-op when no custom accent is set. Switches use
     * switchTints() instead. */
    public static void tintCompoundButton(CompoundButton button, Context context) {
        int accent = accentOrUnset();
        if (accent == UNSET || button instanceof androidx.appcompat.widget.SwitchCompat) {
            return;
        }
        int[][] states = {new int[]{android.R.attr.state_checked}, new int[]{}};
        int[] colors = {accent, uncheckedColor(context)};
        android.content.res.ColorStateList tintList = new android.content.res.ColorStateList(states, colors);
        if (button instanceof androidx.appcompat.widget.AppCompatCheckBox) {
            ((androidx.appcompat.widget.AppCompatCheckBox) button).setSupportButtonTintList(tintList);
        } else if (button instanceof androidx.appcompat.widget.AppCompatRadioButton) {
            ((androidx.appcompat.widget.AppCompatRadioButton) button).setSupportButtonTintList(tintList);
        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
            button.setButtonTintList(tintList);
        }
    }

    /** Tints a DiscreteSeekBar's scrubber and thumb with the custom accent;
     * no-op when no custom accent is set. */
    public static void tintSeekBar(com.ovacoda.devie.widget.DiscreteSeekBar seekBar) {
        int accent = accentOrUnset();
        if (accent == UNSET) {
            return;
        }
        seekBar.setScrubberColor(accent);
        seekBar.setThumbColor(accent, accent);
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
