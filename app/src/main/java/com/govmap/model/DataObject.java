package com.govmap.model;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by MediumMG on 03.09.2015.
 */
public class DataObject implements Parcelable {

    private int mBlock = -1;
    private int mSmooth = -1;
    private String mSearchAddress = "";
    private String mShowedAddress = "";
    private double mLatitude = Double.MAX_VALUE;
    private double mLongitude = Double.MAX_VALUE;

    public DataObject() {
    }

    protected DataObject(Parcel in) {
        mBlock = in.readInt();
        mSmooth = in.readInt();
        mSearchAddress = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mShowedAddress = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBlock);
        dest.writeInt(mSmooth);
        dest.writeString(mSearchAddress);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeString(mShowedAddress);
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

    public int getBlock() {
        return mBlock;
    }

    public int getSmooth() {
        return mSmooth;
    }

    public void setCadastre(int block, int smooth) {
        this.mBlock = block;
        this.mSmooth = smooth;
    }

    public String getSearchAddress() {
        return mSearchAddress;
    }

    public void setSearchAddress(String adress) {
        this.mSearchAddress = adress;
    }

    public String getShowedAddress() {
        return mShowedAddress;
    }

    public void setShowedAddress(String adress) {
        this.mShowedAddress = adress;
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
                "mBlock='" + mBlock + '\'' +
                ", mSmooth='" + mSmooth + '\'' +
                ", mSearchAddress='" + mSearchAddress + '\'' +
                ", mLatitude=" + mLatitude +
                ", mLongitude=" + mLongitude +
                '}';
    }
}
