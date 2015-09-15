package com.govmap.utils;

/**
 * Created by MediumMG on 14.09.2015.
 */
public enum  DataSearchType {

    ADDRESS(0),
    COORDINATES(1),
    CADASTRE(2),
    CURRENT_LOCATION(3);

    private final int value;

    private DataSearchType(int value) {
        this.value = value;
    }

    public static DataSearchType getEnum(int value) {
        switch (value) {
            case 0: return ADDRESS;
            case 1: return COORDINATES;
            case 2: return CADASTRE;
            case 3: return CURRENT_LOCATION;
            default: return null;
        }
    }

}
