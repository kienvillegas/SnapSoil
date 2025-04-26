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
import android.widget.ImageView;
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
    TextView tvN, tvP, tvK, tvpH, tvCrop;
    ImageView imCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);
        btnDone = findViewById(R.id.btnDone);
        tvN = findViewById(R.id.tvN);
        tvP = findViewById(R.id.tvP);
        tvK = findViewById(R.id.tvK);
        tvpH = findViewById(R.id.tvpH);
        tvCrop = findViewById(R.id.tvCrop);
        imCrop = findViewById(R.id.imCrop);

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
                getIntent().hasExtra("pH_pred") &&
                getIntent().hasExtra("crop")
        ) {
            double n = getIntent().getDoubleExtra("n_pred", 0);
            double p = getIntent().getDoubleExtra("p_pred", 0);
            double k = getIntent().getDoubleExtra("k_pred", 0);
            double pH = getIntent().getDoubleExtra("pH_pred", 0);
            String crop = getIntent().getStringExtra("crop");
            int cropImg;



            switch (crop){
                case "rice":
                    cropImg = R.drawable.rice;
                    break;
                case "maize":
                    cropImg = R.drawable.corn;
                    break;

                case "chickpea":
                    cropImg = R.drawable.chick_pea;
                    break;

                case "kidneybeans":
                    cropImg = R.drawable.kidney_beans;
                    break;

                case "pigeonpeas":
                    cropImg = R.drawable.pigeon_peas;
                    break;

                case "mothbeans":
                    cropImg = R.drawable.moth_beans;
                    break;

                case "mungbean":
                    cropImg = R.drawable.mung_beans;
                    break;

                case "blackgram":
                    cropImg = R.drawable.blackgram;
                    break;

                case "lentil":
                    cropImg = R.drawable.lentils;
                    break;

                case "pomegranate":
                    cropImg = R.drawable.pomegranate;
                    break;

                case "banana":
                    cropImg = R.drawable.banana;
                    break;

                case "mango":
                    cropImg = R.drawable.mango;
                    break;

                case "grape":
                    cropImg = R.drawable.grapes;
                    break;

                case "watermelon":
                    cropImg = R.drawable.watermelon;
                    break;

                case "muskmelon":
                    cropImg = R.drawable.muskmelon;
                    break;

                case "apple":
                    cropImg = R.drawable.apple;
                    break;

                case "orange":
                    cropImg = R.drawable.orange;
                    break;

                case "papaya":
                    cropImg = R.drawable.papaya;
                    break;

                case "coconut":
                    cropImg = R.drawable.coconut;
                    break;

                case "cotton":
                    cropImg = R.drawable.cotton;
                    break;

                case "jute":
                    cropImg = R.drawable.jute;
                    break;

                case "coffee":
                    cropImg = R.drawable.coffee;
                    break;

                default:
                    cropImg = R.drawable.orange;
                    Log.d(TAG, "displayValues: " + cropImg);
            }

            String nMeaning = "";
            String pMeaning = "";
            String kMeaning = "";
            String pHMeaning = "";

            nMeaning = evalNitrogen(n);
            pMeaning = evalPhosphorus(p);
            kMeaning = evalPotassium(k);
            pHMeaning = evalpH(pH);

            tvN.setText(df.format(n) + " (" + nMeaning + ")");
            tvP.setText(df.format(p) + " (" + pMeaning + ")");
            tvK.setText(df.format(k) + " (" + kMeaning + ")");
            tvpH.setText(df.format(pH) + " (" + pHMeaning + ")");

            tvCrop.setText(crop);
            imCrop.setImageResource(cropImg);
        } else {
            Toast.makeText(this, "Values are not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public static String evalNitrogen(Double n){
        if(n <= 30){
            return "Low";
        }else if(n > 30 && n <= 150){
            return "Medium";
        }else if(n > 150 && n <= 500) {
            return "High";
        } else return "";
    }

    public static String evalPhosphorus(Double p){
        if(p <= 40){
            return "Low";
        }else if(p > 40 && p <= 100){
            return "Medium";
        }else if(p > 100 && p <= 240){
            return "High";
        }else return "";
    }
    public static String evalPotassium(Double k){
        if(k <= 40){
            return "Low";
        }else if(k > 40 && k <= 100){
            return "Medium";
        }else if(k > 100 && k <= 240){
            return "High";
        }else return "";
    }
    public static String evalpH(Double pH){
        if(pH >= 4.0 && pH <= 5.5){
            return "Low";
        }else if(pH > 5.5 && pH <= 7.5){
            return "Medium";
        }else if(pH > 7.5 && pH <= 10.0){
            return "High";
        }else return "";
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