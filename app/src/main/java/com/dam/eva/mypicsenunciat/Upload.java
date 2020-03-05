package com.dam.eva.mypicsenunciat;

public class Upload {
    private String mName;
    private String mImage;
    public Upload(){

    }

    public Upload(String mName, String mImage) {
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
}
