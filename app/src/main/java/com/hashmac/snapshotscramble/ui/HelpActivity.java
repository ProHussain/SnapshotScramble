package com.hashmac.snapshotscramble.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.hashmac.snapshotscramble.databinding.ActivityHelpBinding;

public class HelpActivity extends BaseActivity {
    ActivityHelpBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }
    private void init() {
        binding.backBtnImage.setOnClickListener(view -> finish());
    }
}