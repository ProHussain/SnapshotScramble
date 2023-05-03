package com.hashmac.snapshotscramble;

import android.app.Application;
import android.media.MediaPlayer;

import com.hashmac.snapshotscramble.models.GameLevel;
import com.hashmac.snapshotscramble.utils.PuzzlePreference;

public class AppController extends Application {
    MediaPlayer player;
    PuzzlePreference puzzlePreference;
    @Override
    public void onCreate() {
        super.onCreate();
        puzzlePreference = new PuzzlePreference(this);
        GameLevel.setGameLevels();
    }

    private void startMusic() {
        if (puzzlePreference.isSoundEnabled()) {
            if (player != null && player.isPlaying()) {
                return;
            }
            player = MediaPlayer.create(this, R.raw.background_music);
            player.setLooping(true);
            player.start();
        }
    }

    public void stopMusic() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
        }
    }

    public void loadPreferences() {
        if (puzzlePreference.isSoundEnabled()) {
            startMusic();
        } else {
            stopMusic();
        }
    }
}
