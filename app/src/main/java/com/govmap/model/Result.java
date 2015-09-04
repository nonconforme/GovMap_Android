package com.govmap.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Result{

    @SerializedName("address_components")
    public ArrayList<AddressComponent> addressComponents;

    @SerializedName("formatted_address")
    public String formattedAddress;

    @SerializedName("geometry")
    public Geometry geometry;

    @SerializedName("partial_match")
    public boolean partialMatch;

    @SerializedName("place_id")
    public String placeId;

    @SerializedName("types")
    public ArrayList<String> types;

}