package com.hashmac.snapshotscramble.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.Utils.FirebaseAuthHelper;
import com.hashmac.snapshotscramble.Utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivitySettingBinding;
import com.hashmac.snapshotscramble.databinding.DialogDifficultiesBinding;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;
    PuzzlePreference puzzlePreference;
    FirebaseAuthHelper authHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBbinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        puzzlePreference = new PuzzlePreference(this);
        binding.backBtnImage.setOnClickListener(view -> finish());
        LoadSettings();


        binding.soundSwitch.setOnClickListener(view -> ChangeSoundSetting());

        binding.vibrationSwitch.setOnClickListener(view -> ChangeVibrationSetting());

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
        binding.tvDifficulties.setText("Easy");
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
        LoadSettings();
    }
}