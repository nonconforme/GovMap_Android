package com.govmap.utils;


import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by MediumMG on 04.09.2015.
 */
public class GeocodeClient {

    private static int TIMEOUT = 30000; // in millisecond

    private static String API_LOBBY_URL = "https://maps.googleapis.com";

    private static GeocodeApi REST_CLIENT_LOBBY;

    static {
        setupRestClient();
    }

    private GeocodeClient() {
    }

    public static GeocodeApi get() {
        return REST_CLIENT_LOBBY;
    }

    private static void setupRestClient() {

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(API_LOBBY_URL)
                .setClient(new OkClient(new OkHttpClient()))
                .setLogLevel(RestAdapter.LogLevel.FULL);

        RestAdapter restAdapter = builder.build();
        REST_CLIENT_LOBBY = restAdapter.create(GeocodeApi.class);
    }
}
