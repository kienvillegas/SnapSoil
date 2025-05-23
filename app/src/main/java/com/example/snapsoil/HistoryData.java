package com.example.snapsoil;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryData {
    private String id;
    private Double nitrogen, phosphorus, potassium, pH;
    private String crop;
    private String createAt;

    public HistoryData(Double nitrogen, Double phosphorus, Double potassium, Double pH, String crop, String createAt) {
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.pH = pH;
        this.crop = crop;
        this.createAt = createAt;
    }

    public HistoryData(Double nitrogen, Double phosphorus, Double potassium, Double pH, String crop) {
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.pH = pH;
        this.crop = crop;
        this.createAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public String getId() {
        return id;
    }

    public Double getNitrogen() {
        return nitrogen;
    }


    public Double getPhosphorus() {
        return phosphorus;
    }


    public Double getPotassium() {
        return potassium;
    }


    public Double getpH() {
        return pH;
    }

    public String getCrop(){ return crop;}

    public String getCreatedAt(){
        return createAt;
    }
}
