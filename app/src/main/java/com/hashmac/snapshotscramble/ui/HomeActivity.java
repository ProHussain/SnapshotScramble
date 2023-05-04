package com.hashmac.snapshotscramble.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hashmac.snapshotscramble.databinding.ActivityHomeBinding;
import com.hashmac.snapshotscramble.models.User;
import com.hashmac.snapshotscramble.R;
import com.hashmac.snapshotscramble.utils.Config;
import com.hashmac.snapshotscramble.utils.FirebaseAuthHelper;

import java.util.Objects;

/**
 * Created by Hashmac on 5/2/2023
 * Home activity for the app
 * Purpose: To provide the home screen for the app
 * 1. Show the app logo
 * 2. Show the info button to show how to play the game
 * 3. Show the Start button to start the game
 * 4. Show the My Account button if the user is logged in then show the My Account button else show the Login button
 * 5. Login Dialog will be shown to login with google
 * 6. My Account Dialog will be shown to show the user info
 */

public class HomeActivity extends BaseActivity {
    ActivityHomeBinding binding;
    private final int SIGN_IN_CODE = 1;
    private FirebaseAuthHelper authHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnMyAccount.setText(Config.IS_LOGIN ? "My Account" : "Login");
        binding.btnStartGame.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, MainActivity.class)));
        binding.btnMyAccount.setOnClickListener(view -> myAccount());
        binding.imgInfo.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, SettingActivity.class)));
    }

    private void myAccount() {
        if (Config.IS_LOGIN) {
            showAccountDialog();
        } else {
            showLoginDialog();
        }
    }

    private void showAccountDialog() {
        new AwesomeInfoDialog(this)
                .setDialogIconOnly(R.drawable.google)
                .setColoredCircle(R.color.colorWhite)
                .setTitle("My Account")
                .setCancelable(true)
                .setMessage("User Name: " + Config.user.getName() + "\n" +
                        "User Email: " + Config.user.getEmail() + "\n" +
                        "User Id: " + Config.user.getId())
                .setPositiveButtonText("Logout")
                .setPositiveButtonbackgroundColor(R.color.colorPrimary)
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonClick(this::logout)
                .show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Config.IS_LOGIN = false;
        Config.user = null;
        binding.btnMyAccount.setText("Login");
        Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show();
    }

    private void showLoginDialog() {
        new AwesomeInfoDialog(this)
                .setDialogIconOnly(R.drawable.google)
                .setColoredCircle(R.color.colorWhite)
                .setTitle("Login")
                .setCancelable(true)
                .setMessage("Login with Google to play the game with your friends")
                .setPositiveButtonText("Login")
                .setPositiveButtonbackgroundColor(R.color.colorPrimary)
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonClick(this::googleLogin)
                .show();
    }

    private void googleLogin() {
        authHelper = new FirebaseAuthHelper(this);
        authHelper.signInWithGoogle(this,SIGN_IN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            if (data == null) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }
            createUser(data);
        }
    }

    private void createUser(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        authHelper.handleSignInResult(task, task1 -> {
            if (task1.isSuccessful()) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    checkDatabase(user.getUid());
                } else {
                    Toast.makeText(HomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HomeActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkDatabase(String id) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            User user = new User(id, account.getDisplayName(), account.getEmail(), account.getPhotoUrl().toString());
            Config.user = user;
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            reference.child("Users").child(id).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Config.IS_LOGIN = true;
                    binding.btnMyAccount.setText("My Account");
                    Toast.makeText(this, "Sign In successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        new AwesomeInfoDialog(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit?")
                .setColoredCircle(R.color.colorPrimaryLight)
                .setDialogIconOnly(R.drawable.flag)
                .setCancelable(true)
                .setPositiveButtonText("Exit")
                .setPositiveButtonbackgroundColor(R.color.colorPrimary)
                .setPositiveButtonTextColor(R.color.colorWhite)
                .setPositiveButtonClick(this::finish)
                .show();
    }
}