package com.govmap.utils;

import com.govmap.model.GeocodeResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by MediumMG on 04.09.2015.
 */
public interface GeocodeApi {

    @GET("/maps/api/geocode/json")
    public void getGeocodeByAddress(@Query("address") String address,
                                    @Query("language") String language,
                                    Callback<GeocodeResponse> callback);

    @GET("/maps/api/geocode/json")
    public void getGeocodeByLatLng(@Query("latlng") String latLng,
                                   @Query("language") String language,
                                   Callback<GeocodeResponse> callback);

}
