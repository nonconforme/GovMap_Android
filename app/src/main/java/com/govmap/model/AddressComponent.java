package com.govmap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AddressComponent{

    @SerializedName("short_name")
    public String shortName;

    @SerializedName("long_name")
    public String longName;

    @SerializedName("types")
    public ArrayList<String> types;
}
