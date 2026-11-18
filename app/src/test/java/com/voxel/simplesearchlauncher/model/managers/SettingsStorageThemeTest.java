package com.voxel.simplesearchlauncher.model.managers;

import static org.junit.Assert.assertEquals;

import androidx.appcompat.app.AppCompatDelegate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Locks the theme -> night-mode mapping (MODE_NIGHT_NO = light!). */
@RunWith(RobolectricTestRunner.class)
@Config(application = android.app.Application.class, sdk = 33)
public class SettingsStorageThemeTest {

    @Test
    public void darkMapsToNightYes() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, SettingsStorage.themeToNightMode(SettingsStorage.THEME_DARK));
    }

    @Test
    public void lightMapsToNightNo() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, SettingsStorage.themeToNightMode(SettingsStorage.THEME_LIGHT));
    }

    @Test
    public void systemAndGarbageFallBackToFollowSystem() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, SettingsStorage.themeToNightMode(SettingsStorage.THEME_SYSTEM));
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, SettingsStorage.themeToNightMode("bogus"));
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, SettingsStorage.themeToNightMode(null));
    }
}
