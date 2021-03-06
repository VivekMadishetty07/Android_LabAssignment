package com.example.mymap.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Usernotes")
public class UserData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    String country;
    String state;
    String postalCode;
    String address;
    String city;
    String knownName;
    String apicity;
    String apicountrycode;
    double latitude;
    double longitude;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getKnownName() {
        return knownName;
    }

    public void setKnownName(String knownName) {
        this.knownName = knownName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getApicity() {
        return apicity;
    }

    public void setApicity(String apicity) {
        this.apicity = apicity;
    }

    public String getApicountrycode() {
        return apicountrycode;
    }

    public void setApicountrycode(String apicountrycode) {
        this.apicountrycode = apicountrycode;
    }
}
