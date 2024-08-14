package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SplashScreen extends AppCompatActivity {
    Button btnStart;
    private static FirebaseAuthHelper firebaseAuthHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_sreen);
        btnStart = findViewById(R.id.btnStart);

        firebaseAuthHelper = new FirebaseAuthHelper();
        btnStart.setOnClickListener(v -> {
            firebaseAuthHelper.signInAnonymously(task -> {
                if(task.isSuccessful()){
                    Log.d(TAG, "Successfully Signed In Anonymously");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Log.d(TAG, "Error: " + task.getException().toString());
                    Toast.makeText(this, "Failed to Sign In", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}