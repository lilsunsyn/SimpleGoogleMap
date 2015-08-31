package com.shilu.vayumaptest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by shilushrestha on 8/28/15.
 */
public class MapObject implements Parcelable {
    double lat;
    double lng;
    String address;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    protected MapObject(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        address = in.readString();
    }

    public static final Creator<MapObject> CREATOR = new Creator<MapObject>() {
        @Override
        public MapObject createFromParcel(Parcel in) {
            return new MapObject(in);
        }

        @Override
        public MapObject[] newArray(int size) {
            return new MapObject[size];
        }
    };

    public MapObject() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(address);
    }
}
