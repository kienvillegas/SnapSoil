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
    private FirebaseAuth.AuthStateListener authStateListener;

    public FirebaseAuthHelper(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void createAccount(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "accountCreation: " + e.getMessage()));
    }
    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "signIn: " + e.getMessage()));
    }

    public void signInAnonymously(OnCompleteListener<AuthResult> listener){
        mAuth.signInAnonymously().addOnCompleteListener(listener);
    }
    public void passwordReset(String email, OnCompleteListener<Void> listener){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "passwordReset: " + e.getMessage()));
    }

    public void changeEmail(String email,String password, OnCompleteListener<Void> listener){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
            currentUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            currentUser.verifyBeforeUpdateEmail(email)
                                    .addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()){
                                            authStateListener = firebaseAuth -> {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if(user!= null && user.isEmailVerified()){
                                                    Log.d(TAG, "changeEmail: Email is successfully verified");
                                                    mAuth.removeAuthStateListener(authStateListener);
                                                }else{
                                                    Log.e(TAG, "changeEmail: email is null or not verified");
                                                }
                                            };
                                            mAuth.addAuthStateListener(authStateListener);
                                            listener.onComplete(Tasks.forResult(null));
                                        }else{
                                            listener.onComplete(Tasks.forException(task1.getException()));
                                        }
                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, "changeEmail: " + e.getMessage());
                                    });
                        }else{
                            listener.onComplete(Tasks.forException(task.getException()));
                        }
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "changeEmail: " + e.getMessage());
                    });
        }
    }

    public void changePassword(String oldPass, String newPass, OnCompleteListener<Void> listener){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPass);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        currentUser.updatePassword(newPass);
                    }else{
                        listener.onComplete(task);
                    }
                }).addOnFailureListener(e -> {
                    Log.d(TAG, "changePassword: " + e.getMessage());
                });
    }

    public void deleteUserAuth(FirebaseUser user, OnCompleteListener<Void> listener){
        user.delete()
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> {
                    Log.d(TAG, "deleteUserAuth: " + e.getMessage());
                });
    }

    public String getEmail(){
        String email =  mAuth.getCurrentUser().getEmail();
        if(email != null){
            return email;
        }else{
            Log.e(TAG, "getEmail: email is null");
            return null;
        }
    }
    public void signOut(){
        mAuth.signOut();
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
