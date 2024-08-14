package com.example.snapsoil;

public interface DetectionRequestListener {
    void onRequestCompleted(String code);
    void onRequestFailed(String errorMessage);
}
