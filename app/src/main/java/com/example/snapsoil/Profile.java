package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

public class Profile extends Fragment {
    private static FirebaseAuthHelper firebaseAuthHelper;
    private static FirebaseFirestoreHelper firebaseFirestoreHelper;
    private static User user;
    TextView textView48, textView56;
    EditText etEditEmail, etEditFName, etEditMName, etEditLName;
    EditText etEditBrgy, etEditCity, etEditProv;
    EditText etCurrentName, etCurrentAddres, etCurrentEmail, etEditEmailPass;
    ImageButton imBtnSettings, imBtnEditName, imBtnEditAddress, imBtnEditEmail;
    Button btnSignOut;
    Dialog dialog;
    private Button btnSaveCancel, btnSaveConfirm, btnSignOutConfirm, btnSignOutCancel;
    private ProgressBar pbSave, pbSignOut;
    private boolean isEditName, isEditAddress, isEditEmail = true;
    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuthHelper = new FirebaseAuthHelper();
        firebaseFirestoreHelper = new FirebaseFirestoreHelper();
        user = new User();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        textView56 = view.findViewById(R.id.textView56);
        textView48 = view.findViewById(R.id.textView48);
        etEditEmail = view.findViewById(R.id.etEditEmail);
        imBtnEditName = view.findViewById(R.id.imBtnEditName);
        imBtnEditAddress = view.findViewById(R.id.imBtnEditAddress);
        imBtnEditEmail = view.findViewById(R.id.imBtnEditEmail);
        imBtnSettings = view.findViewById(R.id.imBtnSettings);
        btnSignOut = view.findViewById(R.id.btnSignOut);
        etEditFName = view.findViewById(R.id.etEditFName);
        etEditMName = view.findViewById(R.id.etEditMName);
        etEditLName = view.findViewById(R.id.etEditLName);
        etCurrentName = view.findViewById(R.id.etCurrentName);
        etCurrentAddres = view.findViewById(R.id.etCurrentAddress);
        etCurrentEmail = view.findViewById(R.id.etCurrentEmail);
        etEditEmailPass = view.findViewById(R.id.etEditEmaiilPass);
        etEditBrgy = view.findViewById(R.id.etEditBarangay);
        etEditCity = view.findViewById(R.id.etEditCity);
        etEditProv = view.findViewById(R.id.etEditProvince);

        displayName();
        displayAddress();
        displayEmail();
        disableCurrentInfo(view);
        initializeDialog();
        initializeSignOutDialog();

        btnSaveConfirm = dialog.findViewById(R.id.btnSaveChangesConfirm);
        btnSignOutConfirm = dialog.findViewById(R.id.btnSignOutConfirm);

        imBtnEditName.setOnClickListener(v -> {
            String fName, mName, lName;

            fName = etEditFName.getText().toString().trim();
            mName = etEditMName.getText().toString().trim();
            lName = etEditLName.getText().toString().trim();

            if(isEditName){
                if(!(isEmpty(fName) && isEmpty(mName) && isEmpty(lName))){
                    btnSaveConfirm.setOnClickListener(v1 -> {
                        showSaveProgressBar();
                        hideEditName(view);
                        updateName(fName,mName,lName);
                        hideSaveProgressBar();
                        dialog.dismiss();
                    });
                    dialog.show();
                }else{
                    hideEditName(view);
                }
            }else{
                showEditName(view);
            }
            imBtnEditName.setBackgroundResource(R.drawable.check);
            isEditName = !isEditName;
        });

        imBtnEditAddress.setOnClickListener(v -> {
            String brgyName, cityName, provName;

            brgyName = etEditBrgy.getText().toString().trim();
            cityName = etEditCity.getText().toString().trim();
            provName = etEditProv.getText().toString().trim();

            if(isEditAddress){
                if(!(isEmpty(brgyName) && isEmpty(cityName) && isEmpty(provName))) {
                    btnSaveConfirm.setOnClickListener(v1 -> {
                        showSaveProgressBar();
                        imBtnEditAddress.setBackgroundResource(R.drawable.edit);

                        updateAddress(brgyName, cityName, provName);
                        hideEditAddress(view);
                        displayAddress();
                        hideSaveProgressBar();
                        dialog.dismiss();
                    });
                    dialog.show();
                }else{
                    imBtnEditAddress.setBackgroundResource(R.drawable.edit);
                    hideEditAddress(view);
                }
            }else{
                imBtnEditAddress.setBackgroundResource(R.drawable.check);
                showEditAddress(view);
            }
            isEditAddress = !isEditAddress;
        });

