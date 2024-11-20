package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseFirestoreHelper {
    private FirebaseFirestore db;

    public FirebaseFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }


    public void addHistory(HistoryData history, String id, OnCompleteListener<DocumentReference> listener){
        Map<String, Object> historyData = new HashMap<>();
        historyData.put("nitrogen", history.getNitrogen());
        historyData.put("phosphorus", history.getPhosphorus());
        historyData.put("potassium", history.getPotassium());
        historyData.put("pH", history.getpH());
        historyData.put("createdAt", history.getCreatedAt());

        DocumentReference historyRef = db.collection("history")
                .document(id);
        historyRef.collection("userHistory")
                .add(historyData)
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "historyRef: " + e.getMessage());
                });
    }

    public void getHistory(String id, OnCompleteListener<QuerySnapshot> listener){
        DocumentReference historyRef = db.collection("history").document(id);
        historyRef.collection("userHistory")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "getHistory: " + e.getMessage());
                });
    }

    public void filterHistory(String id, String startDate, String endDate, OnCompleteListener<QuerySnapshot> listener){
        DocumentReference historyRef = db.collection("history").document(id);
        historyRef.collection("userHistory")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .get()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "filterHistory: " + e.getMessage());
                });
    }
}
