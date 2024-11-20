package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class FirebaseAuthHelper {
    private FirebaseAuth mAuth;

    public FirebaseAuthHelper(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void signInAnonymously(OnCompleteListener<AuthResult> listener){
        mAuth.signInAnonymously().addOnCompleteListener(listener);
    }

    public String getUserId(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            Log.e(TAG, "getUserId: no user is signed in");
            return null;
        }
    }

    public FirebaseUser getCurrentUser(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return user;
        } else {
            Log.e(TAG, "getuser: no user is signed in");
            return null;
        }
    }
}
