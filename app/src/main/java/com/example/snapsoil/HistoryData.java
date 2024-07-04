package com.example.snapsoil;

import com.google.firebase.Timestamp;

public class HistoryData {
    private String id;
    private Double nitrogen, phosphorus, potassium, pH;
    private Timestamp createAt;

    public HistoryData(String id, Double nitrogen, Double phosphorus, Double potassium, Double pH, Timestamp createAt) {
        this.id = id;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.pH = pH;
        this.createAt = createAt;
    }

    public String getId() {
        return id;
    }

    public Double getNitrogen() {
        return nitrogen;
    }

    public void setNitrogen(Double nitrogen) {
        this.nitrogen = nitrogen;
    }

    public Double getPhosphorus() {
        return phosphorus;
    }

    public void setPhosphorus(Double phosphorus) {
        this.phosphorus = phosphorus;
    }

    public Double getPotassium() {
        return potassium;
    }

    public void setPotassium(Double potassium) {
        this.potassium = potassium;
    }

    public Double getpH() {
        return pH;
    }

    public void setpH(Double pH) {
        this.pH = pH;
    }
}
