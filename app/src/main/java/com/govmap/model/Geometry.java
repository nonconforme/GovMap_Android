package com.govmap.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MediumMG on 04.09.2015.
 */
public class Geometry {

    @SerializedName("formatted_address")
    public String formattedAddress;

    @SerializedName("location")
    public LocationCoord location;

}
