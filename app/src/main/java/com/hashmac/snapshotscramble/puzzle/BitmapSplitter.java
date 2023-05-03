package com.hashmac.snapshotscramble.puzzle;

import android.graphics.Bitmap;
public class BitmapSplitter {
    public static Bitmap[][] split(Bitmap image, int width, int height, int row) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, width, width, true);
        Bitmap[][] tiles = new Bitmap[row][row];
        int imageSize = width/row;
        int xAxis = 0, yAxis = 0;
        for(int i=0; i < row ; i++) {
            xAxis = 0;
            for(int j=0; j < row; j++) {
                tiles[i][j] = Bitmap.createBitmap(scaledBitmap, xAxis, yAxis, imageSize, imageSize);
                xAxis += imageSize;
            }
            yAxis += imageSize;
        }
        return tiles;
    }
}

