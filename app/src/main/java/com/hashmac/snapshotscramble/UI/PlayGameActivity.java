package com.hashmac.snapshotscramble.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hashmac.snapshotscramble.Model.LiveGameModel;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.Utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivityPlayGameBinding;
import com.hashmac.snapshotscramble.PuzzleUtills.BitmapSplitter;
import com.hashmac.snapshotscramble.PuzzleUtills.DeviceProperty;
import com.hashmac.snapshotscramble.PuzzleUtills.Position;
import com.hashmac.snapshotscramble.PuzzleUtills.TileItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayGameActivity extends AppCompatActivity implements View.OnTouchListener {
    ActivityPlayGameBinding binding;
    LiveGameModel gameModel;
    DatabaseReference reference;
    Bitmap[][] bitmapTiles;
    LinkedHashMap<Integer, TileItem> puzzleItemList;
    LinkedHashMap<Integer, TileItem> shuffledTiles;
    TileItem emptyTile;
    RectF movableBoundary;
    Point deviceDimension;
    int touchPositionX, touchPositionY;
    public static int boardWidth, boardHeight;
    public static int numberOfRows = 3;
    public static int PUZZLE_BOARD_LEFT_MARGIN = 20;
    private int tileSize;
    private int stepCount=0;
    PuzzlePreference puzzlePreference;
    String game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        game = getIntent().getStringExtra("Game");
        init();
    }

    private void init() {
        puzzlePreference = new PuzzlePreference(this);
        switch (puzzlePreference.getDifficultyLevel()) {
            case "Easy":
                numberOfRows = 3;
                break;
            case "Medium":
                numberOfRows = 4;
                break;
            case "Hard":
                numberOfRows = 5;
                break;
        }
        switch (game) {
            case "new":
                Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_image);
                StartNewGame(image);
                break;
            case "resume":
                ResumeGame();
                break;
            case "custom":
                StartNewGame(Config.puzzleImage);
                break;
            case "online":
                OnlineGame();
                break;
        }
    }

    private void OnlineGame() {
        numberOfRows = 3;
        String gameID = getIntent().getStringExtra("GameID");
        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Live").child(gameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
                    builder.setTitle(getString(R.string.app_name));
                    builder.setMessage("Please wait while other player join table");
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);

                    gameModel = snapshot.getValue(LiveGameModel.class);
                    assert gameModel != null;
                    switch (gameModel.getStatus()) {
                        case "Start":
                            dialog.dismiss();
                            StartOnlineGame();
                            break;
                        case "Completed":
                            ShowOnlineWinDialog();
                            break;
                        case "Waiting":
                            dialog.show();
                            break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlayGameActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowOnlineWinDialog() {
        String winMessage;
        if (gameModel.getWinner().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName())) {
            winMessage = "Congratulations,"+ gameModel.getWinner() +"won the game!";
        } else {
            winMessage = "Oops,"+ gameModel.getWinner() +"won the game!";
        }

        int oldScore = Integer.parseInt(puzzlePreference.getScoreOnline());
        if (oldScore>stepCount || oldScore == 0) {
            puzzlePreference.setScoreOnline(String.valueOf(stepCount));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
        builder.setTitle("Congratulations!");
        builder.setMessage(winMessage);
        builder.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startActivity(new Intent(PlayGameActivity.this,MainActivity.class));
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
    }

    private void StartOnlineGame() {
        Toast.makeText(this, "Game is started", Toast.LENGTH_SHORT).show();
        Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.default_image);
        StartNewGame(image);
    }

    private void ResumeGame() {

    }

    private void StartNewGame(Bitmap image) {
        deviceDimension = DeviceProperty.getDeviceDimension(PlayGameActivity.this);
        boardWidth = deviceDimension.x - 2*PUZZLE_BOARD_LEFT_MARGIN;
        boardHeight = deviceDimension.y - 2*PUZZLE_BOARD_LEFT_MARGIN;
        tileSize = boardWidth/numberOfRows;
        LinearLayout.LayoutParams boardParam = new LinearLayout.LayoutParams(boardWidth,boardWidth);
        boardParam.leftMargin = PUZZLE_BOARD_LEFT_MARGIN;
        boardParam.rightMargin = PUZZLE_BOARD_LEFT_MARGIN;
        binding.puzzleFullBoardView.setLayoutParams(boardParam);
        binding.orignalImage.setLayoutParams(boardParam);
        binding.viewOriginalImage.setImageBitmap(image);
        binding.viewOriginalImage.setOnTouchListener(this);
        bitmapTiles = this.createTileBitmaps(image);
        puzzleItemList = this.initializePuzzleTiles(bitmapTiles);
        this.shuffleAndRenderTiles(puzzleItemList);
    }

    private Bitmap[][] createTileBitmaps(Bitmap image) {
        return BitmapSplitter.split(image, boardWidth, boardHeight, numberOfRows);
    }

    private LinkedHashMap<Integer, TileItem> shuffleTiles(LinkedHashMap<Integer, TileItem> puzzleTile) {
        emptyTile = puzzleTile.get(puzzleTile.size() - 1);
        assert emptyTile != null;
        emptyTile.setImage(null);
        emptyTile.setIsBlank(true);
        List keys = new ArrayList(puzzleTile.keySet());
        Collections.shuffle(keys);
        LinkedHashMap<Integer, TileItem> shuffledTile = new LinkedHashMap<>();
        int i = 0;
        for (Object o : keys) {
            TileItem item = puzzleTile.get(o);
            int xAxis = (i < numberOfRows) ? i : (i % numberOfRows);
            int yAxis = 0;
            if (game.equals("online")) {
                if (i >= 3 && i <= 5) {
                    yAxis = 1;
                } else if (i >= 6 && i <= 8) {
                    yAxis = 2;
                }
            } else {
                if (puzzlePreference.getDifficultyLevel().equals("Easy")) {
                    if (i >= 3 && i <= 5) {
                        yAxis = 1;
                    } else if (i >= 6 && i <= 8) {
                        yAxis = 2;
                    }
                } else if (puzzlePreference.getDifficultyLevel().equals("Medium")) {
                    if (i >= 4 && i <= 7) {
                        yAxis = 1;
                    } else if (i >= 8 && i <= 11) {
                        yAxis = 2;
                    } else if (i >= 12 && i <= 15) {
                        yAxis = 3;
                    }
                } else if (puzzlePreference.getDifficultyLevel().equals("Hard")) {
                    if (i >= 5 && i <= 9) {
                        yAxis = 1;
                    } else if (i >= 10 && i <= 14) {
                        yAxis = 2;
                    } else if (i >= 15 && i <= 19) {
                        yAxis = 3;
                    } else if (i >= 20 && i <= 24) {
                        yAxis = 4;
                    }
                }
            }
            assert item != null;
            item.setCurrentPosition(new Position(xAxis, yAxis));
            shuffledTile.put((int) o, puzzleTile.get(o));
            i++;
        }
        return shuffledTile;
    }

    private LinkedHashMap<Integer, TileItem> initializePuzzleTiles(Bitmap[][] bitmapTiles) {
        LinkedHashMap<Integer, TileItem> puzzleItem = new LinkedHashMap<>();
        int tileWidth = (boardWidth/numberOfRows);
        int bitmapPosition = 0;
        for (int i = 0; i < numberOfRows; i++) {
            for(int j = 0; j< numberOfRows; j++) {
                TileItem tile = new TileItem(getApplicationContext());
                tile.setId(bitmapPosition);
                tile.setNumber(bitmapPosition);
                tile.setStartingPosition(new Position(i, j));
                tile.setCorrectPosition(new Position(j,i));
                tile.setImage(bitmapTiles[i][j]);
                tile.setDimension(tileWidth);
                tile.setOnTouchListener(this);
                puzzleItem.put(bitmapPosition++, tile);
            }
        }
        return puzzleItem;
    }

    private void shuffleAndRenderTiles(LinkedHashMap<Integer, TileItem> puzzleItem) {
        shuffledTiles = this.shuffleTiles(puzzleItem);
        for(Map.Entry<Integer, TileItem> entry: shuffledTiles.entrySet()) {
            TileItem item = entry.getValue();
            binding.puzzleFullBoardView.addView(item, item.setLayout());
        }
    }

    private boolean isPuzzleSolvable(List keys) {
        int blankTileRow = emptyTile.getCurrentPosition().getYAxis();
        int inversions = 0;
        for (int i = 0; i < keys.size(); i++) {
            TileItem currentTile = puzzleItemList.get(keys.get(i));
            if(currentTile == emptyTile) continue;
            for (int j = i+1; j < keys.size(); j++) {
                TileItem nextTile = puzzleItemList.get(keys.get(j));
                if(nextTile == emptyTile) continue;
                if(currentTile.getNumber() > nextTile.getNumber()) {
                    inversions++;
                }
            }
        }
        return (((inversions % 2) == 0) && ((blankTileRow % 2) != 0)) || (((inversions % 2) != 0) && ((blankTileRow % 2) == 0));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.view_original_image) {
            displayOriginalImage(event);
            return true;
        }
        TileItem selectedTile = (TileItem) v;
        if(selectedTile.getIsBlank()) {
            return false;
        }
        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if(!checkIfValidMove(selectedTile))
                    return false;
                touchPositionX = (int) event.getRawX();
                touchPositionY = (int) event.getRawY();
                selectedTile.bringToFront();
                movableBoundary = getMovableBoundary(selectedTile);
                break;
            case MotionEvent.ACTION_MOVE:
                dragTilesAround(selectedTile, event);
                touchPositionX = (int) event.getRawX();
                touchPositionY = (int) event.getRawY();

                break;
            case MotionEvent.ACTION_UP:
                if(tileDraggedMoreThenHalfWay(selectedTile) || isJustClick(selectedTile)) {
                    swapTileWithEmpty(selectedTile);
                    increaseStepCounts();
                } else {
                    bringTileToOriginalPosition(selectedTile);
                }
                break;
        }
        return true;
    }
    public boolean checkIfValidMove(TileItem selectedItem) {
        return (selectedItem.isAboveOf(emptyTile)
                || selectedItem.isBelowOf(emptyTile)
                || selectedItem.isLeftOf(emptyTile)
                || selectedItem.isRightOf(emptyTile));
    }

    public void swapTileWithEmpty(TileItem selectedItem) {
        Position selectedItemPosition = selectedItem.getCurrentPosition();
        selectedItem.swapPositionWith(emptyTile.getCurrentPosition());
        emptyTile.swapPositionWith(selectedItemPosition);
        if (isGameComplete()) {
            if (game.equals("online")) {
                FinishOnlineGame();
            } else {
                DialogGameComplete();
            }
        }
    }

    private void FinishOnlineGame() {
        gameModel.setWinner(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
        gameModel.setStatus("Completed");
        reference.child("Live").child(gameModel.getGameID()).setValue(gameModel);
    }

    private void DialogGameComplete() {
        SetScore();
        AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
        builder.setTitle("Congratulations!");
        builder.setMessage("You have successfully completed the game!");
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                init();
            }
        });
        builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void SetScore() {
        int old;
        switch (puzzlePreference.getDifficultyLevel()) {
            case "Easy":
                old = Integer.parseInt(puzzlePreference.getScoreEasy());
                if (old == 0) {
                    puzzlePreference.setScoreEasy(String.valueOf(stepCount));
                } else {
                    if (stepCount < old) {
                        puzzlePreference.setScoreEasy(String.valueOf(stepCount));
                    }
                }
                break;
            case "Medium":
                old = Integer.parseInt(puzzlePreference.getScoreMedium());
                if (old == 0) {
                    puzzlePreference.setScoreMedium(String.valueOf(stepCount));
                } else {
                    if (stepCount < old) {
                        puzzlePreference.setScoreMedium(String.valueOf(stepCount));
                    }
                }
                break;
            case "Hard":
                old = Integer.parseInt(puzzlePreference.getScoreHard());
                if (old == 0) {
                    puzzlePreference.setScoreHard(String.valueOf(stepCount));
                } else {
                    if (stepCount < old) {
                        puzzlePreference.setScoreHard(String.valueOf(stepCount));
                    }
                }
                break;
        }
    }

    private boolean isGameComplete() {
        for(Map.Entry<Integer, TileItem> entry: puzzleItemList.entrySet()) {
            TileItem item = entry.getValue();
            if (item.getCorrectPosition().getYAxis() != item.getCurrentPosition().getYAxis() || item.getCorrectPosition().getXAxis() != item.getCurrentPosition().getXAxis() ) {
                Log.e("Shuffled tile","Complete");
                return false;
            }
        }
        return true;
    }
    public void bringTileToOriginalPosition(TileItem selectedItem) {
        selectedItem.setLayoutParams(selectedItem.setLayout());
    }
    private boolean tileDraggedMoreThenHalfWay(TileItem selectedItem) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selectedItem.getLayoutParams();
        int originalMargin = 0, currentMargin = 0;
        if(selectedItem.isBelowOf(emptyTile) || selectedItem.isAboveOf(emptyTile)) {
            originalMargin = selectedItem.getCurrentPosition().getYAxis() * tileSize;
            currentMargin = params.topMargin;
        } else {
            originalMargin = selectedItem.getCurrentPosition().getXAxis() * tileSize;
            currentMargin = params.leftMargin;
        }
        return Math.abs(originalMargin - currentMargin) >= tileSize / 2;
    }
    private boolean isJustClick(TileItem selectedItem) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selectedItem.getLayoutParams();
        int originalTopMargin = selectedItem.getCurrentPosition().getYAxis() * tileSize;
        int currentTopMargin = params.topMargin;
        int originLeftMargin = selectedItem.getCurrentPosition().getXAxis() * tileSize;
        int currentLeftMargin = params.leftMargin;
        return (Math.abs(originalTopMargin - currentTopMargin) < 5
                && (originLeftMargin - currentLeftMargin) < 5);
    }
    public void displayOriginalImage(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                binding.puzzleFullBoardView.setVisibility(View.GONE);
                binding.orignalImage.setVisibility(View.VISIBLE);
                break;
            case MotionEvent.ACTION_UP:
                binding.puzzleFullBoardView.setVisibility(View.VISIBLE);
                binding.orignalImage.setVisibility(View.GONE);
                break;
        }
    }

    public void dragTilesAround(TileItem selectedTile, MotionEvent event) {
        int xCoordinate = (int) event.getRawX() - touchPositionX;
        int yCoordinate = (int) event.getRawY() - touchPositionY;
        RectF selectedTileBoundary = getSelectedTileBoundary(selectedTile);
        if(movableBoundary.contains(selectedTileBoundary)) {
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) selectedTile.getLayoutParams();
            param.height = tileSize;
            param.width = tileSize;
            if(selectedTile.isRightOf(emptyTile) || selectedTile.isLeftOf(emptyTile)) {
                param.leftMargin = xCoordinate + param.leftMargin;
            } else {
                param.topMargin = yCoordinate + param.topMargin;
            }
            selectedTile.setLayoutParams(param);
        }
    }

    public RectF getMovableBoundary(TileItem selectedItem) {
        int boardTop = (int) Math.floor(binding.puzzleFullBoardView.getY());
        int boardLeft = (int) Math.floor(binding.puzzleFullBoardView.getX());
        int emptyTop = (emptyTile.getCurrentPosition().getYAxis() * tileSize)+boardTop;
        int emptyLeft = (emptyTile.getCurrentPosition().getXAxis() * tileSize)+boardLeft;
        int emptyRight = emptyLeft + tileSize;
        int emptyBottom = emptyTop +tileSize;
        int selectedItemTop = (selectedItem.getCurrentPosition().getYAxis() * tileSize)+boardTop;
        int selectedItemLeft = (selectedItem.getCurrentPosition().getXAxis() * tileSize)+boardLeft;
        int selectedItemRight = selectedItemLeft + tileSize;
        int selectedItemBottom = selectedItemTop + tileSize;
        int left = Math.min(emptyLeft, selectedItemLeft);
        int top = Math.min(emptyTop, selectedItemTop);
        int right = Math.max(emptyRight, selectedItemRight);
        int bottom = Math.max(emptyBottom, selectedItemBottom);
        return new RectF(left, top, right, bottom);
    }

    public RectF getSelectedTileBoundary(TileItem selectedItem) {
        int boardTop = (int) Math.floor(binding.puzzleFullBoardView.getY());
        int boardLeft = (int) Math.floor(binding.puzzleFullBoardView.getX());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) selectedItem.getLayoutParams();
        int selectedItemLeft = boardLeft + params.leftMargin;
        int selectedItemTop = boardTop + params.topMargin;
        int selectedItemRight = selectedItemLeft + tileSize;
        int selectedItemBottom = selectedItemTop + tileSize;
        return new RectF(selectedItemLeft, selectedItemTop, selectedItemRight, selectedItemBottom);
    }
    public void increaseStepCounts() {
        stepCount++;
        binding.puzzleStepCounts.setText(String.valueOf(stepCount));
    }
}