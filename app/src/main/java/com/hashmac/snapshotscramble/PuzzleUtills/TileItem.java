package com.hashmac.snapshotscramble.PuzzleUtills;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.UI.PlayGameActivity;

public class TileItem extends androidx.appcompat.widget.AppCompatImageView {
    private Position startingPosition;
    private Position currentPosition;
    private Position correctPosition;
    int number;
    Boolean isBlank = false;
    public TileItem(Context context ) {
        super(context);
    }
    public int getNumber() {
        return number;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public Boolean getBlank() {
        return isBlank;
    }
    public void setBlank(Boolean blank) {
        isBlank = blank;
    }
    public RelativeLayout.LayoutParams setLayout() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                PlayGameActivity.boardWidth/ PlayGameActivity.numberOfRows-5,
                PlayGameActivity.boardWidth/PlayGameActivity.numberOfRows-5
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.leftMargin = ((currentPosition.getXAxis() * (PlayGameActivity.boardWidth/PlayGameActivity.numberOfRows)));
        params.topMargin = (currentPosition.getYAxis() * (PlayGameActivity.boardWidth / PlayGameActivity.numberOfRows));
        return params;
    }

    public void swapPositionWith(Position itemPosition) {
        int oldLeftMargin = (currentPosition.getXAxis() * (PlayGameActivity.boardWidth/PlayGameActivity.numberOfRows));
        int oldTopMargin = (currentPosition.getYAxis() * (PlayGameActivity.boardWidth / PlayGameActivity.numberOfRows));
        setCurrentPosition(itemPosition);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                PlayGameActivity.boardWidth/ PlayGameActivity.numberOfRows-5,
                PlayGameActivity.boardWidth/PlayGameActivity.numberOfRows-5
        );
        int newLeftMargin = (currentPosition.getXAxis() * (PlayGameActivity.boardWidth/PlayGameActivity.numberOfRows));
        int newTopMargin = (currentPosition.getYAxis() * (PlayGameActivity.boardWidth / PlayGameActivity.numberOfRows));
        if(newLeftMargin == oldLeftMargin){
            if(oldTopMargin > newTopMargin) {
                for(int i = oldTopMargin; i >= newTopMargin; i--) {
                    params.leftMargin = oldLeftMargin;
                    params.topMargin = i;
                    setLayoutParams(params);

                }
            } else {
                for(int i = oldTopMargin; i<= newTopMargin; i++) {
                    params.leftMargin = oldLeftMargin;
                    params.topMargin = i;
                    setLayoutParams(params);
                }
            }
        } else if(newTopMargin == oldTopMargin) {
            if(oldLeftMargin < newLeftMargin) {
                for(int i = oldLeftMargin; i<= newLeftMargin; i++) {
                    params.leftMargin = i;
                    params.topMargin = oldTopMargin;
                    setLayoutParams(params);
                }
            } else {
                for(int i = oldLeftMargin; i>=newLeftMargin; i--) {
                    params.leftMargin = i;
                    params.topMargin = oldTopMargin;
                    setLayoutParams(params);
                }
            }
        }
    }

    public void setImage(Bitmap image) {
        if(image == null) {
            setBackgroundColor(getContext().getResources().getColor(R.color.colorBackground));
            setAlpha(0);
            isBlank = true;
        } else {
            setImageBitmap(image);
        }

    }


    public Position getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(Position startingPosition) {
        this.startingPosition = startingPosition;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Position getCorrectPosition() {
        return correctPosition;
    }

    public void setCorrectPosition(Position correctPosition) {
        this.correctPosition = correctPosition;
    }

    public void setDimension(int width) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
        setLayoutParams(params);
    }

    public Boolean getIsBlank() {
        return isBlank;
    }

    public void setIsBlank(Boolean isBlank) {
        this.isBlank = isBlank;
    }

    @Override
    public String toString() {
        return "TileItem{" +
                "isBlank=" + isBlank +
                '}';
    }

    public boolean isLeftOf(TileItem matchTile) {

        return (currentPosition.yAxis == matchTile.currentPosition.yAxis
                && currentPosition.xAxis == matchTile.currentPosition.xAxis - 1);
    }

    public boolean isRightOf(TileItem matchTile) {
        return  (currentPosition.yAxis == matchTile.currentPosition.yAxis
                && currentPosition.xAxis == matchTile.currentPosition.xAxis + 1);
    }

    public boolean isBelowOf(TileItem matchTile) {
        return (currentPosition.xAxis == matchTile.currentPosition.xAxis
                && currentPosition.yAxis == matchTile.currentPosition.yAxis+1);
    }

    public boolean isAboveOf(TileItem matchTile) {
        return (currentPosition.xAxis == matchTile.currentPosition.xAxis
                && currentPosition.yAxis == matchTile.currentPosition.yAxis-1);
    }
}