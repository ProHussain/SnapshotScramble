package com.hashmac.snapshotscramble;

import android.app.Application;

import com.hashmac.snapshotscramble.models.GameLevel;

public class AppControler extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GameLevel.setGameLevels();
    }

}
