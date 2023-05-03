package com.hashmac.snapshotscramble.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.hashmac.snapshotscramble.AppController;
import com.hashmac.snapshotscramble.models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Config {
    public static final int SPLASH_DURATION = 1000;// 3.5 seconds
    public static final int PROGRESS_INTERVAL = 100; // 100 milliseconds
    public static boolean UPDATE_AVAILABLE = false;
    public static boolean IS_LOGIN = false;
    public static Bitmap puzzleImage;
    public static User user = null;

    public static String generateUniqueId(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void checkMusic(Context context) {
        AppController appController = (AppController) context.getApplicationContext();
        appController.loadPreferences();
    }
    public static void stopMusic(Context context) {
        AppController appController = (AppController) context.getApplicationContext();
        appController.stopMusic();
    }
}
