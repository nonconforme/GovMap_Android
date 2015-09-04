package com.govmap.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MediumMG on 03.09.2015.
 */
public class DataObject implements Parcelable {

    private String mCadastre = "";
    private String mAddress = "";
    private double mLatitude = 0.d;
    private double mLongitude = 0.d;

    public DataObject() {
    }

    protected DataObject(Parcel in) {
        mCadastre = in.readString();
        mAddress = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCadastre);
        dest.writeString(mAddress);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
    }

    public static final Creator<DataObject> CREATOR = new Creator<DataObject>() {
        @Override
        public DataObject createFromParcel(Parcel in) {
            return new DataObject(in);
        }

        @Override
        public DataObject[] newArray(int size) {
            return new DataObject[size];
        }
    };

    public String getCadastre() {
        return mCadastre;
    }

    public void setCadastre(String cadastre) {
        this.mCadastre = cadastre;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String adress) {
        this.mAddress = adress;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "mCadastre='" + mCadastre + '\'' +
                ", mAddress='" + mAddress + '\'' +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                '}';
    }
}
