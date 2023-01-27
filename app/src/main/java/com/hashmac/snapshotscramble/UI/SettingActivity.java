package com.hashmac.snapshotscramble.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.Utils.FirebaseAuthHelper;
import com.hashmac.snapshotscramble.Utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivitySettingBinding;
import com.hashmac.snapshotscramble.databinding.DialogDifficultiesBinding;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    private static final int SIGN_IN_CODE = 101;
    private static final String TAG = "SettingActivity";
    ActivitySettingBinding binding;
    PuzzlePreference puzzlePreference;
    FirebaseAuthHelper authHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        puzzlePreference = new PuzzlePreference(this);
        binding.backBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        LoadSettings();

        binding.tvDifficulties.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDifficultiesDialog();
            }
        });

        binding.soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeSoundSetting();
            }
        });

        binding.vibrationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeVibrationSetting();
            }
        });

        binding.btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleLogin();
            }
        });
    }

    private void GoogleLogin() {
        authHelper = new FirebaseAuthHelper(this);
        authHelper.signInWithGoogle(this,SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            SignInResult(data);
        }
    }

    private void SignInResult(@Nullable Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        authHelper.handleSignInResult(task, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SettingActivity.this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    assert user != null;
                    binding.tvUserId.setText(user.getUid());
                    Config.IS_LOGIN = true;
                    binding.btnLoginGoogle.setVisibility(View.GONE);
                } else {
                    Toast.makeText(SettingActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ChangeVibrationSetting() {
        boolean vibration = puzzlePreference.isVibrationEnabled();
        puzzlePreference.setSoundEnabled(!vibration);
    }

    private void ChangeSoundSetting() {
        boolean sound = puzzlePreference.isSoundEnabled();
        puzzlePreference.setSoundEnabled(!sound);
    }

    private void LoadSettings() {
        binding.soundSwitch.setChecked(puzzlePreference.isSoundEnabled());
        binding.vibrationSwitch.setChecked(puzzlePreference.isVibrationEnabled());
        binding.tvDifficulties.setText(puzzlePreference.getDifficultyLevel());
        if (Config.IS_LOGIN) {
            binding.btnLogout.setVisibility(View.VISIBLE);
            binding.btnLoginGoogle.setVisibility(View.GONE);
            binding.tvUserId.setText(FirebaseAuth.getInstance().getUid());
        } else {
            binding.btnLogout.setVisibility(View.GONE);
            binding.btnLoginGoogle.setVisibility(View.VISIBLE);
            binding.tvUserId.setText(getString(R.string.login_required));
        }
    }

    private void ShowDifficultiesDialog() {
        DialogDifficultiesBinding difficultiesBinding = DialogDifficultiesBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(difficultiesBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        difficultiesBinding.difficultyEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeDifficulty("Easy");
                dialog.dismiss();
            }
        });

        difficultiesBinding.difficultyMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeDifficulty("Medium");
                dialog.dismiss();
            }
        });

        difficultiesBinding.difficultyHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeDifficulty("Hard");
                dialog.dismiss();
            }
        });
    }

    private void ChangeDifficulty(String level) {
        puzzlePreference.setDifficultyLevel(level);
        LoadSettings();
    }
}