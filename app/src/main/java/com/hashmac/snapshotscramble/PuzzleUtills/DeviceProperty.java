package com.hashmac.snapshotscramble.PuzzleUtills;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;

import android.util.DisplayMetrics;
public class DeviceProperty {
    public static Point getDeviceDimension(Context mContext) {
        DisplayMetrics displayMatrix = new DisplayMetrics();
        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMatrix);
        int width = displayMatrix.widthPixels;
        int height = displayMatrix.heightPixels;
        return new Point(width, height);
    }
}
