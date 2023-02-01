package com.hashmac.snapshotscramble.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import com.hashmac.snapshotscramble.databinding.ActivityMainBinding;
import com.hashmac.snapshotscramble.databinding.DialogCreateOnlineGameBinding;
import com.hashmac.snapshotscramble.databinding.DialogGamePlayBinding;
import com.hashmac.snapshotscramble.databinding.DialogJoinAGameBinding;
import com.hashmac.snapshotscramble.databinding.DialogOfflinePlayBinding;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.databinding.DialogOnlinePlayBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    ActivityMainBinding binding;
    private static final int GALLERY_REQUEST_CODE = 1;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        if (Config.UPDATE_AVAILABLE) {
            showUpdateDialog("v2.1");
        }
        reference = FirebaseDatabase.getInstance().getReference();
        binding.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayGame();
            }
        });

        binding.leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LeaderBoardScreen();
            }
        });

        binding.shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HelpScreen();
            }
        });

        binding.settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingScreen();
            }
        });

        binding.exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExitDialog();
            }
        });
    }

    private void PlayGame() {
        DialogGamePlayBinding gamePlayBinding = DialogGamePlayBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gamePlayBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();
        gamePlayBinding.offlinePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                PlayOfflineDialog();
            }
        });
        gamePlayBinding.onlinePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                PlayOnlineDialog();
            }
        });
    }

    private void LeaderBoardScreen() {
        startActivity(new Intent(MainActivity.this, LeaderBoardActivity.class));
    }

    private void HelpScreen() {
        startActivity(new Intent(MainActivity.this, HelpActivity.class));
    }

    private void SettingScreen() {
        startActivity(new Intent(MainActivity.this, SettingActivity.class));
    }

    private void ExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit the game?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showUpdateDialog(final String newVersion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Update Available");
        builder.setMessage("A new version " + newVersion + " of the game is available. Do you want to update now?");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void PlayOnlineDialog() {
        if (Config.IS_LOGIN) {
            DialogOnlinePlayBinding onlinePlayBinding = DialogOnlinePlayBinding.inflate(getLayoutInflater());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(onlinePlayBinding.getRoot());
            AlertDialog dialog = builder.create();
            dialog.show();
            onlinePlayBinding.createGameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    CreateOnlineGame();
                }
            });

            onlinePlayBinding.joinGameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    JoinOnlineGame();
                    dialog.dismiss();
                }
            });

        } else {
            Toast.makeText(this, "You need to login first from setting", Toast.LENGTH_SHORT).show();
        }
    }

    private void CreateOnlineGame() {
        long timestamp = System.currentTimeMillis();
        String gameID = Config.generateUniqueId(String.valueOf(timestamp));
        DialogCreateOnlineGameBinding createOnlineGameBinding = DialogCreateOnlineGameBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(createOnlineGameBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();
        createOnlineGameBinding.tvGameId.setText(gameID);
        createOnlineGameBinding.copyIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", gameID);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "ID Copied", Toast.LENGTH_SHORT).show();
            }
        });

        createOnlineGameBinding.shareIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareGameID(gameID);
            }
        });

        createOnlineGameBinding.startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String name = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
                LiveGameModel model = new LiveGameModel(gameID, name, "", "Waiting", "");
                StartOnlineGame(model);
            }
        });
    }

    private void ShareGameID(String gameID) {
        String shareMessage = "Join me in solving challenging puzzles on the Puzzle game! Download the app now and let's play together! https://play.google.com/store/apps/details?id="
                + this.getPackageName() + "\n\n\n Here is your Game ID: " + gameID;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(shareIntent);
    }

    private void JoinOnlineGame() {
        DialogJoinAGameBinding joinAGameBinding = DialogJoinAGameBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(joinAGameBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();
        joinAGameBinding.startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String gameID = joinAGameBinding.tvGameId.getText().toString().trim();
                if (!gameID.isEmpty())
                    CheckGameID(gameID);
            }
        });
    }

    private void CheckGameID(String gameID) {
        reference.child("Live").child(gameID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    LiveGameModel model = snapshot.getValue(LiveGameModel.class);
                    assert model != null;
                    if (model.getWinner().isEmpty()) {
                        model.setUserTwo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
                        model.setStatus("Start");
                        StartOnlineGame(model);
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

    private void PlayOfflineDialog() {
        boolean resume = false; // get Data from local Database
        DialogOfflinePlayBinding offlinePlayBinding = DialogOfflinePlayBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(offlinePlayBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();
        if (resume) {
            offlinePlayBinding.resumeBtn.setVisibility(View.VISIBLE);
        } else {
            offlinePlayBinding.resumeBtn.setVisibility(View.GONE);
        }

        offlinePlayBinding.resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ResumeGame();
            }
        });

        offlinePlayBinding.newGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartNewGame();
                dialog.dismiss();
            }
        });

        offlinePlayBinding.customImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectCustomImage();
                dialog.dismiss();
            }
        });

    }

    private void SelectCustomImage() {
        pickFromGallery();
    }

    private void StartNewGame() {
        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.putExtra("Game", "new");
        startActivity(intent);
    }

    private void ResumeGame() {
        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.putExtra("Game", "resume");
        startActivity(intent);
    }

    private void StartOnlineGame(LiveGameModel model) {
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
                    Log.e(TAG, Objects.requireNonNull(task.getException()).getMessage());
                    Log.e(TAG, "Error there");
                    Toast.makeText(MainActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pickFromGallery() {
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
                    Config.puzzleImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), croppedImageUri);
                    Intent intent = new Intent(this, PlayGameActivity.class);
                    intent.putExtra("Game", "custom");
                    startActivity(intent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}