        imBtnEditEmail.setOnClickListener(v -> {
            String email, password;
            email = etEditEmail.getText().toString().trim();
            password = etEditEmailPass.getText().toString().trim();

            if(isEditEmail){
                if(!(isEmpty(email) && isEmpty(password))){
                    if(isEmailValid(email)){
                        btnSaveConfirm.setOnClickListener(v12 -> {
                            showSaveProgressBar();
                            imBtnEditEmail.setBackgroundResource(R.drawable.edit);

                            updateEmail(email, password);
                            hideEditEmail();
                            displayEmail();
                            hideSaveProgressBar();
                            dialog.dismiss();
                        });
                        dialog.show();
                    }else{
                        setErrorMsg(etEditEmail, "Invalid email format");
                    }
                }else{
                    imBtnEditEmail.setBackgroundResource(R.drawable.edit);
                    hideEditEmail();
                }
            }else{
                imBtnEditEmail.setBackgroundResource(R.drawable.check);
                showEditEmail();
            }
            isEditEmail = !isEditEmail;
        });

        imBtnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsPage.class);
            startActivity(intent);
        });

        btnSignOut.setOnClickListener(v -> {
            dialog.show();
            btnSignOutConfirm.setOnClickListener(v13 -> {
                showSignOutProgressBar();
                firebaseAuthHelper.signOut();
                hideSignOutProgressBar();
                dialog.dismiss();

                Intent intent = new Intent(getContext(), SignInPage.class);
                startActivity(intent);
                getActivity().finish();
            });
        });
        return view;
    }

    private void showSaveProgressBar(){
        pbSave.setVisibility(View.VISIBLE);
        btnSaveConfirm.setVisibility(View.GONE);
        btnSaveCancel.setVisibility(View.GONE);
    }

    private void hideSaveProgressBar(){
        pbSave.setVisibility(View.GONE);
        btnSaveConfirm.setVisibility(View.VISIBLE);
        btnSaveCancel.setVisibility(View.VISIBLE);
    }

    private void showSignOutProgressBar(){
        pbSignOut.setVisibility(View.VISIBLE);
        btnSignOutConfirm.setVisibility(View.GONE);
        btnSignOutCancel.setVisibility(View.GONE);
    }

    private void hideSignOutProgressBar(){
        pbSignOut.setVisibility(View.GONE);
        btnSignOutCancel.setVisibility(View.VISIBLE);
        btnSignOutConfirm.setVisibility(View.VISIBLE);
    }
    private void initializeDialog(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.save_changes_confirmation);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        dialog.setCancelable(false);

        btnSaveCancel = dialog.findViewById(R.id.btnSaveChangesCancel);
        btnSaveCancel.setOnClickListener(v -> dialog.dismiss());
        pbSave = dialog.findViewById(R.id.pbSaveChanges);
    }

    private void initializeSignOutDialog(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.sign_out_confirmation);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        dialog.setCancelable(false);

        btnSignOutCancel = dialog.findViewById(R.id.btnSignOutCancel);
        btnSignOutCancel.setOnClickListener(v -> dialog.dismiss());
        pbSignOut = dialog.findViewById(R.id.pbSignOut);
    }
    private void setErrorMsg(EditText editText, String msg){
        editText.setError(msg);
        editText.requestFocus();
    }

    private void displayName(){
        String field = "fullName";
        user.setUserId(firebaseAuthHelper.getUserId());
        firebaseFirestoreHelper.getUserDocument(user, task -> {
            if(task.isSuccessful()){
                String name = task.getResult().getString(field);
                if(name != null){
                    etCurrentName.setText(name);
                }else{
                    Log.d(TAG, "displayName: " + name + " is null");
                }
            }else{
                Log.e(TAG, "displayName: " + task.getException().getMessage());
            }
        });
    }

    private void displayAddress(){
        String field = "address";
        user.setUserId(firebaseAuthHelper.getUserId());
        firebaseFirestoreHelper.getUserDocument(user, task -> {
            if(task.isSuccessful()){
                String address = task.getResult().getString(field);
                if(address != null){
                    etCurrentAddres.setText(address);
                }else{
                    Log.d(TAG, "displayAddress: " + address + " is null");
                }
            }else{
                Log.e(TAG, "displayAddress: " + task.getException(). getMessage());
            }
        });
    }

    private void displayEmail(){
        String email = firebaseAuthHelper.getEmail();
        etCurrentEmail.setText(email);
    }

    private void updateName(String fName, String mName, String lName){
        user = new User();
        user.setUserId(firebaseAuthHelper.getUserId());
        user.setfName(fName);
        user.setmName(mName);
        user.setlName(lName);

        firebaseFirestoreHelper.updateName(user, task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "updateName: Successfully updated name");
                displayName();
                setTextNull(etEditFName);
                setTextNull(etEditMName);
                setTextNull(etEditLName);
            }else{
                Log.d(TAG, "updateName: " + task.getException().toString());
            }
        });
    }

    private void setTextNull(EditText editText){
        editText.setText(null);
    }

    private boolean isEmpty(String str){
        if(str.isEmpty() || str.length() < 2){
            return true;
        }else{
            return false;
        }
    }

    private void updateAddress(String brgyName, String cityName, String provName){
        user = new User();
        user.setUserId(firebaseAuthHelper.getUserId());
        user.setBrgyName(brgyName);
        user.setCityName(cityName);
        user.setProvName(provName);

        firebaseFirestoreHelper.updateAddress(user, task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "updateAddress: Successfully updated address");
                displayName();
                setTextNull(etEditBrgy);
                setTextNull(etEditCity);
                setTextNull(etEditProv);
            }else{
                Log.d(TAG, "updateAddress: " + task.getException().toString());
            }
        });
    }

    private void updateEmail(String email, String password){
        firebaseAuthHelper.changeEmail(email, password, task -> {
            Log.d(TAG, "updateEmail: " + email);
            if(task.isSuccessful()){
                Log.d(TAG, "updateEmail: Successfully updated email");
                displayEmail();
                setTextNull(etEditEmailPass);
                setTextNull(etEditEmail);
            }else{
                Log.d(TAG, "updateEMail: " + task.getException().toString());
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
    public void disableCurrentInfo(View fragmentView){
        View view;

        int[] editTextIds = {R.id.etCurrentName, R.id.etCurrentAddress, R.id.etCurrentEmail};

        for(int id : editTextIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setEnabled(false);
            }
        }
    }
    public void showEditName(View fragmentView){
        View view;

        int[] textViewIds = {R.id.textView38,R.id.textView39,R.id.textView40};
        int[] editTextIds = {R.id.etEditFName, R.id.etEditMName, R.id.etEditLName};

        for(int id : textViewIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setVisibility(View.VISIBLE);
            }
        }

        for(int id : editTextIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hideEditName(View fragmentView){
        View view;

        int[] textViewIds = {R.id.textView38,R.id.textView39,R.id.textView40};
        int[] editTextIds = {R.id.etEditFName, R.id.etEditMName, R.id.etEditLName};

        for(int id : textViewIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setVisibility(View.GONE);
            }
        }

        for(int id : editTextIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setVisibility(View.GONE);
            }
        }
    }


    public void showEditAddress(View fragmentView){
        View view;

        int[] textViewIds = {R.id.textView41,R.id.textView42,R.id.textView44};
        int[] editTextIds = {R.id.etEditBarangay, R.id.etEditCity, R.id.etEditProvince};

        for(int id : textViewIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setVisibility(View.VISIBLE);
            }
        }

        for(int id : editTextIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void hideEditAddress(View fragmentView){
        View view;

        int[] textViewIds = {R.id.textView41,R.id.textView42,R.id.textView44};
        int[] editTextIds = {R.id.etEditBarangay, R.id.etEditCity, R.id.etEditProvince};

        for(int id : textViewIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof TextView){
                TextView textView = (TextView) view;
                textView.setVisibility(View.GONE);
            }
        }

        for(int id : editTextIds){
            view  = fragmentView.findViewById(id);
            if(view instanceof EditText){
                EditText editText = (EditText) view;
                editText.setVisibility(View.GONE);
            }
        }
    }

    public void showEditEmail(){
        textView56.setVisibility(View.VISIBLE);
        textView48.setVisibility(View.VISIBLE);
        etEditEmail.setVisibility(View.VISIBLE);
        etEditEmailPass.setVisibility(View.VISIBLE);
    }

    public void hideEditEmail(){
        textView56.setVisibility(View.GONE);
        textView48.setVisibility(View.GONE);
        etEditEmail.setText(null);
        etEditEmailPass.setText(null);
        etEditEmailPass.setVisibility(View.GONE);
        etEditEmail.setVisibility(View.GONE);
    }
}