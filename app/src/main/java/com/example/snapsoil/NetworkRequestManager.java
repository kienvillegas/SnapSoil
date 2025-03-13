package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class NetworkRequestManager {
    private static final String URL = "http://192.168.51.248:5000";

    private OkHttpClient client;
    private Handler handler;
    private HistoryData historyData;

    public NetworkRequestManager(){
        client = new OkHttpClient();
        handler = new Handler(Looper.getMainLooper());
    }

    public void requestPrediction(Context context, Uri imageUri, PredictionRequestListenter listener) {
        String route = "predict";
        try {
            byte[] imageData = processImage(context, imageUri);
            Request request = buildRequest(imageData, route);
            executePredictRequest(request, listener);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onRequestFailed(e.getMessage());
        }
    }

    public void requestDetection(Context context, Uri imageUri, DetectionRequestListener listener){
        String route = "soil-detection";
        try{
            byte[] imageData = processImage(context, imageUri);
            Request request = buildRequest(imageData, route);
            executeDetectionRequest(request, listener);
        }catch (IOException e){
            e.printStackTrace();
            listener.onRequestFailed(e.getMessage());
        }
    }

    private byte[] processImage(Context context, Uri imageUri) throws IOException {
        InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        Bitmap resizedImage = Bitmap.createScaledBitmap(selectedImage, 224, 224, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }


    private Request buildRequest(byte[] imageData, String request) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData))
                .build();

        return new Request.Builder()
                .url(URL + "/" + request)
                .post(requestBody)
                .build();
    }

    private void executePredictRequest(Request request, PredictionRequestListenter listener) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    listener.onRequestFailed(e.getMessage());});
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    handler.post(() -> {
                        handlePredictionResponse(responseData, listener);
                    });
                } else {
                    Log.d(TAG, "Unsuccessful");
                    handler.post(() -> {
                        listener.onRequestFailed("Unsuccessful: " + response.code());
                    });
                }
            }
        });
    }

    private void executeDetectionRequest(Request request, DetectionRequestListener listener) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    listener.onRequestFailed(e.getMessage());});
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    handler.post(() -> {
                        handleDetectionResponse(responseData, listener);
                    });
                } else {
                    Log.d(TAG, "Unsuccessful");
                    handler.post(() -> {
                        listener.onRequestFailed("Unsuccessful: " + response.code());
                    });
                }
            }
        });
    }

    private void handleDetectionResponse(String responseData, DetectionRequestListener listener){
        try{
            JSONObject json = new JSONObject(responseData);
            String code = json.getString("errorCode");
            if(code.equals("201") || code.equals("202")){
                handler.post(() -> {listener.onRequestCompleted(code);});
            }else{
                handler.post(() -> {listener.onRequestFailed("Failed to detect soil");});
                Log.d(TAG, "handleDetectionResponse: " + code);
            }
        }catch(Exception e){
            Log.e(TAG, "handleResponse: " + e.getMessage());
        }
    }

    private void handlePredictionResponse(String responseData, PredictionRequestListenter listener){
        try{
            JSONObject json = new JSONObject(responseData);
            String code = json.getString("errorCode");
            if(code.equals("203")){
                double nPrediction, pPrediction, kPrediction, pHPrediction;
                String crop;

                nPrediction = json.getDouble("n");
                pPrediction = json.getDouble("p");
                kPrediction = json.getDouble("k");
                pHPrediction = json.getDouble("pH");
                crop = json.getString("crop");

                historyData = new HistoryData(nPrediction, pPrediction, kPrediction, pHPrediction, crop);
                handler.post(() -> {
                   listener.onRequestCompleted(historyData);
                });
            }else{
                handler.post(() -> {
                   listener.onRequestFailed("Failed to predict soil nutrients");
                });
            }
        }catch(Exception e){
            Log.e(TAG, "handleResponse: " + e.getMessage());
        }
    }
}
