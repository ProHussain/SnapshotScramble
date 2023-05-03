package com.hashmac.snapshotscramble.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.utils.Config;
import com.hashmac.snapshotscramble.utils.FirebaseAuthHelper;
import com.hashmac.snapshotscramble.utils.PuzzlePreference;
import com.hashmac.snapshotscramble.adapter.GameLevelAdapter;
import com.hashmac.snapshotscramble.databinding.ActivityMainBinding;
import com.hashmac.snapshotscramble.databinding.DialogCreateOnlineGameBinding;
import com.hashmac.snapshotscramble.databinding.DialogJoinAGameBinding;
import com.hashmac.snapshotscramble.models.LiveGameModel;
import com.hashmac.snapshotscramble.models.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by HashMac on 3/5/2023
 * Activity for the Game Level offline
 * Purpose: To display the game levels
 * 1. Select the level with recycler view
 * 2. Play default level with Play button
 * 3. Challenge friend with Challenge button need to login
 * if not login, show login dialog
 * 4. How to play with info button
 */

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    PuzzlePreference puzzlePreference;
    private final int GALLERY_REQUEST_CODE = 11;
    private FirebaseAuthHelper authHelper;
    private int SIGN_IN_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.d("OpenCV", "OpenCV loaded");
        }
        puzzlePreference = new PuzzlePreference(this);
        binding.btnPlay.setOnClickListener(v -> playGame());
        binding.btnChallenge.setOnClickListener(v -> challengeFriend());
        binding.btnInfo.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HelpActivity.class)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Config.stopMusic(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.rvGameLevel.setAdapter(new GameLevelAdapter(puzzlePreference.getCurrentLevel()));
        Config.checkMusic(this);
    }


    private void challengeFriend() {
        if (Config.IS_LOGIN) {
            createChallenge();
        } else {
            new AwesomeInfoDialog(MainActivity.this)
                    .setDialogIconOnly(R.drawable.flag)
                    .setColoredCircle(R.color.colorBackground)
                    .setTitle("Login")
                    .setMessage("You need to login to challenge your friend")
                    .setCancelable(true)
                    .setPositiveButtonText("Login")
                    .setPositiveButtonTextColor(R.color.colorWhite)
                    .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                    .setPositiveButtonClick(this::login)
                    .setNegativeButtonText("Cancel")
                    .setNegativeButtonTextColor(R.color.colorWhite)
                    .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                    .setNegativeButtonClick(this::cancelLogin)
                    .show();
        }
    }

    private void createChallenge() {
        new AwesomeInfoDialog(MainActivity.this)
                .setDialogIconOnly(R.drawable.flag)
                .setColoredCircle(R.color.colorBackground)
                .setTitle("Challenge Friend")
                .setMessage("Create your challenge or Join your friend challenge")
                .setCancelable(true)
                .setPositiveButtonText("Create")
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                .setPositiveButtonClick(this::createChallengeDialog)
                .setNegativeButtonText("Join")
                .setNegativeButtonTextColor(R.color.colorWhite)
                .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                .setNegativeButtonClick(this::joinChallengeDialog).show();
    }

    private void joinChallengeDialog() {
        DialogJoinAGameBinding joinAGameBinding = DialogJoinAGameBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(joinAGameBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        joinAGameBinding.startGameBtn.setOnClickListener(view -> {
            String gameID = joinAGameBinding.tvGameId.getText().toString().trim();
            if (!gameID.isEmpty())
                checkGameID(gameID);
        });
    }

    private void checkGameID(String gameID) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Live").child(gameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    LiveGameModel model = snapshot.getValue(LiveGameModel.class);
                    assert model != null;
                    if (model.getWinner().isEmpty()) {
                        model.setUserTwo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
                        model.setStatus("Start");
                        startOnlineGame(model);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please check your ID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createChallengeDialog() {
        long timestamp = System.currentTimeMillis();
        String gameID = Config.generateUniqueId(String.valueOf(timestamp));
        DialogCreateOnlineGameBinding createOnlineGameBinding = DialogCreateOnlineGameBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(createOnlineGameBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        createOnlineGameBinding.tvGameId.setText(gameID);
        createOnlineGameBinding.copyIdBtn.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", gameID);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(MainActivity.this, "ID Copied", Toast.LENGTH_SHORT).show();
        });

        createOnlineGameBinding.shareIdBtn.setOnClickListener(view -> shareGameID(gameID));

        createOnlineGameBinding.startGameBtn.setOnClickListener(view -> {
            dialog.dismiss();
            String name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
            LiveGameModel model = new LiveGameModel(gameID, name, "", "Waiting", "");
            startOnlineGame(model);
        });
    }

    private void shareGameID(String gameID) {
        String shareMessage = "Join me in solving challenging puzzles on the Puzzle game! Download the app now and let's play together! https://play.google.com/store/apps/details?id="
                + this.getPackageName() + "\n\n\n Here is your Game ID: " + gameID;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(shareIntent);
    }

    private void startOnlineGame(LiveGameModel model) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Live").child(model.getGameID()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(MainActivity.this, PlayGameActivity.class);
                    intent.putExtra("Game", "online");
                    intent.putExtra("GameID", model.getGameID());
                    Log.e("Game", "Start");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelLogin() {
        // do nothing
    }

    private void login() {
        authHelper = new FirebaseAuthHelper(this);
        authHelper.signInWithGoogle(this,SIGN_IN_CODE);
    }



    private void playGame() {
        new AwesomeInfoDialog(MainActivity.this)
                .setDialogIconOnly(R.drawable.play_24)
                .setColoredCircle(R.color.colorBackground)
                .setTitle("Start Game")
                .setMessage("Select your image or play default image")
                .setCancelable(true)
                .setPositiveButtonText("Play Default")
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonbackgroundColor(R.color.colorPrimaryDark)
                .setPositiveButtonClick(this::playLevel)
                .setNegativeButtonText("Select Your Image")
                .setNegativeButtonTextColor(R.color.colorWhite)
                .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                .setNegativeButtonClick(this::playCustom).show();
    }

    private void playLevel() {
        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.putExtra("Game", "level");
        intent.putExtra("Level", puzzlePreference.getCurrentLevel());
        startActivity(intent);
    }

    private void playCustom() {
        pickYourImage();
    }

    private void pickYourImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedImageUri);
                    Config.puzzleImage = convertToCartoonFilter(image,7, 9, 9, 7, 7, 9);
                    Intent intent = new Intent(MainActivity.this, PlayGameActivity.class);
                    intent.putExtra("Game", "custom");
                    startActivity(intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (requestCode == SIGN_IN_CODE) {
            if (data == null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }
            createUser(data);
        }
    }

    private void createUser(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        authHelper.handleSignInResult(task, task1 -> {
            if (task1.isSuccessful()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    checkDatabase(user.getUid());
                } else {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkDatabase(String id) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            User user = new User(id, account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString());
            Config.user = user;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("Users").child(id).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Config.IS_LOGIN = true;
                    Toast.makeText(this, "Sign In successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap convertToCartoonFilter(Bitmap origBitmapImage, int numBilateral, int bDiameter, double sigmaColor, double sigmaSpace, int mDiameter, int eDiameter) {
        Mat imgMat = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        Mat tempMat1 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);
        Mat tempMat2 = new Mat(origBitmapImage.getHeight(), origBitmapImage.getWidth(), CvType.CV_8UC3);

        Utils.bitmapToMat(origBitmapImage, imgMat);

        imgMat.copyTo(tempMat1);
        imgMat.copyTo(tempMat2);
        Imgproc.cvtColor(tempMat1, tempMat1, Imgproc.COLOR_BGRA2RGB); //tempMat1 RGB
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_BGRA2RGB); //tempMat2 RGB
        for(int i=0; i < 2; i++) {
            Imgproc.pyrDown(tempMat1, tempMat1);
        }
        for(int i=0; i < numBilateral; i++) {
            Imgproc.bilateralFilter(tempMat1, tempMat2, bDiameter, sigmaColor, sigmaSpace);
            System.gc();
            tempMat2.copyTo(tempMat1);
        }
        for(int i=0; i < 2; i++) {
            Imgproc.pyrUp(tempMat1, tempMat1);
        }
        Imgproc.resize(tempMat1, tempMat1, imgMat.size());
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(imgMat, tempMat2, Imgproc.COLOR_RGB2GRAY); //tempMat2 Gray
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.medianBlur(tempMat2, imgMat, mDiameter); //

        Imgproc.adaptiveThreshold(imgMat, tempMat2, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, eDiameter, 2);
        Imgproc.cvtColor(tempMat2, tempMat2, Imgproc.COLOR_GRAY2RGB);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_GRAY2RGB);
        Core.bitwise_and(tempMat1, tempMat2, imgMat);
        Imgproc.cvtColor(imgMat, imgMat, Imgproc.COLOR_RGB2BGRA);
        Utils.matToBitmap(imgMat, origBitmapImage);
        Bitmap finalBitmapImage = origBitmapImage.copy(origBitmapImage.getConfig(), true);
        imgMat.release();
        tempMat1.release();
        tempMat2.release();
        return finalBitmapImage;
    }

}