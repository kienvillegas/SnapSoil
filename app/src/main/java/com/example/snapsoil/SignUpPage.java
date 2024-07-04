package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpPage extends AppCompatActivity {
    private final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_-])(?=\\S+$).{8,20}$";
    private static FirebaseAuthHelper firebaseAuthHelper;
    private static FirebaseFirestoreHelper firebaseFirestoreHelper;
    private static User user;
    private static String userId;
    ImageButton imgBtnCalendar;
    EditText etFname, etMName, etLName, etBDate,etBarangay, etCity, etProvince, etEmail, etPassword, etCPassword;
    Button btnSignUp;
    TextView tvSignIn;
    ProgressBar pbSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
        etFname = findViewById(R.id.etFName);
        etMName = findViewById(R.id.etMName);
        etLName = findViewById(R.id.etLName);
        etBDate = findViewById(R.id.etBdate);
        etBDate.setEnabled(false);
        etBarangay = findViewById(R.id.etBarangay);
        etCity = findViewById(R.id.etCity);
        etProvince = findViewById(R.id.etProvince);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etCPassword = findViewById(R.id.etCPassword);
        pbSignUp = findViewById(R.id.pbSignUp);
        imgBtnCalendar = findViewById(R.id.imgBtnCalendar);

        firebaseAuthHelper = new FirebaseAuthHelper();
        firebaseFirestoreHelper = new FirebaseFirestoreHelper();

        btnSignUp.setOnClickListener(v -> {
            showProgressBar();

            String fName, mName, lName, brgy, city, province, email, password, confirmPassword, bDate;
            fName = etFname.getText().toString().trim();
            mName = etMName.getText().toString().trim();
            lName = etLName.getText().toString().trim();
            bDate = etBDate.getText().toString().trim();
            brgy = etBarangay.getText().toString().trim();
            city = etCity.getText().toString().trim();
            province = etProvince.getText().toString().trim();
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            confirmPassword = etCPassword.getText().toString().trim();
            Timestamp createdAt = new Timestamp(new Date());

            if(isEmpty(fName)){
                setErrorMsg(etFname,"Required");
            }
            if(isEmpty(mName)){
                setErrorMsg(etMName,"Required");
            }
            if(isEmpty(lName)){
                setErrorMsg(etLName,"Required");
            }
            if(isEmpty(brgy)){
                setErrorMsg(etBarangay,"Required");
            }

            if(isEmpty(city)){
                setErrorMsg(etCity,"Required");
            }
            if(isEmpty(province)){
                setErrorMsg(etProvince,"Required");
            }

            if(isEmpty(email)){
                setErrorMsg(etEmail,"Required");
            } else if(!isEmailValid(email)){
                setErrorMsg(etEmail,"Invalid Email Format");
            }

            if(isEmpty(password)){
                setErrorMsg(etPassword,"Required");
            }else if(!isPassLenValid(password)){
                setErrorMsg(etPassword,"Password must be 8-20 characters long");
            }else if(!isPassValid(password)){
                setErrorMsg(etPassword,"Must have atleast one of: \n" +
                        "* lowercase letter (a-z)\n" +
                        "* uppercase letter (A-Z)\n" +
                        "* number (0-9)\n" +
                        "* special character (@#$%^&+=-_)");
            }

            if(isEmpty(confirmPassword)){
                setErrorMsg(etCPassword,"Required");
            }else if(!isCPassMatch(password,confirmPassword)){
                setErrorMsg(etCPassword,"Password does not match");
            } else{
                user = new User(fName, mName, lName, brgy, city, province, bDate, createdAt);
                createAccount(user, email,confirmPassword);
            }
            hideProggressBar();
        });

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignInPage.class);
            startActivity(intent);
            finish();
        });

        imgBtnCalendar.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            displayDatePickerDialog(year, month, day);
        });
    }

    private boolean isEmpty(String str){
        if(str.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    private boolean isEmailValid(String email){
        if(email.endsWith("@gmail.com") || email.endsWith("@lspu.edu.ph")){
            return true;
        }else{
            return false;
        }
    }

    private boolean isPassLenValid(String password){
        if((password.length() >= 8)){
            return true;
        }else{
            return false;
        }
    }

    private boolean isPassValid(String password){
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean isCPassMatch(String password,String confirmPassword){
        if(password.equals(confirmPassword)){
            return true;
        }else{
            return false;
        }
    }

    private void setErrorMsg(EditText editText, String msg){
        editText.setError(msg);
        editText.requestFocus();
    }


    private void showProgressBar(){
        pbSignUp.setVisibility(View.VISIBLE);
        btnSignUp.setVisibility(View.GONE);
    }

    private void hideProggressBar(){
        pbSignUp.setVisibility(View.GONE);
        btnSignUp.setVisibility(View.VISIBLE);
    }


    private void displayDatePickerDialog(int year, int month, int day){
        DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpPage.this ,
                (view, year1, month1, dayOfMonth) ->
                        etBDate.setText(month1 + "-" + dayOfMonth + "-" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    private void createAccount(User user, String email, String confirmPassword){
        showProgressBar();
        firebaseAuthHelper.createAccount(email, confirmPassword, task -> {
            if(task.isSuccessful()){
                hideProggressBar();
                user.setUserId(task.getResult().getUser().getUid());
                addUserToFirestore(user);
            }else{
                hideProggressBar();
                Log.e(TAG, task.getException().toString());
                try{
                    throw task.getException();
                }catch(Exception e){
                    if(e instanceof FirebaseAuthException){
                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) e;
                        String errorCode = firebaseAuthException.getErrorCode();

                        switch (errorCode){
                            case "ERROR_USER_NOT_FOUND":
                                setErrorMsg(etEmail, "User not found");
                                break;
                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                setErrorMsg(etEmail, "Email already in use");
                            default:
                                Log.e(TAG, "Error Code: " + errorCode);
                        }
                    }
                }
            }
        });
    }

    private void addUserToFirestore(User user){
        firebaseFirestoreHelper.addUsers(user, task -> {
            if(task.isSuccessful()){
                Intent intent = new Intent(getApplicationContext(), SignInPage.class);
                startActivity(intent);
                finish();
            }else{
                Log.e(TAG, task.getException().toString());
                return;
            }
            pbSignUp.setVisibility(View.GONE);
            btnSignUp.setVisibility(View.VISIBLE);
        });
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