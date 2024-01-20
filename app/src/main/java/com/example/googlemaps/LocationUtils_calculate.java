package com.example.googlemaps;


import com.google.android.gms.maps.model.LatLng;

public class LocationUtils_calculate{
    private static final double EARTH_RADIUS = 6371.0; // Earth radius in kilometers
    public static double calculateDistance(LatLng location1, LatLng location2) {//Using Haversine formula
        double lat1 = Math.toRadians(location1.latitude);
        double lon1 = Math.toRadians(location1.longitude);
        double lat2 = Math.toRadians(location2.latitude);
        double lon2 = Math.toRadians(location2.longitude);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
    // Reference https://en.wikipedia.org/wiki/Haversine_formula
}

