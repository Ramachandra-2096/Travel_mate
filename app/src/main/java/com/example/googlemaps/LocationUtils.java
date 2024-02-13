package com.example.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

public class LocationUtils {

    private static final int CHECK_INTERVAL = 1000; // 1 second interval for checking location status

    public static void checkLocationStatus(final Context context, final OnLocationEnabledListener listener) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location is already enabled
            listener.onLocationEnabled();
        } else {
            // Location is not enabled, prompt the user to enable it
            listener.onLocationDisabled();
            // Open location settings
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            // Check location status continuously
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        // Location is enabled now
                        listener.onLocationEnabled();
                        handler.removeCallbacks(this); // Stop continuous checking
                    } else {
                        // Continue checking location status
                        handler.postDelayed(this, CHECK_INTERVAL);
                    }
                }
            }, CHECK_INTERVAL);
        }
    }

    public interface OnLocationEnabledListener {
        void onLocationEnabled();

        void onLocationDisabled();
    }

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
    }

    public static void getCurrentLocation(Context context, LocationCallback callback) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check if GPS is enabled
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Request location updates
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Handle location update
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Invoke the callback with the location
                    callback.onLocationReceived(latitude, longitude);

                    // Remove location updates
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, null);
        } else {
            // GPS is not enabled, handle accordingly
            // You might want to prompt the user to enable GPS
            // or use other location providers (network, etc.)
        }
    }
}
