package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.util.Log;

public class Nutrients {
    private double totalNitrogen = 0;
    private double totalPhosphorus = 0;
    private double totalPotassium = 0;
    private double totalpH = 0;
    private int count = 0;

    void addData(double nitrogen, double phosphorus, double potassium, double pH){
        this.totalNitrogen += nitrogen;
        this.totalPhosphorus += phosphorus;
        this.totalPotassium += potassium;
        this.totalpH += pH;
        this.count++;
    }

    double getAverageNitrogen() {
        return count > 0 ? totalNitrogen / count : 0;
    }

    double getAveragePhosphorus() {
        return count > 0 ? totalPhosphorus / count : 0;
    }

    double getAveragePotassium() {
        return count > 0 ? totalPotassium / count : 0;
    }

    double getAveragepH() {
        return count > 0 ? totalpH / count : 0;
    }
}
