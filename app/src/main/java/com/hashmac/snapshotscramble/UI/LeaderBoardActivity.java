package com.hashmac.snapshotscramble.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.hashmac.snapshotscramble.Utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivityLeaderBoardBinding;

public class LeaderBoardActivity extends AppCompatActivity {
    ActivityLeaderBoardBinding binding;
    PuzzlePreference puzzlePreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderBoardBinding.inflate(getLayoutInflater());
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
        LoadScores();
    }

    private void LoadScores() {
        binding.easyHighScore.setText(puzzlePreference.getScoreEasy());
        binding.mediumHighScore.setText(puzzlePreference.getScoreMedium());
        binding.advancedHighScore.setText(puzzlePreference.getScoreHard());
        binding.onlineHighScore.setText(puzzlePreference.getScoreOnline());
    }
}