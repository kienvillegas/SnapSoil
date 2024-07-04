package com.example.snapsoil;

import static android.content.ContentValues.TAG;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsPage extends AppCompatActivity {
    private final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=_-])(?=\\S+$).{8,20}$";

    private static FirebaseAuthHelper firebaseAuthHelper;
    private static FirebaseFirestoreHelper firebaseFirestoreHelper;
    private static User user;
    private Dialog dialog;
    ImageButton imBtnBack, imBtnChangePass, imgBtnDeleteAcc;
    EditText etCurrentPassword, etEditPass;
    Button btnSubmit, btnDelAcc;
    Switch sNotif, sDarkMode;

    private boolean isChangePass = true;
    private boolean isDeleteAcc = true;
    private ProgressBar pbSave, pbDel;
    private Button btnSaveCancel, btnSaveConfirm, btnDelConfirm, btnDelCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changeTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        firebaseAuthHelper = new FirebaseAuthHelper();
        firebaseFirestoreHelper = new FirebaseFirestoreHelper();
        user = new User();

        imBtnBack = findViewById(R.id.imBtnBack);
        imBtnChangePass = findViewById(R.id.imBtnChangePass);
        btnSubmit = findViewById(R.id.btnSubmit);
        sNotif = findViewById(R.id.sNotif);
        sDarkMode = findViewById(R.id.sDarkMode);
        imgBtnDeleteAcc = findViewById(R.id.imgBtnDeleteAcc);
        btnDelAcc = findViewById(R.id.btnDelAcc);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etEditPass = findViewById(R.id.etEditPass);
        firebaseAuthHelper = new FirebaseAuthHelper();

        sNotif.setChecked(isNotificationEnabled());
        sNotif.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleNotifications(isChecked);
        });

        sDarkMode.setChecked(isNightModeEnabled());
        sDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNightMode(isChecked));
        imBtnBack.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        imBtnChangePass.setOnClickListener(v -> {
            if(isChangePass){
                imBtnChangePass.setImageResource(R.drawable.arrow_down);
                showEditChangePass();
            }else{
                imBtnChangePass.setImageResource(R.drawable.arrow_right);
                hideEditChangePass();
            }
            isChangePass = !isChangePass;
        });

        imgBtnDeleteAcc.setOnClickListener(v -> {
            if(isDeleteAcc){
                imgBtnDeleteAcc.setImageResource(R.drawable.arrow_down);
                showEditDelAcc();
            }else{
                imgBtnDeleteAcc.setImageResource(R.drawable.arrow_right);
                hideEditDelAcc();
            }
            isDeleteAcc = !isDeleteAcc;
        });

      btnDelAcc.setOnClickListener(v -> {
          showDialogProgressBar(pbDel,btnDelConfirm, btnDelCancel);
          btnDelConfirm.setOnClickListener(v1 -> {
                delAccFirestore(user);
                hideDialogProgressBar(pbDel,btnDelConfirm, btnDelCancel);
                dialog.dismiss();
          });
          dialog.show();
      });

      btnSubmit.setOnClickListener(v -> {
          initializeSaveChagesDialog();
          String password, newPassword;
          password = etCurrentPassword.getText().toString().trim();
          newPassword = etEditPass.getText().toString().trim();

          if(isEmpty(password)){
              setErrorMsg(etEditPass, "Required");
          }

          if(isEmpty(newPassword)){
              setErrorMsg(etEditPass, "Required");
          }else if(!isLenValid(newPassword)){
              setErrorMsg(etEditPass, "Password must be 8-20 characters long");
          }else if(!isPassValid(newPassword)){
              setErrorMsg(etEditPass,"Must have atleast one of: \n" +
                      "* lowercase letter (a-z)\n" +
                      "* uppercase letter (A-Z)\n" +
                      "* number (0-9)\n" +
                      "* special character (@#$%^&+=-_)");
          }else{
              showDialogProgressBar(pbSave, btnSaveConfirm, btnSaveCancel);
              changePassword(password,newPassword);
              hideDialogProgressBar(pbSave, btnSaveConfirm, btnSaveCancel);
          }
      });
    }

    private void showDialogProgressBar(ProgressBar pb, Button confirm, Button cancel){
        pb.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
    }

    private void hideDialogProgressBar(ProgressBar pb, Button confirm, Button cancel){
        pb.setVisibility(View.GONE);
        confirm.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);    }

    private boolean isEmpty(String str){
        return str.isEmpty() ? true : false;
    }

    private void setErrorMsg(EditText editText, String msg){
        editText.setError(msg);
        editText.requestFocus();
    }

    private boolean isLenValid(String password){
        if((password.length() >= 8) && (password.length() <= 20)){
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

    private void changePassword(String password, String newPassword){
        firebaseAuthHelper.changePassword(password, newPassword, task -> {
            if(task.isSuccessful()){
                Log.e(TAG, "changePasssword: Successfully changed your password");
                hideEditChangePass();
                etEditPass.setText(null);
                etCurrentPassword.setText(null);
            }else{
                Log.e(TAG, "changePasssword: " + task.getException().toString());
            }
        });
    };
    public void delAccFirestore(User user){
        String userId = firebaseAuthHelper.getUserId();
        FirebaseUser firebaseUser = firebaseAuthHelper.getCurrentUser();

        if(userId != null){
            user.setUserId(userId);
            firebaseFirestoreHelper.deleteUserFirestore(user, task -> {
                if(task.isSuccessful()){
                    Log.d(TAG, "delAccFirestore: Successfully deleted account on firestore");
                    delAccAuth(firebaseUser);
                }else{
                    Log.d(TAG, "delAccFirestore: " + task.getException().toString());
                }
            });
        }else{
            Log.d(TAG, "delAccFirestore: userId is null");
        }
    }

    public void delAccAuth(FirebaseUser firebaseUser){
        firebaseAuthHelper.deleteUserAuth(firebaseUser, task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "delAccAuth: Successfully deleted account authentication");
                Intent intent = new Intent(getApplicationContext(), SignInPage.class);
                startActivity(intent);
                finish();
            }else{
                Log.e(TAG, "delAccAuth: " + task.getException().toString());
            }
        });
    }

    public void initializeSaveChagesDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.save_changes_confirmation);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        dialog.setCancelable(false);

        btnSaveCancel = dialog.findViewById(R.id.btnSaveChangesCancel);
        btnSaveCancel.setOnClickListener(v -> dialog.dismiss());
        pbSave = dialog.findViewById(R.id.pbSaveChanges);
    }

    public void initializeDelDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.delete_confirmation);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        dialog.setCancelable(false);

        btnDelCancel = dialog.findViewById(R.id.btnDelAccCancel);
        btnDelCancel.setOnClickListener(v -> dialog.dismiss());
        pbDel = dialog.findViewById(R.id.pbDelAcc);
    }

    public void showEditChangePass(){
        int[] textViewIds = {R.id.textView52, R.id.textView53};
        int[] editViewIds = {R.id.etEditPass, R.id.etCurrentPassword};

        for(int id : textViewIds){
            View view = findViewById(id);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setVisibility(View.VISIBLE);
            }
        }

        for(int id : editViewIds){
            View view = findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setVisibility(View.VISIBLE);
            }
        }
        btnSubmit.setVisibility(View.VISIBLE);
    }
    public void hideEditChangePass(){
        int[] textViewIds = {R.id.textView52, R.id.textView53};
        int[] editViewIds = {R.id.etEditPass, R.id.etCurrentPassword};
        for(int id : textViewIds){
            View view = findViewById(id);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setVisibility(View.GONE);
            }
        }

        for(int id : editViewIds){
            View view = findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setVisibility(View.GONE);
            }
        }
        btnSubmit.setVisibility(View.GONE);
    }

    private void showEditDelAcc(){
        btnDelAcc.setVisibility(View.VISIBLE);
    }

    private void hideEditDelAcc(){
        btnDelAcc.setVisibility(View.GONE);
    }
    private boolean isNightModeEnabled() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("NightMode", false);
    }

    private void toggleNightMode(boolean enable) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("NightMode", enable);
        editor.apply();

        recreate();
    }

    private boolean isNotificationEnabled() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("NotificationEnabled", true);
    }

    private void toggleNotifications(boolean enable) {
        SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("NotificationEnabled", enable);
        editor.apply();
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