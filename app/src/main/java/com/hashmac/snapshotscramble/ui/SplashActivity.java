package com.hashmac.snapshotscramble.ui;

import static com.hashmac.snapshotscramble.utils.Config.PROGRESS_INTERVAL;
import static com.hashmac.snapshotscramble.utils.Config.SPLASH_DURATION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hashmac.snapshotscramble.utils.Config;
import com.hashmac.snapshotscramble.databinding.ActivitySplashBinding;

/**
 * Created by Hashmac on 5/2/2023
 * Splash activity for the app
 * Purpose: To show the splash screen for the app
 * 1. Show the splash screen for 3 seconds
 * 2. Check if the user is logged in or not
 * 3. If the user is logged in, then save the user details in the Config class
 */

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
    ActivitySplashBinding binding;
    private int mProgress = 0;
    private Handler mHandler;
    private Runnable mProgressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseAnalytics.getInstance(this);
        binding.progressBar.setMax(SPLASH_DURATION);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists())
                        Config.user = snapshot.getValue(com.hashmac.snapshotscramble.models.User.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        Config.IS_LOGIN = user != null;
        mHandler = new Handler(Looper.getMainLooper());
        mProgressRunnable = this::run;
        mHandler.postDelayed(mProgressRunnable, PROGRESS_INTERVAL);
    }

    private void run() {
        mProgress += PROGRESS_INTERVAL;
        binding.progressBar.setProgress(mProgress);
        if (mProgress >= SPLASH_DURATION) {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            finish();
        } else {
            mHandler.postDelayed(mProgressRunnable, PROGRESS_INTERVAL);
        }
    }
}