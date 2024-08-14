package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;
    private static FirebaseAuthHelper firebaseAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNav);
        firebaseAuthHelper = new FirebaseAuthHelper();

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Fragment newFragment = new Home();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                fragmentTransaction.commit();
                return true;
            } else if (item.getItemId() == R.id.nav_history) {
                Fragment newFragment = new History();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainerView, newFragment);
                fragmentTransaction.commit();
                return true;
            }
            return false;
        });
    }
    private boolean isNightModeEnabled() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("NightMode", false);
    }
    private void changeTheme(){
        if (isNightModeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuthHelper.getCurrentUser();
        if(user == null){
            firebaseAuthHelper.signInAnonymously(task -> {
                if(task.isSuccessful()){
                    Log.d(TAG, "Successfully to created an anonymous account");
                }else{
                    Log.d(TAG, "Failed to create anonymous account");
                }
            });
        }
    }
}