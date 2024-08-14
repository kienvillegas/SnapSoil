package com.example.snapsoil;

public interface PredictionRequestListenter {
    void onRequestCompleted(HistoryData data);
    void onRequestFailed(String errorMessage);
}
