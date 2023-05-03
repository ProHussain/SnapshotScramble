package com.hashmac.snapshotscramble.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.Utils.FirebaseAuthHelper;
import com.hashmac.snapshotscramble.Utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivitySettingBinding;
import com.hashmac.snapshotscramble.databinding.DialogDifficultiesBinding;

public class SettingActivity extends AppCompatActivity {
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
        binding.imgBack.setOnClickListener(view -> finish());
        loadSettings();

        binding.linearLayoutSound.setOnClickListener(view -> changeSoundSetting());
        binding.linearLayoutVibration.setOnClickListener(view -> changeVibrationSetting());

    }
    

    private void changeVibrationSetting() {
        boolean vibration = puzzlePreference.isVibrationEnabled();
        puzzlePreference.setVibrationEnabled(!vibration);
        loadSettings();
    }

    private void changeSoundSetting() {
        boolean sound = puzzlePreference.isSoundEnabled();
        puzzlePreference.setSoundEnabled(!sound);
        loadSettings();
    }

    private void loadSettings() {
        if (puzzlePreference.isSoundEnabled()) {
            binding.imgSound.setImageResource(R.drawable.ic_sound);
            binding.txtSound.setText("Music effect is enabled");
        } else {
            binding.imgSound.setImageResource(R.drawable.ic_no_sound);
            binding.txtSound.setText("Music effect is disabled");
        }

        if (puzzlePreference.isVibrationEnabled()) {
            binding.txtVibration.setText("Vibration is enabled");
        } else {
            binding.txtVibration.setText("Vibration is disabled");
        }

        if (Config.IS_LOGIN) {
            binding.btnSignout.setVisibility(View.VISIBLE);
        } else {
            binding.btnSignout.setVisibility(View.GONE);
        }
    }
}