package com.example.snapsoil;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoSoilNutrients extends AppCompatActivity {

    ImageView imgBtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_soil_nutrients);

        imgBtnBack = findViewById(R.id.back);
        imgBtnBack.setOnClickListener(v -> finish());
    }
}