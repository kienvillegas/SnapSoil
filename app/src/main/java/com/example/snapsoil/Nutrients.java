package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class Nutrients {
    private double nitrogen = 0;
    private  double phosphorus = 0;
    private  double potassium = 0;
    private  double pH = 0;
    private LocalDateTime createdAt;


    public Nutrients(double nitrogen, double phosphorus, double potassium, double pH, String createdAt) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.createdAt = LocalDateTime.parse(createdAt, formatter);
            this.nitrogen = nitrogen;
            this.phosphorus = phosphorus;
            this.potassium = potassium;
            this.pH = pH;
        }
    }

    public double getNitrogen() {
        return nitrogen;
    }

    public double getPhosphorus() {
        return phosphorus;
    }

    public double getPotassium() {
        return potassium;
    }

    public double getpH() {
        return pH;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
