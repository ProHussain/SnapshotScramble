package com.hashmac.snapshotscramble.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.airbnb.lottie.L;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hashmac.snapshotscramble.adapter.LeaderBoardAdapter;
import com.hashmac.snapshotscramble.models.LeaderBoard;
import com.hashmac.snapshotscramble.utils.Config;
import com.hashmac.snapshotscramble.utils.PuzzlePreference;
import com.hashmac.snapshotscramble.databinding.ActivityLeaderBoardBinding;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoardActivity extends AppCompatActivity {
    ActivityLeaderBoardBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLeaderBoardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        binding.backBtnImage.setOnClickListener(view -> finish());
        loadBoard();
    }

    private void loadBoard() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("leaderboard");
        ref.orderByChild("score").limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LeaderBoard> leaderBoardList = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    LeaderBoard leaderBoard = ds.getValue(LeaderBoard.class);
                    leaderBoardList.add(0,leaderBoard);
                }
                binding.rvLeaderBoard.setAdapter(new LeaderBoardAdapter(leaderBoardList));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LeaderBoardActivity", "onCancelled: " + error.getMessage());
            }
        });
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