package com.example.snapsoil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class ViewingPage extends AppCompatActivity {
    private static FirebaseAuthHelper firebaseAuthHelper;
    ImageView imCapImg;
    Button btnProceed, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing_page);
        imCapImg = findViewById(R.id.imCapImg);
        btnProceed = findViewById(R.id.btnProceed);
        btnCancel = findViewById(R.id.btnSaveChangesCancel);

        firebaseAuthHelper = new FirebaseAuthHelper();
        displayImg();

        btnProceed.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ResultPage.class);
            startActivity(intent);
            finish();
        });

        btnCancel.setOnClickListener(v -> onBackPressed());
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
    private void  displayImg(){
        if (getIntent().hasExtra("imageUri")) {
            String imageUriString = getIntent().getStringExtra("imageUri");
            Uri imageUri = Uri.parse(imageUriString);

            ImageView imageView = findViewById(R.id.imCapImg);
            imageView.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "No image URI found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuthHelper.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}