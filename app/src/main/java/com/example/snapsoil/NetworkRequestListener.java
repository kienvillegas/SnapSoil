package com.example.snapsoil;

import org.json.JSONObject;

public interface NetworkRequestListener {
    void onSucess(JSONObject json);
    void onFailure(String errorMessage);
}
