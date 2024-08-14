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
    private DocumentReference userRef;

    public FirebaseFirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUsers(User user,OnCompleteListener<Void> listener){
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", user.getUserId());
        userData.put("fullName", user.getfName() + " " + user.getmName().charAt(0) + ", " + user.getlName());
        userData.put("firstName", user.getfName());
        userData.put("middleName", user.getmName());
        userData.put("lastName", user.getlName());
        userData.put("address" , "Brgy. " + user.getBrgyName() + " " + user.getCityName() + ", " + user.getProvName());
        userData.put("brgyName", user.getBrgyName());
        userData.put("cityName", user.getCityName());
        userData.put("provName", user.getProvName());
        userData.put("birthday", user.getBDay());
        userData.put("createdAt", user.getCreateAt());

        db.collection("users")
                .document(user.getUserId())
                .set(userData)
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "addUser: " + e.getMessage());
                });
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

    public void getHistory(String id, boolean direction, OnCompleteListener<QuerySnapshot> listener){
        if(direction == true){
            Log.d(TAG, "getHistory: Ascending");
            DocumentReference historyRef = db.collection("history").document(id);
            historyRef.collection("userHistory")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(listener)
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "getHistory: " + e.getMessage());
                    });
        }else{
            Log.d(TAG, "getHistory: Descending");
            DocumentReference historyRef = db.collection("history").document(id);
            historyRef.collection("userHistory")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(listener)
                    .addOnFailureListener(e -> {
                        Log.d(TAG, "getHistory: " + e.getMessage());
                    });
        }
    }

    public void updateName(User user, OnCompleteListener<Void> listener){
        db.collection("users").document(user.getUserId())
                .update(
                        "firstName", user.getfName(),
                        "middleName", user.getmName(),
                        "lastName", user.getlName(),
                        "fullName", user.getfName() + " " + user.getmName() + ", " + user.getlName()
                ).addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "updateName: " + e.getMessage());
                });
    }
    public void updateAddress(User user, OnCompleteListener<Void> listener){
        db.collection("users").document(user.getUserId())
                .update(
                        "brgyName", user.getBrgyName(),
                        "cityName", user.getCityName(),
                        "provName", user.getProvName(),
                        "address", "Brgy. " + user.getBrgyName() + " " + user.getCityName() + ", " + user.getProvName()
                ).addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "updateAddress: " + e.getMessage());
                });
    }

    public void getUserDocument(User user, OnCompleteListener<DocumentSnapshot> listener){
        db.collection("users").document(user.getUserId())
                .get()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "getFieldValue: " + e.getMessage());
                });
    }

    public void deleteUserFirestore(User user, OnCompleteListener<Void> listener){
        db.collection("users").document(user.getUserId())
                .delete()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "deleteUserFirestore: " + e.getMessage());
                });
    }
}
