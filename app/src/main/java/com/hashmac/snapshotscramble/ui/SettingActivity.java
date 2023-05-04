package com.hashmac.snapshotscramble.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.hashmac.snapshotscramble.AppController;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.databinding.ActivitySettingBinding;
import com.hashmac.snapshotscramble.utils.Config;
import com.hashmac.snapshotscramble.utils.PuzzlePreference;

public class SettingActivity extends BaseActivity {
    ActivitySettingBinding binding;
    PuzzlePreference puzzlePreference;
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
        binding.linearLayoutLeaderBoard.setOnClickListener(view -> startActivity(new Intent(this, LeaderBoardActivity.class)));
        binding.linearLayoutRate.setOnClickListener(v-> rateApp());
        binding.linearLayoutShare.setOnClickListener(v-> shareApp());
        binding.linearLayoutMoreApps.setOnClickListener(v-> moreApps());
        binding.linearLayoutFeedback.setOnClickListener(v-> sendFeedback());
        binding.linearLayoutPrivacyPolicy.setOnClickListener(v-> privacyPolicy());
        binding.btnSignout.setOnClickListener(view -> signOut());
    }

    private void privacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://www.google.com"));
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Hi " + getString(R.string.developer_name) + ",");
        startActivity(Intent.createChooser(intent, "Send Feedback"));
    }

    private void moreApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://play.google.com/store/apps/developer?id=" + getString(R.string.developer_id)));
        startActivity(intent);
    }

    private void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Wallcraft");
        intent.putExtra(Intent.EXTRA_TEXT, "Get " + getString(R.string.app_name) + " to get the best wallpapers for your phone: https://play.google.com/store/apps/details?id=" + getPackageName());
        startActivity(Intent.createChooser(intent, "Share App"));
    }

    private void signOut() {
        new AwesomeInfoDialog(this)
                .setTitle("Sign out")
                .setMessage("Are you sure you want to sign out?")
                .setColoredCircle(R.color.colorPrimaryLight)
                .setDialogIconOnly(R.drawable.flag)
                .setCancelable(true)
                .setPositiveButtonText("Yes")
                .setPositiveButtonbackgroundColor(R.color.colorPrimary)
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setNegativeButtonText("No")
                .setNegativeButtonbackgroundColor(R.color.colorPrimary)
                .setNegativeButtonTextColor(R.color.colorWhite)
                .setPositiveButtonClick(() -> {
                    Config.IS_LOGIN = false;
                    Config.user = null;
                    FirebaseAuth.getInstance().signOut();
                    loadSettings();
                }).setNegativeButtonClick(() -> {})
                .show();
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
        AppController appController = (AppController) getApplicationContext();
        appController.loadPreferences();
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

    @Override
    protected void onPause() {
        super.onPause();
        Config.stopMusic(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Config.checkMusic(this);
    }
}