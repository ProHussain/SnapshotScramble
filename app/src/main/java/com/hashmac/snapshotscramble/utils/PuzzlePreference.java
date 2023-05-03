package com.hashmac.snapshotscramble.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PuzzlePreference {
    private static final String PREF_NAME = "puzzle_preferences";
    private static final String VIBRATION_ENABLED = "vibration_enabled";
    private static final String SOUND_ENABLED = "sound_enabled";
    private static final String CURRENT_LEVEL = "current_level";

    private final SharedPreferences preferences;

    public PuzzlePreference(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setVibrationEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(VIBRATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isVibrationEnabled() {
        return preferences.getBoolean(VIBRATION_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(SOUND_ENABLED, enabled);
        editor.apply();
    }

    public boolean isSoundEnabled() {
        return preferences.getBoolean(SOUND_ENABLED, true);
    }


    public void setCurrentLevel(int level) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CURRENT_LEVEL, level);
        editor.apply();
    }

    public int getCurrentLevel() {
        return preferences.getInt(CURRENT_LEVEL, 1);
    }
}
