package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Date;

public class ResultPage extends AppCompatActivity {
    private static FirebaseAuthHelper firebaseAuthHelper;
    Button btnDone;
    DecimalFormat df = new DecimalFormat("0.00");
    TextView tvN, tvP, tvK, tvpH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);
        btnDone = findViewById(R.id.btnDone);
        tvN = findViewById(R.id.tvN);
        tvP = findViewById(R.id.tvP);
        tvK = findViewById(R.id.tvK);
        tvpH = findViewById(R.id.tvpH);

        firebaseAuthHelper = new FirebaseAuthHelper();
        displayValues();

        btnDone.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void displayValues(){
        if (getIntent().hasExtra("n_pred") &&
                getIntent().hasExtra("p_pred") &&
                getIntent().hasExtra("k_pred") &&
                getIntent().hasExtra("pH_pred")
        ) {
            double n = getIntent().getDoubleExtra("n_pred", 0);
            double p = getIntent().getDoubleExtra("p_pred", 0);
            double k = getIntent().getDoubleExtra("k_pred", 0);
            double pH = getIntent().getDoubleExtra("pH_pred", 0);

            tvN.setText(df.format(n));
            tvP.setText(df.format(p));
            tvK.setText(df.format(k));
            tvpH.setText(df.format(pH));
        } else {
            Toast.makeText(this, "Values are not found", Toast.LENGTH_SHORT).show();
            finish();
        }
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