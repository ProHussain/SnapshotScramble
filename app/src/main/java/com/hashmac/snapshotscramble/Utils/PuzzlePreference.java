package com.hashmac.snapshotscramble.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PuzzlePreference {
    private static final String PREF_NAME = "puzzle_preferences";
    private static final String VIBRATION_ENABLED = "vibration_enabled";
    private static final String SOUND_ENABLED = "sound_enabled";
    private static final String CURRENT_LEVEL = "current_level";

    private static final String SCORE_EASY = "score_easy";
    private static final String SCORE_MEDIUM = "score_medium";
    private static final String SCORE_HARD = "score_hard";
    private static final String SCORE_ONLINE = "score_online";

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

    public void setScoreEasy(String score) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SCORE_EASY, score);
        editor.apply();
    }

    public void setCurrentLevel(int level) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CURRENT_LEVEL, level);
        editor.apply();
    }

    public int getCurrentLevel() {
        return preferences.getInt(CURRENT_LEVEL, 1);
    }

    public String getScoreEasy() {
        return preferences.getString(SCORE_EASY, "0");
    }

    public void setScoreMedium(String score) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SCORE_MEDIUM, score);
        editor.apply();
    }

    public String getScoreMedium() {
        return preferences.getString(SCORE_MEDIUM, "0");
    }

    public void setScoreHard(String score) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SCORE_HARD, score);
        editor.apply();
    }

    public String getScoreHard() {
        return preferences.getString(SCORE_HARD, "0");
    }

    public void setScoreOnline(String score) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SCORE_ONLINE, score);
        editor.apply();
    }

    public String getScoreOnline() {
        return preferences.getString(SCORE_ONLINE, "0");
    }
}
