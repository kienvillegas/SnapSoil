package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ViewingPage extends AppCompatActivity {
    private static FirebaseAuthHelper firebaseAuthHelper;
    private static FirebaseFirestoreHelper firebaseFirestoreHelper;

    ImageView imCapImg;
    Button btnProceed, btnCancel;
    private NetworkRequestManager requestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing_page);
        imCapImg = findViewById(R.id.imCapImg);
        btnProceed = findViewById(R.id.btnProceed);
        btnCancel = findViewById(R.id.btnSaveChangesCancel);

        firebaseAuthHelper = new FirebaseAuthHelper();
        firebaseFirestoreHelper = new FirebaseFirestoreHelper();
        displayImg();


        btnProceed.setOnClickListener(v -> {
            Uri imageUri = Uri.parse(getImg());
            requestManager = new NetworkRequestManager();
            requestManager.requestPrediction(getApplicationContext(), imageUri, new PredictionRequestListenter() {
                @Override
                public void onRequestCompleted(HistoryData data) {
                    double n, p, k, pH;
                    n = data.getNitrogen();
                    p = data.getPhosphorus();
                    k = data.getPotassium();
                    pH = data.getpH();
                    addToHistory(data);

                    Intent intent = new Intent(getApplicationContext(), ResultPage.class);
                    intent.putExtra("n_pred", n);
                    intent.putExtra("p_pred", p);
                    intent.putExtra("k_pred", k);
                    intent.putExtra("pH_pred", pH);
                    startActivity(intent);
                }
                @Override
                public void onRequestFailed(String errorMessage) {
                    Log.e(TAG, "proceedButton: " + errorMessage);
                }
            });
        });

        btnCancel.setOnClickListener(v -> onBackPressed());
    }
    private void addToHistory(HistoryData data){
        String id = firebaseAuthHelper.getUserId();
        firebaseFirestoreHelper.addHistory(data, id, task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "addToHistory: Success");
            }else{
                Log.d(TAG, "addToHistory: " + task.getException().toString());
            }
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
    private String getImg(){
        if (getIntent() != null && getIntent().hasExtra("imageUri")) {
            String imageUriString = getIntent().getStringExtra("imageUri");
            return imageUriString;
        } else {
            Toast.makeText(this, "No image URI found in intent", Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
    }
    private void displayImg() {
        String imageUriString = getImg();
        if (imageUriString != null) {
            Uri imageUri = Uri.parse(imageUriString);
            Log.d(TAG, "displayImg: " + imageUri);
            imCapImg.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Image URI string is null", Toast.LENGTH_SHORT).show();
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