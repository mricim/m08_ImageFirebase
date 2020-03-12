package com.dam.eva.mypicsenunciat;

import com.google.firebase.database.Exclude;

public class Upload {
    private String mName;
    private String mImage;
    private String key;

    public Upload() {

    }

    public Upload(String mName, String mImage) {
        if (mName.trim().equals("")) {
            mName = "no name";
        }
        this.mName = mName;
        this.mImage = mImage;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String mImage) {
        this.mImage = mImage;
    }

    @Exclude
    public String getKey() {
        return key;
    }
}
