package com.example.snapsoil;


import com.google.firebase.Timestamp;

import java.util.Date;

public class User {
    private String userId, fName, mName, lName, brgyName, cityName, provName;
    private String bDay;
    private Timestamp createAt;
    private FirebaseAuthHelper firebaseAuthHelper;

    public User(){
    }

    public User(String fName, String mName, String lName, String brgyName, String cityName, String provName, String bDay, Timestamp createAt) {
        this.fName = fName;
        this.mName = mName;
        this.lName = lName;
        this.brgyName = brgyName;
        this.cityName = cityName;
        this.provName = provName;
        this.bDay = bDay;
        this.createAt = createAt;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getBDay(){
        return bDay;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getBrgyName() {
        return brgyName;
    }

    public void setBrgyName(String brgyName) {
        this.brgyName = brgyName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvName() {
        return provName;
    }

    public void setProvName(String provName) {
        this.provName = provName;
    }

    public Timestamp getCreateAt(){
        return createAt;
    }
}
