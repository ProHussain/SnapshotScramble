package com.hashmac.snapshotscramble.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hashmac.snapshotscramble.PuzzleUtills.BitmapSplitter;
import com.hashmac.snapshotscramble.PuzzleUtills.DeviceProperty;
import com.hashmac.snapshotscramble.PuzzleUtills.Position;
import com.hashmac.snapshotscramble.PuzzleUtills.TileItem;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.Utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivityPlayGameBinding;
import com.hashmac.snapshotscramble.models.GameLevel;
import com.hashmac.snapshotscramble.models.LiveGameModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayGameActivity extends BaseActivity implements View.OnTouchListener {
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
    public static int numberOfRows = 4;
    public static int PUZZLE_BOARD_LEFT_MARGIN = 20;
    private int tileSize;
    private int stepCount = 0;
    PuzzlePreference puzzlePreference;
    String game;
    CountDownTimer timer;
    GameLevel gameLevel;
    private int targetMoves;
    private int remainingTime;
    boolean gamePaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        puzzlePreference = new PuzzlePreference(this);
        game = getIntent().getStringExtra("Game");
        init();
        binding.btnPause.setOnClickListener(view -> showPauseDialog());
    }

    @Override
    protected void onPause() {
        super.onPause();
        gamePaused = true;
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (game.equals("level") && gamePaused) {
            showPauseDialog();
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }

    private void showPauseDialog() {
        if (timer != null) {
            timer.cancel();
        }

        new AwesomeInfoDialog(PlayGameActivity.this)
                .setDialogIconOnly(R.drawable.pause)
                .setColoredCircle(R.color.colorBackground)
                .setTitle("Game Paused")
                .setMessage("Do you want to resume the game?")
                .setCancelable(false)
                .setPositiveButtonText("Resume")
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                .setPositiveButtonClick(() -> {
                    if (game.equals("level")) {
                        if (gameLevel.getType() == GameLevel.TYPE_TIME) {
                            startCounter(remainingTime);
                        }
                    }
                })
                .setNegativeButtonText("Exit")
                .setNegativeButtonTextColor(R.color.colorWhite)
                .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                .setNegativeButtonClick(this::finish).show();
    }

    private void init() {
        numberOfRows = 4;
        switch (game) {
            case "level":
                int level = getIntent().getIntExtra("Level", 1);
                gameLevel = GameLevel.getGameLevel(level);
                Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.puzzle_image);
                if (gameLevel.getType() == GameLevel.TYPE_MOVE) {
                    startNewMoveGame(image, gameLevel);
                } else {
                    startNewTimeGame(image, gameLevel);
                }
                break;
            case "custom":
                targetMoves = 0;
                startNewGame(Config.puzzleImage);
                break;
            case "online":
                playOnlineGame();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + game);
        }
    }

    private void startNewTimeGame(Bitmap image, GameLevel gameLevel) {
        startCounter(gameLevel.getTarget());
        startNewGame(image);
    }

    private void startCounter(int target) {
        binding.gameType.setText("Time : ");
        timer = new CountDownTimer(target * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = (int) millisUntilFinished / 1000;
                binding.gameTarget.setText(remainingTime + "s");
            }

            @Override
            public void onFinish() {
                binding.gameTarget.setText("0s");
                showGameOverDialog("time");
            }
        }.start();
    }

    private void showGameOverDialog(String message) {
        new AwesomeInfoDialog(PlayGameActivity.this)
                .setDialogIconOnly(R.drawable.dislike)
                .setColoredCircle(R.color.colorBackground)
                .setTitle("Game Over")
                .setMessage("You have failed to complete the puzzle in required " + message)
                .setCancelable(false)
                .setPositiveButtonText("Restart")
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                .setPositiveButtonClick(() -> init())
                .setNegativeButtonText("Exit")
                .setNegativeButtonTextColor(R.color.colorWhite)
                .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                .setNegativeButtonClick(() -> finish()).show();
    }

    private void startNewMoveGame(Bitmap image, GameLevel gameLevel) {
        binding.gameTarget.setText(String.valueOf(gameLevel.getTarget()));
        binding.gameType.setText("Moves : ");
        targetMoves = gameLevel.getTarget();
        startNewGame(image);
    }

    private void playOnlineGame() {
        String gameID = getIntent().getStringExtra("GameID");

        AwesomeInfoDialog dialog = new AwesomeInfoDialog(PlayGameActivity.this)
                .setDialogIconOnly(R.drawable.flag)
                .setColoredCircle(R.color.colorBackground)
                .setTitle("Waiting for opponent")
                .setMessage("Please wait while we connect you to your opponent")
                .setCancelable(false);

        reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Live").child(gameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    gameModel = snapshot.getValue(LiveGameModel.class);
                    if (gameModel != null) {
                        if (!gameModel.getWinner().isEmpty()) {
                            showOnlineWinDialog();
                        } else if (gameModel.getUserTwo().isEmpty()) {
                            dialog.show();
                        } else {
                            dialog.hide();
                            startOnlineGame();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlayGameActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOnlineWinDialog() {
        String winMessage;
        if (gameModel.getWinner().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName())) {
            winMessage = "Congratulations," + gameModel.getWinner() + "won the game!";
        } else {
            winMessage = "Oops," + gameModel.getWinner() + "won the game!";
        }
        new AwesomeInfoDialog(PlayGameActivity.this)
                .setDialogIconOnly(R.drawable.flag)
                .setColoredCircle(R.color.colorBackground)
                .setTitle("Game Over")
                .setMessage(winMessage)
                .setCancelable(false)
                .setNegativeButtonText("Exit")
                .setNegativeButtonTextColor(R.color.colorWhite)
                .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                .setNegativeButtonClick(this::finish).show();
    }

    private void startOnlineGame() {
        Toast.makeText(this, "Game is started", Toast.LENGTH_SHORT).show();
        Bitmap image = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.puzzle_image);
        startNewGame(image);
    }


    private void startNewGame(Bitmap image) {
        binding.orignalImage.setImageBitmap(image);
        deviceDimension = DeviceProperty.getDeviceDimension(PlayGameActivity.this);
        boardWidth = deviceDimension.x - 2 * PUZZLE_BOARD_LEFT_MARGIN;
        boardHeight = deviceDimension.y - 2 * PUZZLE_BOARD_LEFT_MARGIN;
        tileSize = boardWidth / numberOfRows;
        LinearLayout.LayoutParams boardParam = new LinearLayout.LayoutParams(boardWidth, boardWidth);
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
            if (i >= 4 && i <= 7) {
                yAxis = 1;
            } else if (i >= 8 && i <= 11) {
                yAxis = 2;
            } else if (i >= 12 && i <= 15) {
                yAxis = 3;
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
        int tileWidth = (boardWidth / numberOfRows);
        int bitmapPosition = 0;
        for (int i = 0; i < numberOfRows; i++) {
            for (int j = 0; j < numberOfRows; j++) {
                TileItem tile = new TileItem(getApplicationContext());
                tile.setId(bitmapPosition);
                tile.setNumber(bitmapPosition);
                tile.setStartingPosition(new Position(i, j));
                tile.setCorrectPosition(new Position(j, i));
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
        binding.puzzleFullBoardView.removeAllViews();
        for (Map.Entry<Integer, TileItem> entry : shuffledTiles.entrySet()) {
            TileItem item = entry.getValue();
            binding.puzzleFullBoardView.addView(item, item.setLayout());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.view_original_image) {
            displayOriginalImage(event);
            return true;
        }
        TileItem selectedTile = (TileItem) v;
        if (selectedTile.getIsBlank()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!checkIfValidMove(selectedTile))
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
                if (tileDraggedMoreThenHalfWay(selectedTile) || isJustClick(selectedTile)) {
                    swapTileWithEmpty(selectedTile);
                } else {
                    bringTileToOriginalPosition(selectedTile);
                }
                break;
        }
        return true;
    }

    private void checkMoves() {
        targetMoves--;
        binding.gameTarget.setText(String.valueOf(targetMoves));
        if (targetMoves == 0) {
            showGameOverDialog("Moves");
        }
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
        if (game.equals("level")) {
            if (gameLevel.getType() == GameLevel.TYPE_MOVE)
                checkMoves();
        } else if (game.equals("custom")) {
            incrementMoves();
        }

        if (isGameComplete()) {
            if (game.equals("online")) {
                FinishOnlineGame();
            } else {
                finishOfflineGame();
            }
        }
    }

    private void incrementMoves() {
        targetMoves++;
        binding.gameTarget.setText(String.valueOf(targetMoves));
    }

    private void FinishOnlineGame() {
        gameModel.setWinner(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
        gameModel.setStatus("Completed");
        reference.child("Live").child(gameModel.getGameID()).setValue(gameModel);
    }

    private void finishOfflineGame() {
        if (game.equals("level")) {
            if (gameLevel.getNumber() >= new PuzzlePreference(this).getCurrentLevel()) {
                new PuzzlePreference(this).setCurrentLevel(gameLevel.getNumber() + 1);
            }
            if (timer != null) {
                timer.cancel();
            }
            new AwesomeInfoDialog(PlayGameActivity.this)
                    .setDialogIconOnly(R.drawable.star)
                    .setColoredCircle(R.color.colorBackground)
                    .setTitle("Congratulations")
                    .setMessage("You have completed this level successfully.")
                    .setCancelable(false)
                    .setPositiveButtonText("Next Level")
                    .setPositiveButtonTextColor(R.color.colorWhite)
                    .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                    .setPositiveButtonClick(this::init)
                    .setNegativeButtonText("Exit")
                    .setNegativeButtonTextColor(R.color.colorWhite)
                    .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                    .setNegativeButtonClick(this::finish).show();
        } else {
            new AwesomeInfoDialog(PlayGameActivity.this)
                    .setDialogIconOnly(R.drawable.star)
                    .setColoredCircle(R.color.colorBackground)
                    .setTitle("Congratulations")
                    .setMessage("You have completed this level successfully.")
                    .setCancelable(false)
                    .setPositiveButtonText("Play Again")
                    .setPositiveButtonTextColor(R.color.colorWhite)
                    .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                    .setPositiveButtonClick(this::init)
                    .setNegativeButtonText("Exit")
                    .setNegativeButtonTextColor(R.color.colorWhite)
                    .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                    .setNegativeButtonClick(this::finish).show();
        }
    }

    private boolean isGameComplete() {
        for (Map.Entry<Integer, TileItem> entry : puzzleItemList.entrySet()) {
            TileItem item = entry.getValue();
            if (item.getCorrectPosition().getYAxis() != item.getCurrentPosition().getYAxis() || item.getCorrectPosition().getXAxis() != item.getCurrentPosition().getXAxis()) {
                Log.e("Shuffled tile", "Complete");
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
        if (selectedItem.isBelowOf(emptyTile) || selectedItem.isAboveOf(emptyTile)) {
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
        if (movableBoundary.contains(selectedTileBoundary)) {
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) selectedTile.getLayoutParams();
            param.height = tileSize;
            param.width = tileSize;
            if (selectedTile.isRightOf(emptyTile) || selectedTile.isLeftOf(emptyTile)) {
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
        int emptyTop = (emptyTile.getCurrentPosition().getYAxis() * tileSize) + boardTop;
        int emptyLeft = (emptyTile.getCurrentPosition().getXAxis() * tileSize) + boardLeft;
        int emptyRight = emptyLeft + tileSize;
        int emptyBottom = emptyTop + tileSize;
        int selectedItemTop = (selectedItem.getCurrentPosition().getYAxis() * tileSize) + boardTop;
        int selectedItemLeft = (selectedItem.getCurrentPosition().getXAxis() * tileSize) + boardLeft;
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
}