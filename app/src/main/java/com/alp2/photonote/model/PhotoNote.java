package com.alp2.photonote.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PhotoNote implements Parcelable {
    private String text;
    private float x;
    private float y;

    public PhotoNote(String text, float x, float y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    protected PhotoNote(Parcel in) {
        text = in.readString();
        x = in.readFloat();
        y = in.readFloat();
    }

    public static final Creator<PhotoNote> CREATOR = new Creator<PhotoNote>() {
        @Override
        public PhotoNote createFromParcel(Parcel in) {
            return new PhotoNote(in);
        }

        @Override
        public PhotoNote[] newArray(int size) {
            return new PhotoNote[size];
        }
    };

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeFloat(x);
        dest.writeFloat(y);
    }
} 