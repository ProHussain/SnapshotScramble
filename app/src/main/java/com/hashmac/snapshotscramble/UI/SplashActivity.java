package com.hashmac.snapshotscramble.UI;

import static com.hashmac.snapshotscramble.Utils.Config.PROGRESS_INTERVAL;
import static com.hashmac.snapshotscramble.Utils.Config.SPLASH_DURATION;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hashmac.snapshotscramble.Utils.Config;
import com.hashmac.snapshotscramble.databinding.ActivitySplashBinding;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    ActivitySplashBinding binding;
    private int mProgress = 0;
    private Handler mHandler;
    private Runnable mProgressRunnable;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        binding.progressBar.setMax(SPLASH_DURATION);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Config.IS_LOGIN = true;
        }

        mHandler = new Handler();
        mProgressRunnable = new Runnable() {
            @Override
            public void run() {
                // Update the progress bar
                mProgress += PROGRESS_INTERVAL;
                binding.progressBar.setProgress(mProgress);

                // Check if the splash timer has expired
                if (mProgress >= SPLASH_DURATION) {
                    // Stop the animation
                    binding.lottieAnimation.cancelAnimation();

                    // Move on to the next activity
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Continue updating the progress bar
                    mHandler.postDelayed(mProgressRunnable, PROGRESS_INTERVAL);
                }
            }
        };

        // Start the progress update loop
        mHandler.postDelayed(mProgressRunnable, PROGRESS_INTERVAL);

    }

}