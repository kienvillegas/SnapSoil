package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class SignInPage extends AppCompatActivity {
    private static FirebaseAuthHelper firebaseAuthHelper;
    Button btnSignIn;
    TextView tvSignUp, tvForgotPassword;
    EditText etEmail, etPassword;
    ProgressBar pbSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_page);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp = findViewById(R.id.tvSignUp);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        pbSignIn = findViewById(R.id.pbSignIn);
        tvForgotPassword= findViewById(R.id.tvForgotPassword);

        firebaseAuthHelper = new FirebaseAuthHelper();

        btnSignIn.setOnClickListener(v -> {
            showProgressBar();

            String email, password;
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();

            if(isEmailValid(email)){
                setErrorMsg(etEmail, "Required");
            }else if(!isEmailValid(email)){
                setErrorMsg(etEmail, "Invalid Email Format");
            }

            if(isEmpty(password)){
                hideProggressBar();
                setErrorMsg(etPassword, "Required");
            }else{
                signIn(email, password);
            }
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUpPage.class);
            startActivity(intent);
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            showProgressBar();
            String email = etEmail.getText().toString().trim();

            if(isEmpty(email)){
                hideProggressBar();
                setErrorMsg(etEmail, "Required");
            }else if(!isEmailValid(email)){
                hideProggressBar();
                setErrorMsg(etEmail, "Invalid Email Format");
            }else{
                passwordReset(email);
            }
        });
    }

    private boolean isEmpty(String str){
        return str.isEmpty() ? true : false;
    }

    private void setErrorMsg(EditText editText, String msg){
        editText.setError(msg);
        editText.requestFocus();
    }
    private void signIn(String email, String password){
        showProgressBar();
        etEmail.setError(null);
        etPassword.setError(null);

        firebaseAuthHelper.signIn(email, password, task -> {
            if(task.isSuccessful()){
                hideProggressBar();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }else{
                hideProggressBar();
                Log.e(TAG, "signIn(Page): " + task.getException().getMessage());
                try{
                    throw task.getException();
                }catch(Exception e){
                    if(e instanceof FirebaseAuthException){
                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) e;
                        String errorCode = firebaseAuthException.getErrorCode();

                        switch (errorCode){
                            case "ERROR_INVALID_CREDENTIAL":
                                etEmail.setError("Incorrect Email");
                                etEmail.requestFocus();
                                etPassword.setError("Incorrect Password");
                                etPassword.requestFocus();
                                break;
                            default:
                                Log.e(TAG, "Error Code: " + errorCode);
                        }
                    }
                }
            }
        });
    }
    private void passwordReset(String email){
        showProgressBar();
        firebaseAuthHelper.passwordReset(email, task -> {
            if(task.isSuccessful()){
                hideProggressBar();
                Toast.makeText(this, "Successfully sent password reset link", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Successfully sent password reset link");
            }else{
                hideProggressBar();
                Log.d(TAG, task.getException().toString());
                try{
                    throw task.getException();
                }catch(Exception e){
                    if(e instanceof FirebaseAuthException){
                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) e;
                        String errorCode = firebaseAuthException.getErrorCode();

                        switch (errorCode){
                            case "ERROR_USER_NOT_FOUND":
                                etEmail.setError("User Not Found");
                                etEmail.requestFocus();
                                break;
                            default:
                                Log.e(TAG, "Error Code: " + errorCode);
                        }
                    }
                }
            }
        });
    }
    private boolean isEmailValid(String email){
        if(email.endsWith("@gmail.com") || email.endsWith("@lspu.edu.ph")){
            return true;
        }else{
            return false;
        }
    }

    private void showProgressBar(){
        pbSignIn.setVisibility(View.VISIBLE);
        btnSignIn.setVisibility(View.GONE);
    }

    private void hideProggressBar(){
        pbSignIn.setVisibility(View.GONE);
        btnSignIn.setVisibility(View.VISIBLE);
    }

    private boolean isNightModeEnabled() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("NightMode", false);
    }
    private void changeTheme(){
        if (isNightModeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuthHelper.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}